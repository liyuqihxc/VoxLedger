package com.liyuqihxc.voxledger.data.local

import androidx.room.TypeConverter
import com.liyuqihxc.voxledger.model.TransactionType

class Converters {
    @TypeConverter
    fun transactionTypeToString(type: TransactionType): String = type.name

    @TypeConverter
    fun stringToTransactionType(value: String): TransactionType =
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.EXPENSE)
}
