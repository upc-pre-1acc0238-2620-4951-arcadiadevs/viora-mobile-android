package pe.edu.upc.viora.features.harvestsettlement.domain.service

/** Asks the platform to send the pending settlements once the network is available. */
interface SettlementSyncScheduler {

    /** Enqueues a sync pass; calling it while one is already queued must not lose the new request. */
    fun schedule()
}
