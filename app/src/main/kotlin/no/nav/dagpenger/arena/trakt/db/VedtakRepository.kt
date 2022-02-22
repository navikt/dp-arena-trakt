package no.nav.dagpenger.arena.trakt.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService.Vedtak
import org.intellij.lang.annotations.Language

internal class VedtakRepository {
    companion object {
        @Language("PostgreSQL")
        private val lagreQuery = """
            |INSERT INTO vedtak (vedtak_id, sak_id)
            |VALUES (?, ?)
            |ON CONFLICT (vedtak_id) DO UPDATE 
            |    SET sist_oppdatert=NOW(), antall_oppdateringer = vedtak.antall_oppdateringer + 1
        """.trimMargin()
    }

    fun lagreVedtak(vedtak: Vedtak): Int {
        return using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf(
                    lagreQuery,
                    vedtak.vedtakId,
                    vedtak.sakId,
                ).asUpdate
            )
        }
    }
}
