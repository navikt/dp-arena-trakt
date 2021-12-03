package no.nav.dagpenger.arena.trakt.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav.Resultat
import no.nav.dagpenger.arena.trakt.datakrav.Vedtak
import no.nav.dagpenger.arena.trakt.datakrav.VedtakData
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta

internal class VedtakHendelseJsonBuilder(vedtak: Hendelse) : HendelseVisitor {
    private lateinit var vedtakstype: Vedtakstype
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val fakta = mapper.createArrayNode()

    init {
        vedtak.accept(this)
    }

    fun resultat() = root

    override fun preVisit(hendelse: Hendelse, type: Hendelse.Type, id: String) {
        root.put("vedtakId", id)

        root.replace("fakta", fakta)
    }

    override fun <T> visit(datakrav: Datakrav<T>, id: String, resultat: Resultat<T>?, oppfylt: Boolean) {
        if (resultat == null) throw IllegalStateException("Kan ikke lage hendelse med manglende data")
        when (datakrav) {
            is Beregningsledd -> leggTilFakta(id, resultat.data.toString())
            is Vedtaksfakta -> leggTilFakta(id, resultat.data.toString())
            is Vedtak -> {
                require(resultat.data is VedtakData)
                leggTilFakta("utfall", resultat.data.utfall)

                vedtakstype = when (resultat.data.vedtakstype) {
                    "O" -> Vedtakstype.Opprettet
                    "E" -> Vedtakstype.Endret
                    else -> throw Error("Ukjent type")
                }
            }
        }
    }

    private fun leggTilFakta(id: String, verdi: String) {
        mapper.createObjectNode().also {
            it.put("id", id)
            it.put("verdi", verdi)

            fakta.add(it)
        }
    }

    override fun postVisit(hendelse: Hendelse, type: Hendelse.Type, id: String) {
        root.put("@event_type", vedtakstype.hendelsesNavn)
    }

    private enum class Vedtakstype(val hendelsesNavn: String) {
        Opprettet("VedtakOpprettet"),
        Endret("VedtakEndret")
    }
}
