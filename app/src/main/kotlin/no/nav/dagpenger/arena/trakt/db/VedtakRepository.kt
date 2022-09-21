package no.nav.dagpenger.arena.trakt.db

import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.Sak
import no.nav.dagpenger.arena.trakt.Vedtak
import no.nav.dagpenger.arena.trakt.db.SakRepository.SakObserver
import org.intellij.lang.annotations.Language

internal class VedtakRepository private constructor(
    private val sakRepository: SakRepository,
    private val observers: MutableList<VedtakObserver>
) : SakObserver {
    constructor(sakRepository: SakRepository) : this(sakRepository, mutableListOf())

    init {
        sakRepository.leggTilObserver(FinnUsendteVedtak())
        sakRepository.leggTilObserver(SlettVedtakFraAndreYtelser())
    }

    companion object {
        @Language("PostgreSQL")
        private val lagreQuery = """
            |INSERT INTO vedtak (vedtak_id,
            |                    sak_id,
            |                    person_id,
            |                    vedtaktypekode,
            |                    utfallkode,
            |                    rettighetkode,
            |                    vedtakstatuskode, 
            |                    opprettet, 
            |                    oppdatert,
            |                    saknummer,
            |                    lopenummer)
            |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
            |ON CONFLICT (vedtak_id, oppdatert) DO NOTHING
        """.trimMargin()

        @Language("PostgreSQL")
        private const val slettQuery = "DELETE FROM vedtak WHERE vedtak_id = ?"

        @Language("PostgreSQL")
        private const val finnUsendteVedtakQuery = """
            SELECT v.*
            FROM vedtak v
                LEFT JOIN hendelse_vedtak hv ON v.vedtak_id = hv.vedtak_id
            WHERE hv.vedtak_id IS NULL AND v.sak_id = ? LIMIT 1000
        """

        @Language("PostgreSQL")
        private const val gjenbruktVedtakIdQuery = "SELECT COUNT(1) FROM vedtak WHERE vedtak_id=? AND sak_id != ?"
    }

    fun leggTilObserver(observer: VedtakObserver) = observers.add(observer)

    fun lagre(vedtak: Vedtak): Int {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            if (session.gjenbruktVedtakId(vedtak)) throw IllegalArgumentException("Samme vedtakId er brukt i flere saker")

            session.run(
                queryOf(
                    lagreQuery,
                    vedtak.vedtakId,
                    vedtak.sakId,
                    vedtak.personId,
                    vedtak.vedtaktypekode,
                    vedtak.utfallkode,
                    vedtak.rettighetkode,
                    vedtak.vedtakstatuskode,
                    vedtak.opprettet,
                    vedtak.oppdatert,
                    vedtak.saknummer,
                    vedtak.løpenummer
                ).asUpdate
            )
        }.also {
            when (sakRepository.erDagpenger(vedtak.sakId)) {
                true -> emitNyttDagpengeVedtak(vedtak)
                false -> slett(vedtak)
                null -> {
                    // Vi må vente til vi får sak, så vi kan avgjøre om det er dagpenger
                }
            }
        }
    }

    private fun Session.gjenbruktVedtakId(vedtak: Vedtak) = run(
        queryOf(
            gjenbruktVedtakIdQuery,
            vedtak.vedtakId,
            vedtak.sakId
        ).map { it.int(1) >= 1 }.asSingle
    ) ?: false

    private fun emitNyttDagpengeVedtak(vedtak: Vedtak) =
        observers.forEach { it.nyttDagpengeVedtak(vedtak) }

    fun finnUsendteVedtakMedSak(sakId: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(queryOf(finnUsendteVedtakQuery, sakId).map { it.vedtak() }.asList)
        }

    internal interface VedtakObserver {
        fun nyttDagpengeVedtak(vedtak: Vedtak) {}
    }

    private fun slett(vedtak: Vedtak) = slett(vedtak.vedtakId)

    private fun slett(vedtakId: Int) = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(queryOf(slettQuery, vedtakId).asExecute)
    }

    // Finner dagpengevedtak som ankom før vi hadde sak
    private inner class FinnUsendteVedtak : SakObserver {
        override fun nySak(sak: Sak) {
            if (!sak.erDagpenger) return
            finnUsendteVedtakMedSak(sak.sakId).forEach(::emitNyttDagpengeVedtak)
        }
    }

    // Sletter vedtak fra andre ytelser når vi får saken
    private inner class SlettVedtakFraAndreYtelser : SakObserver {
        override fun nySak(sak: Sak) {
            if (sak.erDagpenger) return
            slettVedtakMedSak(sak.sakId)
        }
    }

    private fun slettVedtakMedSak(sakId: Int) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    "DELETE FROM vedtak WHERE sak_id = ?",
                    sakId
                ).asExecute
            )
        }
    }

    private fun Row.vedtak() = Vedtak(
        sakId = int("sak_id"),
        vedtakId = int("vedtak_id"),
        personId = int("person_id"),
        vedtaktypekode = string("vedtaktypekode"),
        utfallkode = string("utfallkode"),
        rettighetkode = string("rettighetkode"),
        vedtakstatuskode = string("vedtakstatuskode"),
        opprettet = localDateTime("opprettet"),
        oppdatert = localDateTime("oppdatert"),
        saknummer = string("saknummer"),
        løpenummer = int("lopenummer")
    )
}
