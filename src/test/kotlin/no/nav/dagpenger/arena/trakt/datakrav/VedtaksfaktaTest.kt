package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.vedtaksfaktaJSON
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtaksfaktaTest {
    private val repository = DataRepository()
    private val vedtak = Hendelse.vedtak("123")
    private val vedtaksfakta = Vedtaksfakta("ENDRTILUNN").apply { hendelse = vedtak.hendelseId }

    @Test
    fun `Vedtaksfaktakrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(vedtaksfakta.oppfylt())
        }
    }

    @Test
    fun `Vedtaksfaktakrav er oppfylt`() {
        withMigratedDb {
            repository.lagre(vedtaksfaktaJSON())
            assertTrue(vedtaksfakta.oppfylt())
        }
    }
}
