package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.Hendelse.Type.VedtakEndret
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.VedtaksFaktaJSON
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtaksfaktaTest {
    private val repository = DataRepository()
    private val vedtaksfakta = Vedtaksfakta("ENDRTILUNN")
    private val vedtak = Hendelse(VedtakEndret, "123", vedtaksfakta)

    @Test
    fun `Vedtaksfaktakrav er ikke oppfylt`() {
        withMigratedDb {
            assertFalse(vedtaksfakta.oppfyltFor(vedtak))
        }
    }

    @Test
    fun `Vedtaksfaktakrav er oppfylt`() {
        withMigratedDb {
            repository.lagre(VedtaksFaktaJSON)
            assertTrue(vedtaksfakta.oppfyltFor(vedtak))
        }
    }

    @Test
    fun `Vedtaksfaktakrav kan leses`() {
        withMigratedDb {
            repository.lagre(VedtaksFaktaJSON)
            assertEquals("foobar", vedtaksfakta.oppfyltFor(vedtak))
        }
    }
}
