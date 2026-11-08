package com.smarttracker.app.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,
    val category: String,
    val merchant: String,
    val accountOrCard: String,
    val balanceAfterTx: Double?,
    val referenceNumber: String?,
    val timestamp: Long,
    val isConfirmed: Boolean = false,
    val rawSmsBody: String? = null
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isConfirmed = 1 ORDER BY timestamp DESC LIMIT 50")
    fun getConfirmedTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET isConfirmed = 1, category = :category WHERE id = :id")
    suspend fun confirmTransaction(id: Long, category: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND isConfirmed = 1")
    fun getTotalSpent(): Flow<Double?>
}

@Database(entities = [TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "detex_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
