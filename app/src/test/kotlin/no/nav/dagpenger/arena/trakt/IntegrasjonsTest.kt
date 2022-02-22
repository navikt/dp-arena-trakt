package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.HendelseRepository
import no.nav.dagpenger.arena.trakt.db.PubliserNyttVedtakObserver
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.tjenester.DAGPENGER_SAKSKODE
import no.nav.dagpenger.arena.trakt.tjenester.SakSink
import no.nav.dagpenger.arena.trakt.tjenester.VedtakSink
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class IntegrasjonsTest {
    private val testRapid = TestRapid()
    private val hendelseRepository = HendelseRepository(testRapid)
    private val sakRepository = SakRepository()
    private val vedtakRepository = VedtakRepository(sakRepository).also {
        it.leggTilObserver(PubliserNyttVedtakObserver(hendelseRepository))
    }

    @BeforeEach
    fun setup() {
        SakSink(testRapid, sakRepository)
        VedtakSink(testRapid, vedtakRepository)
    }

    @Test
    fun `Med dagpengevedtak kommer en hendelse ut`() {
        withMigratedDb {
            testRapid.sendTestMessage(vedtakJSON(1, 2))
            testRapid.sendTestMessage(sakJSON(2, DAGPENGER_SAKSKODE))

            with(testRapid.inspektør) {
                assertEquals(1, size)
                val vedtakHendelse = message(0)
                assertEquals("vedtak", vedtakHendelse["@event_name"].asText())
                assertEquals("arena", vedtakHendelse["kilde"].asText())
                assertEquals(1, vedtakHendelse["vedtakId"].asInt())
                assertEquals(2, vedtakHendelse["sakId"].asInt())
            }
        }
    }
}
