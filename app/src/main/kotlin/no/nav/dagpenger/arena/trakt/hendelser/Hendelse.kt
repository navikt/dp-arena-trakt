package no.nav.dagpenger.arena.trakt.hendelser

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.rapids_rivers.JsonMessage
import java.util.UUID

internal abstract class Hendelse(val meldingId: UUID) {
    companion object {
        internal val objectMapper = ObjectMapper()
    }

    abstract fun message(): JsonMessage
}
