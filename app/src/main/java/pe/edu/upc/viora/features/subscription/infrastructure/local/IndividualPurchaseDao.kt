package pe.edu.upc.viora.features.subscription.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface IndividualPurchaseDao {
    @Query("SELECT * FROM individual_purchase_journal WHERE owner = :owner")
    suspend fun find(owner: String): IndividualPurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: IndividualPurchaseEntity)

    /** Never recreate a journal removed by an account switch while a request was in flight. */
    @Update
    suspend fun update(row: IndividualPurchaseEntity): Int

    /** Closes the journal of a purchase already paid, so a renewal can open its own. */
    @Query("DELETE FROM individual_purchase_journal WHERE owner = :owner")
    suspend fun delete(owner: String)

    /** Signing out (or another account signing in): the whole table goes, see the context's tables wiper. */
    @Query("DELETE FROM individual_purchase_journal")
    suspend fun wipeAll()
}
