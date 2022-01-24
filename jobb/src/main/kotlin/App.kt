import no.nav.rapids_and_rivers.cli.ConsumerProducerFactory
import no.nav.rapids_and_rivers.cli.JsonRiver
import no.nav.rapids_and_rivers.cli.OnPremConfig
import no.nav.rapids_and_rivers.cli.RapidsCliApplication
import no.nav.rapids_and_rivers.cli.seekTo
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.LocalDateTime

private val config = OnPremConfig.default // AivenConfig.default
private val factory = ConsumerProducerFactory(config)

fun main() {
    val topics = listOf("my-cool-topic")
    val producer = factory.createProducer()
    RapidsCliApplication(factory).apply {
        // parses every message as json
        JsonRiver(this).apply {
            // listens only on json messages
            val typer = listOf("my_cool_event", "my_other_event")
            validate { _, node, reasons -> node.hasNonNull("@event_name").ifFailed(reasons, "Mangler @event_name") }
            validate { _, node, reasons -> node.path("@event_name").isTextual.ifFailed(reasons, "@event_name er ikke tekstlig") }
            validate { _, node, reasons -> (node.path("@event_name").asText() !in typer).ifFailed(reasons, "${node.path("@event_name").asText()} er ikke forventet type") }
            onMessage { _, node -> println(node.toString()) }
            onError { record, _, reasons ->
                println("Failed to validate because:\n${reasons.joinToString()}")
                producer.send(ProducerRecord("dead-letter-queue", record.key(), record.value()))
            }
        }
        // listens on every "raw" string message
        register(printStatistics())
    }.start("my-cool-consumer", topics) { consumer ->
        // seek to a particular time
        topics.onEach { topic -> consumer.seekTo(topic, LocalDateTime.now().minusHours(1)) }
    }
}

private fun MutableList<String>.failed(why: String) = false.also { this.add(why) }
private fun Boolean.ifFailed(reasons: MutableList<String>, why: String) = if (this) true else reasons.failed(why)

// print a message count for each partition on every message
private fun printStatistics(): (ConsumerRecord<String, String>) -> Unit {
    val messageCounts = mutableMapOf<Int, Long>()
    return fun (record: ConsumerRecord<String, String>) {
        val partition = record.partition()
        messageCounts.increment(partition)
        println(messageCounts.toString(partition))
    }
}

private fun <K> MutableMap<K, Long>.increment(key: K) =
    (getOrDefault(key, 0) + 1).also { this[key] = it }

private fun <K : Comparable<K>> Map<K, Long>.toString(selectedKey: K) =
    map { it.key to it.value }
        .sortedBy(Pair<K, *>::first)
        .joinToString(separator = "  ") { (key, count) ->
            val marker = if (key == selectedKey) "*" else " "
            val countString = count.toString().padStart(5, ' ')
            "[$marker$countString]"
        }
