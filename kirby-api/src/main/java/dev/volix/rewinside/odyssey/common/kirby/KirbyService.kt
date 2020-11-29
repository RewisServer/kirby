package dev.volix.rewinside.odyssey.common.kirby

import dev.volix.lib.grape.Service

/**
 * Service to expose to other systems, so that they can
 * use the different functionalities without knowing the
 * concrete implementation.
 *
 * Implements the grape [Service] to be able to register this service
 * with the Grape service provider system.
 *
 * @author Tobias Büser
 * @since 0.1.0
 */
interface KirbyService : Service {

    /**
     * Namespace in which the metrics will be tracked.
     * Is used to append a prefix to every metric and its records.
     */
    fun getNamespace(): String

    /**
     * Adds [metric] to an internal registry, from which the service
     * accesses it when publishing a record.
     *
     * If a metric is not registered and a record still tries to
     * reference it, it will cause an exception. Reason for that is
     * that the metric holds important information for publishing the
     * record and can not be left out.
     */
    fun registerMetric(metric: Metric)

    /**
     * Puts [publisher] into an internal registry. The service
     * will access it when publishing a record.
     *
     * If no publisher is registered, it will 100% cause an exception
     * when referring to one.
     * Otherwise either the best publisher or the only publisher
     * will be chosen.
     */
    fun registerPublisher(publisher: MetricPublisher)

    /**
     * Publishes the [record] of [metric] to a specific [MetricPublisher],
     * either specified in the [Metric] or in this service.
     *
     * @see MetricPublisher.publish
     */
    fun publish(record: Record, metric: Metric)

    /**
     * Same as [publish] but with specifying which publisher should be used,
     * namely the publisher with key [publisherKey].
     *
     * @see MetricPublisher.getKey
     */
    fun publish(record: Record, metric: Metric, publisherKey: String)

    /**
     * Same as [publish] but with specifying which publisher should be used,
     * namely the publisher with class [publisherClass].
     *
     * @see MetricPublisher.javaClass
     */
    fun publish(record: Record, metric: Metric, publisherClass: Class<out MetricPublisher>)

    /**
     * Returns a new [Record.Builder], to create a [Record] from and
     * eventually publishes it via [publish].
     *
     * [metricName] corresponds to [Metric.name].
     */
    fun record(metricName: String): Record.Builder

    /**
     * Same as [record] but with referring to the [Metric]
     * via its [metricClass].
     */
    fun record(metricClass: Class<out Metric>): Record.Builder

    /**
     * Executes given [task] asynchronously. Instead of a simple [Runnable]
     * we take a [ServiceTask] as parameter, as it enables the function
     * to take in additional information about the task itself. (e.g. the service,
     * the scheduling time, etc.)
     */
    fun schedule(task: ServiceTask)

    /**
     * Same as [schedule] but with specifying a [delay] in ms before
     * executing the [task].
     */
    fun schedule(task: ServiceTask, delay: Long)

    /**
     * Same as [schedule] but with specifying a [delay] in ms before
     * executing the [task].
     *
     * Also schedules the task to be executed periodically with [period] in ms.
     */
    fun schedule(task: ServiceTask, delay: Long, period: Long)

    /**
     * ;)
     */
    fun sex(): Boolean

}
