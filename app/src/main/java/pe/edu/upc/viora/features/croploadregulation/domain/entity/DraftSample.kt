package pe.edu.upc.viora.features.croploadregulation.domain.entity

/**
 * A tree sample recorded on the phone and kept until the server takes it. [batchId] goes with every send of
 * this draft, so a retry of a batch the server already took is a replay it ignores, never a second round.
 * It is null only for drafts saved before version 15 of the database, which get one on their first send.
 */
data class DraftSample(
    val id: String,
    val plotId: String,
    val plotName: String,
    val campaignYear: Int,
    val sample: TreeSample,
    val batchId: String?,
) {
    val treeIdentifier: String get() = sample.treeIdentifier
    val isSynced: Boolean get() = sample.isSynced
}
