package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.Replikeringslogg
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IntegrationTest {
    private val testRapid = TestRapid()
    private val sakRepository = SakRepository()
    private val vedtakRepository = VedtakRepository(sakRepository)
    private val radMottak = RadMottak(sakRepository, vedtakRepository, HendelseRepository(testRapid))

    init {
        ReplikeringMediator(
            testRapid,
            radMottak,
            Replikeringslogg()
        )
    }

    @Test
    fun `Med dagpengevedtak kommer en hendelse ut`() {
        withMigratedDb {
            testRapid.sendTestMessage(vedtakJSON(1, 2))
            testRapid.sendTestMessage(vedtakJSON(1, 2))
            testRapid.sendTestMessage(vedtakJSON(1, 2))
            testRapid.sendTestMessage(sakJSON(2, "DAGP"))

            with(testRapid.inspektør) {
                assertEquals(1, size)
                val vedtakHendelse = message(0)
                println(vedtakHendelse)
                assertEquals("vedtak", vedtakHendelse["@event_name"].asText())
                assertEquals("arena", vedtakHendelse["@kilde"].asText())
                assertEquals(1, vedtakHendelse["vedtakId"].asInt())
                assertEquals(2, vedtakHendelse["sakId"].asInt())
            }
        }
    }
}
