package no.nav.dagpenger.arena.trakt.db

internal class VedtakParser {
    fun parse(vedtaksData: List<String>): DagpengeVedtak {
        return UfullstendigDagpengeVedtak
    }

    internal interface DagpengeVedtak
    internal class FullstendigDagpengevedtak : DagpengeVedtak
    internal object UfullstendigDagpengeVedtak : DagpengeVedtak
}
