package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.db.SakRepository.SakObserver
import no.nav.dagpenger.arena.trakt.tjenester.SakService
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService.Vedtak
import org.intellij.lang.annotations.Language

internal class VedtakRepository : SakObserver {
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
    }

    val observers = mutableListOf<VedtakObserver>()

    fun lagre(vedtak: Vedtak) {
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
            val erDagpenger = session.run(
                queryOf(
                    "SELECT er_dagpenger AS er_dagpenger FROM sak WHERE sak_id = ?",
                    vedtak.sakId
                ).map {
                    it.boolean("er_dagpenger")
                }.asSingle
            )

            when (erDagpenger) {
                true -> observers.forEach { it.nyttDagpengeVedtak(vedtak) }
                false -> slett(vedtak)
                null -> { /*Vent*/
                }
            }
        }
    }

    private fun slett(vedtak: Vedtak) {
        TODO("Not yet implemented")
    }

    override fun nySak(sak: SakService.Sak) {
        if (sak.erDagpenger) {
            // finn vedtak som ikke er sendt
            finnUsendteVedtakMedSak(sak.sakId).forEach { vedtak ->
                observers.forEach { it.nyttDagpengeVedtak(vedtak) }
            }
        } else {
            // finn evt. vedtak som skal slettes
            slettVedtakMedSak(sak.sakId)
        }
    }

    private fun finnUsendteVedtakMedSak(sakId: Int): List<Vedtak> {
        TODO("Not yet implemented")
    }

    private fun slettVedtakMedSak(sakId: Int) {
        TODO("Not yet implemented")
    }

    interface VedtakObserver {
        fun nyttDagpengeVedtak(vedtak: Vedtak) {}
    }
}
