package com.example.saferhouseui.data.local

import androidx.room.*
import com.example.saferhouseui.data.model.PairingCode
import kotlinx.coroutines.flow.Flow

@Dao
interface PairingCodeDao {
    @Query("SELECT * FROM pairing_codes WHERE code = :code")
    suspend fun getPairingCode(code: String): PairingCode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPairingCode(pairingCode: PairingCode)

    @Query("DELETE FROM pairing_codes WHERE code = :code")
    suspend fun deletePairingCode(code: String)
}
