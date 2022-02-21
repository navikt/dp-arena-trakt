package no.nav.dagpenger.arena.trakt.db

internal class VedtakParser {
    //Forstår json og skal produserer vedtak

    fun parse(vedtaksData: List<String>): DagpengeVedtak = lagVedtak(vedtaksData)

    private fun lagVedtak(vedtaksData: List<String>): DagpengeVedtak {
        /* vedtaksData.forEach {
             val json = ObjectMapper().readTree(it.reader())
             when(json["tabell"].asText()){
             }
         }*/
        return UfullstendigDagpengeVedtak;
    }


    internal interface DagpengeVedtak
    internal class FullstendigDagpengevedtak : DagpengeVedtak
    internal object UfullstendigDagpengeVedtak : DagpengeVedtak //nullobject
}
