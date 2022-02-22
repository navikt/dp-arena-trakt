package no.nav.dagpenger.arena.trakt.tjenester

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.vedtakJSON
import no.nav.dagpenger.arena.trakt.tjenester.VedtakService.Vedtak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class VedtakServiceTest {
    private val dataRepository = mockk<DataRepository>(relaxed = true)
    private val rapid = TestRapid().also {
        VedtakService(it, dataRepository)
    }

    @Test
    fun `lagrer vedtak`(){
        rapid.sendTestMessage(vedtakJSON())
        verify {
            dataRepository.lagreVedtak(ofType<Vedtak>())
        }

    }

}