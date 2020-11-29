package dev.volix.rewinside.odyssey.common.kirby

/**
 * A normal [Runnable]-like functional interface with the
 * purpose to serve additional data for a task that handles with
 * metrics.
 *
 * @author Tobias Büser
 * @since 0.1.0
 */
fun interface ServiceTask {

    fun run(service: KirbyService, context: ServiceTaskContext)

}

data class ServiceTaskContext(
        val delay: Long = 0,
        val period: Long = -1,
        val scheduledAt: Long = System.currentTimeMillis(),
        var cancelled: Boolean = false
)
