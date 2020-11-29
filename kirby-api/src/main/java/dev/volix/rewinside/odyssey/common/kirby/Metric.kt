package dev.volix.rewinside.odyssey.common.kirby

/**
 * A metric holds abstract definitions for multiple [Record]s.
 * It e.g. defines what the records observe and in what namespace
 * they exist in.
 *
 * @author Tobias Büser
 * @since 0.1.0
 */
open class Metric @JvmOverloads constructor(
        /**
         * Simple non-space name of the metric, to uniquely
         * identify it. e.g.: `players_online`
         */
        val name: String,

        /**
         * A short description about what the metric tracks.
         */
        val description: String,

        /**
         * Defines in which way the metric tracks its records.
         * e.g. a gauge is a simple one-value metric, which can
         * increase and decrease in value.
         * Notice: Not every [MetricPublisher] supports every metric type.
         * Most of the time a gauge is what you want anyways.
         */
        val type: MetricType = MetricType.GAUGE,

        /**
         * Key of the tags, that can be attached to [Record]s.
         * Used e.g. in Prometheus collector definitions.
         */
        val tagFields: List<String> = mutableListOf(),

        /**
         * Prefix of every [Record] field. Will be used in combination
         * with [name] like `$namespace_$metricName_$recordField`.
         */
        var namespace: String = "",

        /**
         * A [Record] has to be published somehow and if no explicit
         * [MetricPublisher] is being chosen, it will default to this one.
         * Has to be registered at the [KirbyService] though.
         */
        var defaultPublisher: Class<MetricPublisher>? = null
) {

    val namespacedName: String
        get() {
            if(namespace.isEmpty()) return name
            return "${namespace}_$name"
        }

}

/**
 * Specifies how the metric tracks its [Record]s.
 * Notice: Not every type is being supported by every publisher.
 * e.g. Influx only supports gauges.
 */
enum class MetricType {

    /**
     * Only increasing value.
     */
    COUNTER,

    /**
     * Value which can decrease and increase.
     */
    GAUGE,

    /**
     * Stores the observations in statistical buckets. Each bucket
     * represents a specific threshold for values (e.g. 0.25, 0.5, 0.75 for random values
     * between 0 and 1).
     */
    HISTOGRAM,

    /**
     * Does the same thing as [HISTOGRAM], but instead of storing the values
     * inside buckets, a summary automatically stores quantiles.
     */
    SUMMARY

}
