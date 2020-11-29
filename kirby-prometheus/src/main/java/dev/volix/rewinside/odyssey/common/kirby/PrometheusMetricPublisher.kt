package dev.volix.rewinside.odyssey.common.kirby

import io.prometheus.client.Collector
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.BasicAuthHttpConnectionFactory
import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.exporter.PushGateway
import java.net.InetSocketAddress

/**
 * @author Tobias Büser
 * @since 0.1.0
 */
class PrometheusMetricPublisher(
        val host: String, val port: Int,
        val user: String, val password: String,
        val publishingType: PublishingType,
        val jobName: String
) : MetricPublisher {

    private val metricToCollectorMap = mutableMapOf<String, Collector>()

    private lateinit var gateway: PushGateway
    private lateinit var registry: CollectorRegistry

    private var servesHttp = false
    private var httpServer: HTTPServer? = null

    override fun getKey(): String {
        return "prometheus"
    }

    override fun initialize() {
        if (this::registry.isInitialized) return
        this.registry = CollectorRegistry()

        if (this.publishingType.isPushgateway()) {
            this.gateway = PushGateway("$host:$port")
            gateway.setConnectionFactory(BasicAuthHttpConnectionFactory(user, password))
        }
    }

    override fun publish(record: Record, metric: Metric) {
        // we create a seperate push registry, as we only
        // want to push single records if the publishing
        // type is PUSHGATEWAY.
        val pushRegistry = CollectorRegistry()

        record.fanOut().forEach { subRecord ->
            val entry = subRecord.firstEntry()!!
            val name = if (entry.key.isEmpty()) metric.namespacedName else "${metric.namespacedName}_${entry.key}"

            var collector = metricToCollectorMap[name]
            if (collector == null) {
                collector = metric.toPrometheusCollector(name)
                this.metricToCollectorMap[name] = collector!!

                if (this.publishingType.isHttpServer()) {
                    this.registry.register(collector)
                } else {
                    pushRegistry.register(collector)
                }
            }
            collector.record(subRecord.getTagValuesAsArray(), entry.value)
        }

        if (this.publishingType.isPushgateway()) {
            this.gateway.pushAdd(pushRegistry, jobName)
        }
    }

    fun serveHttp(port: Int) {
        if (!this.publishingType.isHttpServer() || httpServer != null) return

        // automatically starts the server. weird, I know
        this.httpServer = HTTPServer(InetSocketAddress(port), this.registry, true)
        this.servesHttp = true
    }

    enum class PublishingType {

        HTTP_SERVER,
        PUSHGATEWAY,
        BOTH;

        fun isHttpServer(): Boolean {
            return this == HTTP_SERVER || this == BOTH
        }

        fun isPushgateway(): Boolean {
            return this == PUSHGATEWAY || this == BOTH
        }

        companion object {

            fun fromName(name: String): PublishingType? {
                return values().firstOrNull { it.name.equals(name, ignoreCase = true) }
            }

        }

    }

}
