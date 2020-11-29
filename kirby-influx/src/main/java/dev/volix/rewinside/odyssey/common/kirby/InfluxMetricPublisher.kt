package dev.volix.rewinside.odyssey.common.kirby

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point

/**
 * @author Tobias Büser
 * @since 0.2.0
 */
class InfluxMetricPublisher(
        val host: String, val port: Int,
        val user: String, val password: String
) : MetricPublisher {

    private lateinit var influxClient: InfluxDBClient

    override fun getKey(): String {
        return "influx"
    }

    override fun initialize() {
        if(this::influxClient.isInitialized) return
        this.influxClient = InfluxDBClientFactory.create("http://$host:$port", user, password.toCharArray())
    }

    override fun publish(record: Record, metric: Metric) {
        val writeApi = influxClient.writeApi

        val point = Point.measurement(metric.namespacedName)
        record.tags.forEach { point.addTag(it.key, it.value) }
        record.fields.forEach { point.addField(it.key, it.value) }
        point.time(record.at, WritePrecision.MS)

        writeApi.writePoint(point)
    }

}
