package pe.edu.upc.viora.features.phenology.application.published

/*
 * The published language of Phenology: the types its use cases hand to other contexts. Other contexts import them
 * from here, never from this context's domain, so the domain can change behind this list without them noticing.
 */

typealias ChillTracker = pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker

typealias WinterSeasonState = pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState

typealias HarvestRecord = pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord

typealias BearingYear = pe.edu.upc.viora.features.phenology.domain.entity.BearingYear

typealias BbiClass = pe.edu.upc.viora.features.phenology.domain.entity.BbiClass

typealias PhaseCalendar = pe.edu.upc.viora.features.phenology.domain.PhaseCalendar

typealias OlivePhase = pe.edu.upc.viora.features.phenology.domain.OlivePhase

typealias PhaseTiming = pe.edu.upc.viora.features.phenology.domain.PhaseTiming

typealias PhaseWindow = pe.edu.upc.viora.features.phenology.domain.PhaseWindow

typealias CurrentPhase = pe.edu.upc.viora.features.phenology.domain.CurrentPhase

typealias PlotPhaseDates = pe.edu.upc.viora.features.phenology.domain.PlotPhaseDates
