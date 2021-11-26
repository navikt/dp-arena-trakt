package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.BeregningsleddJSON
import no.nav.dagpenger.arena.trakt.IverksattVedtak
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BeregningsleddTest {
    private val repository = DataRepository()
    private val beregningsledd = Beregningsledd("DPTEL")
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
            repository.lagre(BeregningsleddJSON)
            assertTrue(beregningsledd.oppfyltFor(vedtak))
        }
    }
}
