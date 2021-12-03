package no.nav.dagpenger.arena.trakt.serde

import no.nav.dagpenger.arena.trakt.Hendelse
import no.nav.dagpenger.arena.trakt.datakrav.Datakrav

internal interface HendelseVisitor : DatakravVisitor {
    fun preVisit(hendelse: Hendelse, type: Hendelse.Type, id: String) {}
    fun postVisit(hendelse: Hendelse, type: Hendelse.Type, id: String) {}
}

internal interface DatakravVisitor {
    fun <T> visit(datakrav: Datakrav<T>, id: String, resultat: Datakrav.Resultat<T>?, oppfylt: Boolean) {}
}
