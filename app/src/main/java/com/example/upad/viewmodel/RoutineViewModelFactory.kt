package com.example.upad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.upad.data.FirebaseRepository
import com.example.upad.data.DataStoreManager
import com.example.upad.data.LanguageDataStore
import com.google.firebase.FirebaseApp

class RoutineViewModelFactory(private val repository: FirebaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
            val context = FirebaseApp.getInstance().applicationContext
            val dataStoreManager = DataStoreManager(context)
            val languageDataStore = LanguageDataStore(context)

            @Suppress("UNCHECKED_CAST")
            return RoutineViewModel(repository, dataStoreManager, languageDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}