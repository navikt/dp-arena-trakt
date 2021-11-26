package no.nav.dagpenger.arena.trakt.datakrav

import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.Hendelse.Type.VedtakIverksatt
import no.nav.dagpenger.arena.trakt.db.DataRepository
import no.nav.dagpenger.arena.trakt.helpers.Postgres.withMigratedDb
import no.nav.dagpenger.arena.trakt.helpers.VedtakJSON
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtakTest {
    private val repository = DataRepository()
    private val vedtak = Vedtak()
    private val hendelse = Hendelse(VedtakIverksatt, "123", vedtak)

    @Test
    fun `Vedtak finnes ikke enda`() {
        withMigratedDb {
            assertFalse(vedtak.oppfyltFor(hendelse))
        }
    }

    @Test
    fun `Vedtak finnes`() {
        withMigratedDb {
            repository.lagre(VedtakJSON)
            assertTrue(vedtak.oppfyltFor(hendelse))
        }
    }
}
