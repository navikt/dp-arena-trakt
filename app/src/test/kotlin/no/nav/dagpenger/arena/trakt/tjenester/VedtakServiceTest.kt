package no.nav.dagpenger.arena.trakt.tjenester

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.arena.trakt.db.VedtakRepository
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class VedtakServiceTest {
    private val vedtakRepository = mockk<VedtakRepository>(relaxed = true)
    private val rapid = TestRapid().also {
        VedtakService(it, vedtakRepository)
    }

    @Test
    fun `lagrer vedtak`() {
        rapid.sendTestMessage(vedtakJSON())

        verify {
            vedtakRepository.lagre(ofType())
        }
    }
}
