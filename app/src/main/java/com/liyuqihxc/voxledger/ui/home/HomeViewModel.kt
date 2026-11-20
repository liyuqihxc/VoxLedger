package com.liyuqihxc.voxledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.liyuqihxc.voxledger.data.repository.MonthSummary
import com.liyuqihxc.voxledger.data.repository.TransactionRepository
import com.liyuqihxc.voxledger.di.AppContainer
import com.liyuqihxc.voxledger.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val displayedMonth = MutableStateFlow(YearMonth.now())

    val yearMonth: StateFlow<YearMonth> = displayedMonth.stateIn(
        viewModelScope, SharingStarted.Eagerly,
        displayedMonth.value,
    )

    val summary: StateFlow<MonthSummary> = displayedMonth
        .flatMapLatest { ym ->
            flow {
                emit(repository.summary(ym.atDay(1), ym.atEndOfMonth()))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthSummary(0, 0))

    val transactions: StateFlow<List<Transaction>> = displayedMonth
        .flatMapLatest { ym ->
            repository.observeBetween(ym.atDay(1), ym.atEndOfMonth())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun previousMonth() {
        displayedMonth.value = displayedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        displayedMonth.value = displayedMonth.value.plusMonths(1)
    }

    fun currentMonth(): YearMonth = displayedMonth.value

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(container.transactionRepository) }
        }
    }
}
