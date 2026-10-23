package pe.edu.upc.viora.features.subscription.infrastructure.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/** One durable purchase journal per account; operation keys are written before any POST. */
@Entity(tableName = "individual_purchase_journal")
data class IndividualPurchaseEntity(
    @PrimaryKey val owner: String,
    val plan: String,
    val quota: String,
    val createKey: String,
    val checkoutKey: String,
    val contractJson: String?,
    val intentId: String?,
    val checkoutUrl: String?,
    val expiresAt: String?,
)
