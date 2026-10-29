package pe.edu.upc.viora.features.harvestsettlement.application.published

/*
 * The published language of Harvest settlement: the types its use cases hand to other contexts. Other contexts import them
 * from here, never from this context's domain, so the domain can change behind this list without them noticing.
 */

typealias HarvestSettlement = pe.edu.upc.viora.features.harvestsettlement.domain.entity.HarvestSettlement

typealias SettlementStatus = pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementStatus

typealias PlotToSettle = pe.edu.upc.viora.features.harvestsettlement.domain.PlotToSettle
