package com.liyuqihxc.voxledger.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.liyuqihxc.voxledger.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(txs: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(tx: TransactionEntity)

    @Delete
    suspend fun delete(tx: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAtEpochMilli DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE dateEpochDay BETWEEN :startDay AND :endDay " +
            "ORDER BY dateEpochDay DESC, createdAtEpochMilli DESC"
    )
    fun observeBetween(startDay: Long, endDay: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE reimbursable = 1 ORDER BY dateEpochDay DESC")
    fun observeReimbursable(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE tripName = :tripName ORDER BY dateEpochDay ASC")
    fun observeByTrip(tripName: String): Flow<List<TransactionEntity>>

    @Query("SELECT DISTINCT tripName FROM transactions WHERE tripName IS NOT NULL AND tripName != ''")
    suspend fun getTripNames(): List<String>

    @Query(
        "SELECT COALESCE(SUM(amountCents), 0) FROM transactions " +
            "WHERE type = :type AND dateEpochDay BETWEEN :startDay AND :endDay"
    )
    suspend fun sumByType(type: TransactionType, startDay: Long, endDay: Long): Long
}

data class CategorySum(
    val category: String,
    val totalCents: Long,
)
