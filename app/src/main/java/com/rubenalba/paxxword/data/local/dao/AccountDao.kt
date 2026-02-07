package com.rubenalba.paxxword.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rubenalba.paxxword.data.local.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    // REPLACE: If you save an account with an existing ID, overwrite it. (useful for editing accounts)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts ORDER BY service_name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE folder_id = :folderId")
    fun getAccountsForFolder(folderId: Long): Flow<List<Account>>

    // To view accounts that are not in any folder
    @Query("SELECT * FROM accounts WHERE folder_id IS NULL")
    fun getAccountsWithoutFolder(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Long): Account?
}