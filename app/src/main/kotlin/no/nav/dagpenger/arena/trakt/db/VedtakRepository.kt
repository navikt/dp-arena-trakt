package no.nav.dagpenger.arena.trakt.db

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.SakRepository.SakObserver
import no.nav.dagpenger.arena.trakt.tjenester.SakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService.Vedtak
import org.intellij.lang.annotations.Language

internal class VedtakRepository private constructor(
    private val sakRepository: SakRepository,
    private val observers: MutableList<VedtakObserver>,
) : SakObserver {
    constructor(sakRepository: SakRepository) : this(sakRepository, mutableListOf())

    init {
        sakRepository.leggTilObserver(FinnUsendteVedtak(this))
        sakRepository.leggTilObserver(SlettVedtakFraAndreYtelser(this))
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
            |                    vedtakstatuskode)
            |VALUES (?, ?, ?, ?, ?, ?, ?)
            |ON CONFLICT (vedtak_id) DO NOTHING
        """.trimMargin()

        @Language("PostgreSQL")
        private val slettQuery = "DELETE FROM vedtak WHERE vedtak_id = ?"
    }

    fun leggTilObserver(observer: VedtakObserver) = observers.add(observer)

    fun lagre(vedtak: Vedtak): Int {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    vedtak.vedtakId,
                    vedtak.sakId,
                    vedtak.personId,
                    vedtak.vedtaktypekode,
                    vedtak.utfallkode,
                    vedtak.rettighetkode,
                    vedtak.vedtakstatuskode
                ).asUpdate
            )
        }.also {
            when (sakRepository.erDagpenger(vedtak.sakId)) {
                true -> observers.forEach { it.nyttDagpengeVedtak(vedtak) }
                false -> slett(vedtak)
                null -> {
                    // Vi må vente til vi får sak, så vi kan avgjøre om det er dagpenger
                }
            }
        }
    }

    private fun slett(vedtak: Vedtak) = slett(vedtak.vedtakId)

    private fun slett(vedtakId: Int) = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
        session.run(queryOf(slettQuery, vedtakId).asExecute)
    }

    private class FinnUsendteVedtak(private val repository: VedtakRepository) : SakObserver {
        override fun nySak(sak: SakService.Sak) {
            if (!sak.erDagpenger) return
            repository.finnUsendteVedtakMedSak(sak.sakId).forEach { vedtak ->
                repository.observers.forEach { it.nyttDagpengeVedtak(vedtak) }
            }
        }
    }

    private class SlettVedtakFraAndreYtelser(private val repository: VedtakRepository) : SakObserver {
        override fun nySak(sak: SakService.Sak) {
            if (sak.erDagpenger) return
            repository.slettVedtakMedSak(sak.sakId)
        }
    }

    private fun finnUsendteVedtakMedSak(sakId: Int) =
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    "SELECT * FROM vedtak WHERE sak_id = ?", sakId
                ).map { it.vedtak() }.asList
            )
        }

    private fun slettVedtakMedSak(sakId: Int) {
        using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    "DELETE FROM vedtak WHERE sak_id = ?", sakId
                ).asExecute
            )
        }
    }

    interface VedtakObserver {
        fun nyttDagpengeVedtak(vedtak: Vedtak) {}
    }

    private fun Row.vedtak() = Vedtak(
        sakId = int("sak_id"),
        vedtakId = int("vedtak_id"),
        personId = int("person_id"),
        vedtaktypekode = string("vedtaktypekode"),
        utfallkode = string("utfallkode"),
        rettighetkode = string("rettighetkode"),
        vedtakstatuskode = string("vedtakstatuskode")
    )
}
