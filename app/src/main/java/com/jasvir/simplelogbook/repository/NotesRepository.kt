package com.jasvir.simplelogbook.repository

import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.jasvir.simplelogbook.model.NoteModel
import com.jasvir.simplelogbook.uitls.Constants.ALL_NOTE
import com.jasvir.simplelogbook.uitls.Constants.PERSONAL_NOTE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val mAuth: FirebaseAuth,
    private val db: FirebaseFirestore, private val docName: String
    , private val mRef: StorageReference, val mPrefrences: SharedPreferences
) {
    @ExperimentalCoroutinesApi
    suspend fun getData(): Flow<MutableList<NoteModel>> {
        var mDocName = docName
        if (mAuth.currentUser != null) {
            mDocName = mAuth.currentUser!!.uid
        }

        return callbackFlow {
            val listenerRegistration = db.collection(ALL_NOTE).document(mDocName)
                .collection(PERSONAL_NOTE)
                .orderBy("creationDate", Query.Direction.DESCENDING)
                .addSnapshotListener { value, exeption ->
                    val tempNoteList = mutableListOf<NoteModel>()
                    value?.let {
                        for (doc in it.iterator()) {
                            val note = doc.toObject(NoteModel::class.java)
                            tempNoteList.add(note)
                        }
                    }
                    offer(tempNoteList)
                }

            awaitClose {
                listenerRegistration.remove()
            }

        }
    }

    @ExperimentalCoroutinesApi
    suspend fun updateNote(
        noteId: Int, noteData: Map<String, Any>,
        doOnSuccess: () -> Unit,
        doOnFailure: () -> Unit
    ): Flow<Unit> {

        var mDocName = docName
        if (mAuth.currentUser != null) {
            mDocName = mAuth.currentUser!!.uid
        }

        return callbackFlow {
            val listenerUpdate = db.collection(ALL_NOTE)
                .document(mDocName)
                .collection(PERSONAL_NOTE)
                .document("$noteId")
                .update(noteData)
                .addOnSuccessListener {
                    offer(doOnSuccess.invoke())
                }
                .addOnFailureListener {
                    offer(doOnFailure.invoke())
                }
            awaitClose {
                // listenerUpdate.result
            }
        }

    }


    @ExperimentalCoroutinesApi
    suspend fun saveNote(
        note: NoteModel,
        doOnSuccess: () -> Unit,
        doOnFailure: () -> Unit
    ): Flow<Unit> {

        var mDocName = docName
        if (mAuth.currentUser != null) {
            mDocName = mAuth.currentUser!!.uid
        }
        return callbackFlow {
            val listnerSave = db.collection(ALL_NOTE)
                .document(mDocName)
                .collection(PERSONAL_NOTE)
                .document("${note.id}")
                .set(note)
                .addOnSuccessListener {
                    offer(doOnSuccess.invoke())
                }
                .addOnFailureListener {
                    offer(doOnFailure.invoke())
                }
            awaitClose {

            }
        }

    }


    @ExperimentalCoroutinesApi
    suspend fun deleteNote(
        note: NoteModel,
        doOnSuccess: () -> Unit,
        doOnFailure: () -> Unit
    ): Flow<Unit> {
        var mDocName = docName
        if (mAuth.currentUser != null) {
            mDocName = mAuth.currentUser!!.uid
        }
        return callbackFlow {
            db.collection(ALL_NOTE)
                .document(mDocName)
                .collection(PERSONAL_NOTE)
                .document("${note.id}")
                .delete()
                .addOnSuccessListener {
                    offer(doOnSuccess.invoke())
                }
                .addOnFailureListener {
                    offer(doOnFailure.invoke())
                }
            awaitClose {

            }
        }
    }


    @ExperimentalCoroutinesApi
    suspend fun uploadImage(mPath: Uri?): Flow<String> {
        return callbackFlow {
            val listener = mRef.child(System.currentTimeMillis().toString()).putFile(mPath!!)
                .addOnCompleteListener {
                    if (it.isSuccessful()) {
                        val uri = it.result
                        val result: Task<Uri> =
                            uri?.storage!!.downloadUrl
                        result.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            offer(imageUrl)
                        }
                    }


                }
            awaitClose {
            }

        }


    }


    fun setIsFirstInstall() {
        mPrefrences.edit().putBoolean("install", false).apply()
    }

    fun getFirstInsall(): Boolean {
        return mPrefrences.getBoolean("install", true)
    }


}





