package no.nav.dagpenger.arena.trakt

internal abstract class Datakrav(val navn: String) {
    internal abstract val tabell: String
     internal abstract fun oppfyltFor(vedtak: Vedtak): Boolean

}

internal class Beregningsledd(navn: String) : Datakrav(navn) {
    override val tabell: String = "SIAMO.BEREGNINGSLEDD"
    override fun oppfyltFor(vedtak: Vedtak): Boolean {
        val query = """SELECT * FROM $tabell 
            |WHERE BEREGNINGSLEDDKODE=$navn AND TABELLNAVNALIAS_KILDE=vedtak AND OBJEKT_ID_KILDE=${vedtak.id}""".trimMargin()
        return false
    }
}

internal class Vedtaksfakta(navn: String) : Datakrav(navn) {
    override val tabell: String = "SIAMO.VEDTAKFAKTA"
    override fun oppfyltFor(vedtak: Vedtak): Boolean {
        val query = """SELECT * FROM $tabell 
            |WHERE VEDTAKFAKTAKODE=$navn AND VEDTAK_ID=${vedtak.id}""".trimMargin()
        return false
    }
}
