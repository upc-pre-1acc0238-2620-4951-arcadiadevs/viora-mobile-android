package pe.edu.upc.viora.features.plotmanagement.domain.service

/** Asks the platform to send the notes not sent yet once the network is available. */
interface FieldNoteSyncScheduler {

    /** Enqueues a sync pass; calling it while one is already queued must not lose the new request. */
    fun schedule()
}
