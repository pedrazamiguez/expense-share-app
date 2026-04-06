package es.pedrazamiguez.splittrip.data.firebase.firestore.datasource.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import es.pedrazamiguez.splittrip.data.firebase.firestore.document.GroupDocument
import es.pedrazamiguez.splittrip.data.firebase.firestore.mapper.toDomain
import es.pedrazamiguez.splittrip.domain.model.Group
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Helper that centralizes Firestore group-document loading (cache-first + server fallback).
 *
 * Extracted from [FirestoreGroupDataSourceImpl] to keep function counts
 * within the configured detekt threshold.
 */
class FirestoreGroupLoader(private val firestore: FirebaseFirestore) {

    suspend fun loadGroupsFromCache(groupIds: List<String>): List<Group> {
        val groups = groupIds.mapNotNull { groupId ->
            try {
                @Suppress("kotlin:S6518")
                val cachedDoc = firestore
                    .collection(GroupDocument.COLLECTION_PATH)
                    .document(groupId)
                    .get(Source.CACHE)
                    .await()

                if (cachedDoc.exists()) {
                    cachedDoc.toObject(GroupDocument::class.java)?.toDomain()
                } else {
                    null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                Timber.d("Cache miss for group $groupId")
                null
            }
        }

        // Members are already included via toDomain() from denormalized memberIds
        return groups.sortedByDescending { it.lastUpdatedAt }
    }

    suspend fun loadSingleGroupFromCache(groupId: String): Group? = try {
        @Suppress("kotlin:S6518")
        val cachedDoc = firestore
            .collection(GroupDocument.COLLECTION_PATH)
            .document(groupId)
            .get(Source.CACHE)
            .await()

        if (cachedDoc.exists()) {
            cachedDoc
                .toObject(GroupDocument::class.java)
                ?.toDomain()
        } else {
            null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        Timber.d("Cache miss for group $groupId")
        null
    }

    suspend fun loadGroupsFromServer(groupIds: List<String>): List<Group> = coroutineScope {
        groupIds.map { groupId ->
            async {
                try {
                    val groupDoc = firestore
                        .collection(GroupDocument.COLLECTION_PATH)
                        .document(groupId)
                        .get()
                        .await()

                    if (groupDoc.exists()) {
                        groupDoc
                            .toObject(GroupDocument::class.java)
                            ?.toDomain()
                    } else {
                        null
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(
                        e,
                        "Error fetching group $groupId from server"
                    )
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }
}
