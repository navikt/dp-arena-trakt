package no.nav.dagpenger.arena.trakt

import mu.KotlinLogging
import no.nav.dagpenger.arena.trakt.db.Replikeringslogg
import no.nav.dagpenger.arena.trakt.meldinger.ReplikeringsMelding
import no.nav.dagpenger.arena.trakt.tjenester.SakRiver
import no.nav.dagpenger.arena.trakt.tjenester.VedtakRiver
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.withMDC
import java.sql.SQLException

private val logg = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

// Kobler replikeringsmeldinger fra Kafka til meldinger i kode
internal class ReplikeringMediator(
    rapidsConnection: RapidsConnection,
    private val radMottak: IRadMottak,
    private val replikeringslogg: Replikeringslogg,
) : IReplikeringMediator {
    private var messageRecognized = false
    private val riverErrors = mutableListOf<Pair<String, MessageProblems>>()

    init {
        DelegatedRapid(rapidsConnection).also {
            SakRiver(rapidsConnection, this)
            VedtakRiver(rapidsConnection, this)
        }
    }

    fun beforeRiverHandling() {
        messageRecognized = false
        riverErrors.clear()
    }

    override fun onRecognizedMessage(
        message: ReplikeringsMelding,
        context: MessageContext,
    ) {
        try {
            messageRecognized = true
            message.logRecognized(sikkerlogg)
            replikeringslogg.lagre(message)

            if (message.skalDuplikatsjekkes && replikeringslogg.erBehandlet(message.id)) {
                message.logDuplikat(logg)
                return
            }

            radMottak.behandle(message)
            replikeringslogg.markerSomBehandlet(message.id)
        } catch (err: SQLException) {
            severeErrorHandler(err, message)
        } catch (err: Exception) {
            errorHandler(err, message)
        }
    }

    override fun onRiverError(
        riverName: String,
        problems: MessageProblems,
        context: MessageContext,
    ) {
        riverErrors.add(riverName to problems)
    }

    fun afterRiverHandling(message: String) {
        if (messageRecognized || riverErrors.isEmpty()) return
        sikkerlogg.warn(
            "kunne ikke gjenkjenne melding:\n\t$message\n\nProblemer:\n${riverErrors.joinToString(
                separator = "\n",
            ) { "${it.first}:\n${it.second}" }}",
        )
    }

    private fun severeErrorHandler(
        err: Exception,
        message: ReplikeringsMelding,
    ) {
        errorHandler(err, message)
        throw err
    }

    private fun errorHandler(
        err: Exception,
        message: ReplikeringsMelding,
    ) {
        errorHandler(err, message.toJson())
    }

    private fun errorHandler(
        err: Exception,
        message: String,
        context: Map<String, String> = emptyMap(),
    ) {
        logg.error("alvorlig feil: ${err.message} (se sikkerlogg for melding)", err)
        withMDC(context) { sikkerlogg.error("alvorlig feil: ${err.message}\n\t$message", err) }
    }

    private inner class DelegatedRapid(
        private val rapidsConnection: RapidsConnection,
    ) : RapidsConnection(),
        RapidsConnection.MessageListener {
        override fun rapidName() = "replikeringsMediator"

        init {
            rapidsConnection.register(this)
        }

        override fun onMessage(
            message: String,
            context: MessageContext,
        ) {
            beforeRiverHandling()
            notifyMessage(message, context)
            afterRiverHandling(message)
        }

        override fun publish(message: String) {
            rapidsConnection.publish(message)
        }

        override fun publish(
            key: String,
            message: String,
        ) {
            rapidsConnection.publish(key, message)
        }

        override fun start() = throw IllegalStateException()

        override fun stop() = throw IllegalStateException()
    }
}

internal interface IReplikeringMediator {
    fun onRecognizedMessage(
        message: ReplikeringsMelding,
        context: MessageContext,
    )

    fun onRiverError(
        riverName: String,
        problems: MessageProblems,
        context: MessageContext,
    )
}
