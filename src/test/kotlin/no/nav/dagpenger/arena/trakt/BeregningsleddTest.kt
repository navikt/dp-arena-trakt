package no.nav.dagpenger.arena.trakt

import no.nav.dagpenger.arena.trakt.db.BeregningsleddRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BeregningsleddTest {
    private val repository = BeregningsleddRepository()
    private val beregningsledd = Beregningsledd("DPTEL", repository)
    private val vedtak = IverksattVedtak("123", beregningsledd)

    @Test
    fun `Beregningsleddkrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(beregningsledd.oppfyltFor(vedtak))
        }
    }

    @Test
    fun `Beregningsleddkrav er oppfylt`() {
        withMigratedDb {
            repository.insert(BeregningsleddJSON)
            assertTrue(beregningsledd.oppfyltFor(vedtak))
        }
    }
}
