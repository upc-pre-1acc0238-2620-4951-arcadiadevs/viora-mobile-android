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
}
