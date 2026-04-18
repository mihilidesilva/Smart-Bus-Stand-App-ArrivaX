package com.example.arrivax.repository

import com.example.arrivax.model.Conductor
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ConductorRepository {

    private val db = FirebaseFirestore.getInstance()
    // CONDUCTORS are stored in the 'users' collection with role 'CONDUCTOR'
    private val usersCollection = db.collection("users")

    suspend fun getAllConductors(): List<Conductor> {
        return try {
            // Filter by role to only get conductors from the users collection
            usersCollection.whereEqualTo("role", "CONDUCTOR").get().await().map {
                it.toObject(Conductor::class.java).apply { id = it.id }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteConductor(conductorId: String): Result<Unit> {
        return try {
            usersCollection.document(conductorId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateConductorStatus(conductorId: String, newStatus: String): Result<Unit> {
        return try {
            usersCollection.document(conductorId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateConductor(conductorId: String, updatedData: Map<String, Any>): Result<Unit> {
        return try {
            usersCollection.document(conductorId).update(updatedData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
