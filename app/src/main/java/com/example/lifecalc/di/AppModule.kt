package com.example.lifecalc.di
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.lifecalc.Viewmodel.HistoryViewModel
import com.example.lifecalc.Viewmodel.InputViewModel
import com.example.lifecalc.Viewmodel.ResultViewModel
import com.example.lifecalc.Viewmodel.SharedViewModel
import com.example.lifecalc.billing.BillingManager
import com.example.lifecalc.data.db.AppDatabase
import com.example.lifecalc.data.preference.UserPreferences
import com.example.lifecalc.data.repository.CalculationRepository
import com.example.lifecalc.domain.usecase.CalculateLifetimeUseCase
import org.koin.core.module.dsl.viewModelOf

val appModule = module {

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "zeitwert_db"
        ).build()
    }
    single { get<AppDatabase>().calculationDao() }

    single { UserPreferences(androidContext()) }


    // Repositories
    single { CalculationRepository(get()) }

    // Billing
    single { BillingManager(androidContext()) }

    // Use Cases
    factory { CalculateLifetimeUseCase() }

    // ViewModels
    viewModelOf(::InputViewModel)

    viewModelOf(::ResultViewModel)

    viewModelOf(::HistoryViewModel)

    viewModelOf(::SharedViewModel)
}