package pe.edu.upc.viora.features.plotmanagement.application.published

/*
 * The published language of Plot management: the types its use cases hand to other contexts. Other contexts import them
 * from here, never from this context's domain, so the domain can change behind this list without them noticing.
 */

typealias Plot = pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

typealias FieldNote = pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote

typealias FieldNoteSyncState = pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncState

typealias FieldNoteType = pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
