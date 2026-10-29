package pe.edu.upc.viora.features.croploadregulation.domain.service

/** Asks the platform to send the tree samples not sent yet once the network is available. */
interface SamplingSyncScheduler {

    /** Enqueues a sync pass; calling it while one is already queued must not lose the new request. */
    fun schedule()
}
