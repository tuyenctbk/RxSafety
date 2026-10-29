package com.example.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.donation.DonationManager
import com.example.data.repository.MedicationRepository
import com.example.ui.viewmodel.RxSafetyViewModel

class RxSafetyViewModelFactory(
    private val repository: MedicationRepository,
    private val donationManager: DonationManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RxSafetyViewModel::class.java)) {
            return RxSafetyViewModel(repository, donationManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
