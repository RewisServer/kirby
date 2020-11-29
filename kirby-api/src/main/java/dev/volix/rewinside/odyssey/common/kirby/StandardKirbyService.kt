package dev.volix.rewinside.odyssey.common.kirby

import java.util.concurrent.Executors

/**
 * @author Tobias Büser
 * @since 0.1.0
 */
open class StandardKirbyService(
        private val namespace: String
) : KirbyService {

    private val threadPool = Executors.newSingleThreadExecutor { r ->
        val thread = Executors.defaultThreadFactory().newThread(r)
        thread.isDaemon = true
        thread
    }

    private val metricRegistry = mutableMapOf<String, Metric>()
    private val publisherRegistry = mutableMapOf<String, MetricPublisher>()

    override fun getNamespace(): String {
        return namespace
    }

    override fun registerMetric(metric: Metric) {
        if (metric.namespace.isEmpty()) metric.namespace = this.namespace
        val name = "${metric.namespace}_${metric.name}"

        require(!metricRegistry.containsKey(name)) { "This metric has already been registered!" }

        metricRegistry[name] = metric
    }

    override fun registerPublisher(publisher: MetricPublisher) {
        require(!publisherRegistry.containsKey(publisher.getKey())) { "This publisher has already been registered!" }

        publisher.initialize()
        publisherRegistry[publisher.getKey()] = publisher
    }

    override fun publish(record: Record, metric: Metric, publisherKey: String) {
        val publisher = publisherRegistry[publisherKey]
        require(publisher != null) { "This publisher does not exist. ($publisherKey)" }

        publisher.publish(record, metric)
    }

    override fun publish(record: Record, metric: Metric, publisherClass: Class<out MetricPublisher>) {
        val publisher = publisherRegistry.values.first { it.javaClass == publisherClass }

        publisher.publish(record, metric)
    }

    override fun publish(record: Record, metric: Metric) {
        val publisher =
                if (metric.defaultPublisher == null) publisherRegistry.values.first()
                else publisherRegistry.values.first { it.javaClass == metric.defaultPublisher }
        publisher.publish(record, metric)
    }

    override fun record(metricName: String): Record.Builder {
        val name = if (!metricName.startsWith(this.namespace)) "${namespace}_${metricName}" else metricName

        require(metricRegistry.containsKey(name)) { "Metric with name $metricName does not exist!" }
        val metric = metricRegistry[name]!!
        return Record.Builder(metric, this)
    }

    override fun record(metricClass: Class<out Metric>): Record.Builder {
        val metric = metricRegistry.values.first { it.javaClass == metricClass }
        return Record.Builder(metric, this)
    }

    override fun schedule(task: ServiceTask) {
        val context = ServiceTaskContext()
        this.threadPool.execute {
            task.run(this, context)
        }
    }

    override fun schedule(task: ServiceTask, delay: Long) {
        val context = ServiceTaskContext(delay = delay)
        this.threadPool.execute {
            if(delay > 0) Thread.sleep(delay)
            task.run(this, context)
        }
    }

    override fun schedule(task: ServiceTask, delay: Long, period: Long) {
        throw UnsupportedOperationException("Periodically executed tasks are not supported in the standard implementation.")
    }

    override fun sex(): Boolean {
        return true
    }

}
