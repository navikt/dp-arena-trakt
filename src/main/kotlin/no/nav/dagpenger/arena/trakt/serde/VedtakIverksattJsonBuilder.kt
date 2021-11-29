package no.nav.dagpenger.arena.trakt.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.datakrav.Beregningsledd
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav
import no.nav.dagpenger.arena.trakt.datakrav.Vedtak
import no.nav.dagpenger.arena.trakt.datakrav.VedtakData
import no.nav.dagpenger.arena.trakt.datakrav.Vedtaksfakta

internal class VedtakIverksattJsonBuilder(vedtak: Hendelse) : HendelseVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val fakta = mapper.createArrayNode()

    init {
        vedtak.accept(this)
    }

    fun resultat() = root

    override fun preVisit(hendelse: Hendelse, type: Hendelse.Type, id: String) {
        root.put("hendelse", type.toString())
        root.put("vedtakId", id)

        root.replace("fakta", fakta)
    }

    override fun <T> visit(datakrav: Datakrav<T>, id: String, data: T?) {
        when (datakrav) {
            is Beregningsledd -> leggTilFakta(id, data.toString())
            is Vedtaksfakta -> leggTilFakta(id, data.toString())
            is Vedtak -> {
                require(data is VedtakData)
                leggTilFakta("utfall", data.utfall)
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
}