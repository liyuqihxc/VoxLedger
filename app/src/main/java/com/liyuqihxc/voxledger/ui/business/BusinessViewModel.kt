package com.liyuqihxc.voxledger.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.liyuqihxc.voxledger.data.repository.TransactionRepository
import com.liyuqihxc.voxledger.di.AppContainer
import com.liyuqihxc.voxledger.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BusinessViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val selectedTrip = MutableStateFlow<String?>(null)

    val trips: StateFlow<List<String>> = repository.observeReimbursable()
        .map { list -> list.mapNotNull { it.tripName?.takeIf { name -> name.isNotBlank() } }.distinct() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedTripName: StateFlow<String?> = selectedTrip.stateIn(
        viewModelScope, SharingStarted.Eagerly, null,
    )

    val transactions: StateFlow<List<Transaction>> = repository.observeReimbursable()
        .map { list -> list.filter { selectedTrip.value == null || it.tripName == selectedTrip.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalCents: StateFlow<Long> = transactions
        .map { list -> list.sumOf { it.amountCents } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun selectTrip(trip: String?) {
        selectedTrip.value = trip
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { BusinessViewModel(container.transactionRepository) }
        }
    }
}
