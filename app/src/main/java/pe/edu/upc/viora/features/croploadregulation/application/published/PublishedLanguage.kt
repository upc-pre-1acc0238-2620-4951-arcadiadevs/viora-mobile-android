package pe.edu.upc.viora.features.croploadregulation.application.published

/*
 * The published language of Crop load regulation (thinning): the types its use cases hand to other contexts. Other contexts import them
 * from here, never from this context's domain, so the domain can change behind this list without them noticing.
 */

typealias CampaignPlan = pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan

typealias ThinningPrescription = pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription

typealias ThinningEvent = pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningEvent

typealias ThinningEventType = pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningEventType

typealias SamplingStatus = pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
