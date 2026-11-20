package com.liyuqihxc.voxledger.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.liyuqihxc.voxledger.data.repository.TransactionRepository
import com.liyuqihxc.voxledger.di.AppContainer
import com.liyuqihxc.voxledger.model.Transaction
import com.liyuqihxc.voxledger.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val filter = MutableStateFlow<TransactionType?>(null)

    val filterType: StateFlow<TransactionType?> = filter.stateIn(
        viewModelScope, SharingStarted.Eagerly, null,
    )

    val transactions: StateFlow<List<Transaction>> = combine(
        repository.observeAll(),
        filter,
    ) { list, type -> if (type == null) list else list.filter { it.type == type } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(type: TransactionType?) {
        filter.value = type
    }

    fun delete(tx: Transaction) {
        viewModelScope.launch { repository.delete(tx) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoryViewModel(container.transactionRepository) }
        }
    }
}
