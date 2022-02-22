package no.nav.dagpenger.arena.trakt.tjenester

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.arena.trakt.db.SakRepository
import no.nav.dagpenger.arena.trakt.helpers.sakJSON
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class SakSinkTest {
    private val repository = mockk<SakRepository>(relaxed = true)
    private val rapid = TestRapid().also {
        SakSink(it, repository)
    }

    @Test
    fun `lagrer sak`() {
        rapid.sendTestMessage(sakJSON())

        verify {
            repository.lagre(ofType())
        }
    }
}
