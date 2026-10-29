package pe.edu.upc.viora.features.subscription.infrastructure.local

import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper

/** The tables of this context, its plan catalog and the purchase journal: emptied when the account leaves the phone. */
class SubscriptionTablesWiper @Inject constructor(
    private val producerPlanDao: ProducerPlanDao,
    private val individualPurchaseDao: IndividualPurchaseDao,
) : AccountDataWiper {
    override suspend fun wipe() {
        producerPlanDao.wipeAll()
        individualPurchaseDao.wipeAll()
    }
}
