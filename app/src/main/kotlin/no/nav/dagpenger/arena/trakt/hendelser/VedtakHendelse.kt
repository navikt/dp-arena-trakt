package no.nav.dagpenger.arena.trakt.hendelser

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import no.nav.dagpenger.arena.trakt.Vedtak
import java.time.LocalDateTime
import java.util.UUID

internal class VedtakHendelse(
    private val vedtak: Vedtak,
) : Hendelse(UUID.randomUUID()) {
    internal val vedtakId = vedtak.vedtakId
    internal val oppdatert = vedtak.oppdatert
    private val root: ObjectNode = objectMapper.createObjectNode()
    private val rettighetstype get() = Rettighetstype.valueOf(vedtak.rettighetkode).navn
    private val vedtakstype get() = Vedtakstype.valueOf(vedtak.vedtaktypekode).navn
    private val status get() = Status.valueOf(vedtak.vedtakstatuskode).navn
    private val utfall get() = Utfall.valueOf(vedtak.utfallkode).navn

    init {
        root.put("@event_name", "vedtak")
        root.put("@kilde", "arena")
        root.put("@opprettet", LocalDateTime.now().toString())
        root.put("@meldingId", meldingId.toString())
        root.put("vedtakId", vedtak.vedtakId)
        root.put("sakId", vedtak.sakId)
        root.put("personId", vedtak.personId)
        root.put("rettighet", rettighetstype)
        root.put("type", vedtakstype)
        root.put("status", status)
        root.put("utfall", utfall)
        root.put("opprettet", vedtak.opprettet.toString())
        root.put("oppdatert", vedtak.oppdatert.toString())
        root.put("saknummer", vedtak.saknummer)
        root.put("løpenummer", vedtak.løpenummer)
    }

    private enum class Rettighetstype(
        val navn: String,
    ) {
        DAGO("Ordinær"),
        PERM("Permittering"),
        LONN("Lønnsgarantimidler"),
        FISK("Permittering fiskeindustri"),
        DEKS("Eksport"),
    }

    private enum class Vedtakstype(
        val navn: String,
    ) {
        E("Endring"),
        F("Forlenget ventetid"),
        G("Gjenopptak"),
        N("Annuller sanksjon"),
        O("Ny rettighet"),
        S("Stans"),
        T("Tidsbegrenset bortfall"),
    }

    private enum class Status(
        val navn: String,
    ) {
        AVSLU("Avsluttet"),
        GODKJ("Godkjent"),
        INNST("Innstilt"),
        IVERK("Iverksatt"),
        MOTAT("Mottatt"),
        OPPRE("Opprettet"),
        REGIS("Registrert"),
    }

    private enum class Utfall(
        val navn: String,
    ) {
        JA("Ja"),
        NEI("Nei"),
        AVBRUTT("Avbrutt"),
    }

    override fun message(): JsonMessage =
        JsonMessage(
            root.toString(),
            MessageProblems(root.toString()),
            metrics = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
        )
}
