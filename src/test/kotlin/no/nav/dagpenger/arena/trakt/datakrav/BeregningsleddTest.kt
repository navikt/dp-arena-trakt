package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.db.PostgresDataSourceBuilder
import no.nav.dagpenger.arena.trakt.helpers.beregningsleddJSON
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BeregningsleddTest {
    private val repository = DataRepository()
    private val vedtak = Hendelse(Hendelse.Type.VedtakIverksatt, "123") {}
    private val beregningsledd = Beregningsledd("DPTEL").apply { hendelse = vedtak }

    @Test
    fun `Beregningsleddkrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(beregningsledd.oppfylt())
        }
    }

    @Test
    fun `Beregningsleddkrav er oppfylt`() {
        withMigratedDb {
            repository.lagre(beregningsleddJSON())
            assertTrue(beregningsledd.oppfylt())
        }

        PostgresDataSourceBuilder.dataSource
    }
}
