package com.example.arrivax.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arrivax.model.Conductor
import com.example.arrivax.repository.ConductorRepository
import kotlinx.coroutines.launch

class ConductorListViewModel : ViewModel() {

    private val repository = ConductorRepository()

    private val _conductors = MutableLiveData<List<Conductor>>()
    val conductors: LiveData<List<Conductor>> = _conductors

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchConductors()
    }

    fun fetchConductors() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _conductors.value = repository.getAllConductors()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch conductors: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateConductorStatus(conductorId: String, isActive: Boolean) {
        viewModelScope.launch {
            val newStatus = if (isActive) "ACTIVE" else "INACTIVE"
            val result = repository.updateConductorStatus(conductorId, newStatus)
            result.onSuccess {
                fetchConductors()
            }.onFailure { e ->
                _errorMessage.value = "Failed to update status: ${e.message}"
                fetchConductors()
            }
        }
    }

    // FINAL FIX: Restoring the missing update and delete functions

    fun updateConductor(conductorId: String, updatedData: Map<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true
            // This assumes a general-purpose update function exists in the repository.
            // The previous fix had removed it, so this might need to be re-added there too.
            val result = repository.updateConductor(conductorId, updatedData)
            result.onSuccess {
                fetchConductors()
            }.onFailure { e ->
                _errorMessage.value = "Failed to update conductor: ${e.message}"
            }.also {
                _isLoading.value = false
            }
        }
    }

    fun deleteConductor(conductorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // This assumes a delete function exists in the repository.
            val result = repository.deleteConductor(conductorId)
            result.onSuccess {
                fetchConductors()
            }.onFailure { e ->
                _errorMessage.value = "Failed to delete conductor: ${e.message}"
            }.also {
                _isLoading.value = false
            }
        }
    }
}
