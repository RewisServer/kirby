package dev.volix.rewinside.odyssey.common.kirby

/**
 * Represents a single observation at one specific point in time.
 *
 * @author Tobias Büser
 * @since 0.1.0
 */
class Record(
        /**
         * Defines at which time this record has been observed.
         * Milliseconds since Unix epoch.
         */
        val at: Long,

        /**
         *  Tags that are attached to the record as additional information.
         *  e.g.: `server`=``lobby-1`.
         */
        val tags: Map<String, String> = mapOf(),

        /**
         * The specific values regarding the observation.
         * If multiple fields are set and the underlying
         * publisher does not support it, we have
         * to fan out the fields to get one field per record.
         */
        val fields: Map<String, Double> = mapOf()
) {

    class Builder(
            /**
             * The metric to add the record to.
             */
            private val metric: Metric,

            /**
             * Service from which we publish the [Record] from.
             */
            private val service: KirbyService
    ) {

        private var at: Long = 0
        private val tags = mutableMapOf<String, String>()
        private val fields = mutableMapOf<String, Double>()

        /**
         * Specifies [at] which time this record has been observed.
         */
        fun at(at: Long): Builder {
            this.at = at
            return this
        }

        /**
         * Tags the observation with [key] and [value].
         * Example usage: `.tag("server", "lobby-1")`
         */
        fun tag(key: String, value: String): Builder {
            this.tags[key] = value
            return this
        }

        /**
         * Adds a specific observed double field [value] to the record
         * by key [key].
         */
        fun field(key: String, value: Double): Builder {
            this.fields[key] = value;
            return this
        }

        /**
         * Adds a specific observed int field [value] to the record
         * by key [key].
         */
        fun field(key: String, value: Int): Builder {
            return this.field(key, value.toDouble())
        }

        /**
         * Default of [field] with empty key.
         */
        fun value(value: Double): Builder {
            return this.field("", value)
        }

        /**
         * Default of [field] with empty key.
         */
        fun value(value: Int): Builder {
            return this.value(value.toDouble())
        }

        fun build(): Record {
            if(at == 0L) at = System.currentTimeMillis()
            return Record(at, tags, fields)
        }

        /**
         * Publishes the record to a specified [MetricPublisher], either defined
         * in the [Metric] or as default in the [KirbyService].
         */
        fun publish() {
            service.schedule { service, _ -> service.publish(build(), metric) }
        }

        /**
         * Same as [publish] but synchronous, i.e. potentially blocking.
         */
        fun publishSync() {
            service.publish(build(), metric)
        }

    }

}
