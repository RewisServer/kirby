package dev.volix.rewinside.odyssey.common.kirby

import java.util.concurrent.CompletableFuture

/**
 * Represents some type of publisher (e.g. Prometheus, Influx, ...) which takes
 * metrics and its records and publishes them to the outside, i.e. stores them
 * in some way to an external server.
 *
 * @author Tobias Büser
 * @since 0.1.0
 */
interface MetricPublisher {

    /**
     * Unique idenfitier for this publisher.
     * Can be used to register it at a [KirbyService].
     */
    fun getKey(): String

    /**
     * Should be used to do initializing actions like
     * connecting to an external server or starting up one.
     */
    fun initialize()

    /**
     * Publishes a [record] with specifications of its [metric] to
     * this publisher.
     */
    fun publish(record: Record, metric: Metric)

}
