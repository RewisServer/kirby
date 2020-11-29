package dev.volix.rewinside.odyssey.common.kirby

import io.prometheus.client.Collector
import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.Histogram
import io.prometheus.client.Summary

fun Metric.toPrometheusCollector(name: String): Collector? {
    val builder = when (this.type) {
        MetricType.COUNTER -> Counter.build()
        MetricType.GAUGE -> Gauge.build()
        MetricType.HISTOGRAM -> Histogram.build()
        MetricType.SUMMARY -> Summary.build()
    }

    return builder.name(name)
            .help(this.description)
            .labelNames(*this.tagFields.toTypedArray()).create()
}

fun Collector.record(tagValues: Array<String>, value: Double) {
    when (this) {
        is Counter -> this.labels(*tagValues).inc(value)
        is Gauge -> this.labels(*tagValues).set(value)
        is Histogram -> this.labels(*tagValues).observe(value)
        is Summary -> this.labels(*tagValues).observe(value)
    }
}

fun Record.fanOut(): List<Record> {
    if (this.fields.isEmpty()) return listOf()
    if (this.fields.size == 1) return listOf(this)

    return this.fields.toList().map { (name, record) ->
        Record(at, tags, mapOf(name to record))
    }
}

fun Record.getTagValuesAsArray() = this.tags.values.toTypedArray()

fun Record.firstEntry() = if (this.fields.isEmpty()) null else this.fields.entries.first()
