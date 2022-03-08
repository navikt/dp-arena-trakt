package no.nav.dagpenger.arena.trakt.db

import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.arena.trakt.db.HendelseRepository.Companion.fraVedtak
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.vedtak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

internal class HendelseRepositoryTest {
    private val sakRepository = mockk<SakRepository>(relaxed = true).also {
        every { it.erDagpenger(any()) } returns true
    }
    private val vedtakRepository = VedtakRepository(sakRepository)
    private val testRapid = TestRapid()
    private val hendelseRepository = HendelseRepository(testRapid)

    @Test
    fun `Lager en hendelse av vedtak`() {
        withMigratedDb {
            val vedtak = vedtak().also {
                vedtakRepository.lagre(it)
            }
            val hendelse = fraVedtak(vedtak)
            hendelseRepository.publiser(hendelse)

            with(testRapid.inspektør) {
                val melding = message(0)
                assertDoesNotThrow { UUID.fromString(melding["@meldingId"].asText()) }
                assertEquals(1, melding["vedtakId"].asInt())
                assertEquals(1, melding["sakId"].asInt())
                assertEquals("Ordinær", melding["rettighet"].asText())
                assertEquals("Ny rettighet", melding["type"].asText())
                assertEquals("Iverksatt", melding["status"].asText())
                assertEquals("Ja", melding["utfall"].asText())
                assertEquals("Ja", melding["utfall"].asText())
                assertEquals("2019580493", melding["saknummer"].asText())
            }
        }
    }
}
