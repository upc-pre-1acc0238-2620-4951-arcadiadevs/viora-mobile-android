package pe.edu.upc.viora.features.subscription.domain.entity

import java.time.Instant

/** A successful empty catalog has a timestamp; a missing cache does not. */
data class ProducerPlanCatalog(val offers: List<ProducerPlanOffer>, val fetchedAt: Instant?)
