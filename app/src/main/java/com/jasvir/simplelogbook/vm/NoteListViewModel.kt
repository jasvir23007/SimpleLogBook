package com.jasvir.simplelogbook.vm

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.StorageReference
import com.jasvir.simplelogbook.model.NoteModel
import com.jasvir.simplelogbook.repository.NotesRepository
import com.jasvir.simplelogbook.uitls.Constants.ALL_NOTE
import com.jasvir.simplelogbook.uitls.Constants.PERSONAL_NOTE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class NoteListViewModel @ViewModelInject constructor(
    private val mNotesRepository: NotesRepository
) : ViewModel() {

    private val _allNotes = MutableLiveData<List<NoteModel>>()
    val allNotes: LiveData<List<NoteModel>>
        get() = _allNotes

    init {
        loadNoteAllNote()
    }

    @ExperimentalCoroutinesApi
    fun loadNoteAllNote() {
        viewModelScope.launch(Dispatchers.IO) {
            mNotesRepository.getData().collect {
                _allNotes.postValue(it)
            }

        }
    }

    fun filterNote(query: String): List<NoteModel> {
        return _allNotes.value?.let { noteList ->
            noteList.filter { note ->
                note.title.equals(query, true)
            }
        } ?: listOf()

    }

    @ExperimentalCoroutinesApi
    fun updateNote(
        noteId: Int, noteData: Map<String, Any>,
        doOnSuccess: () -> Unit,
        doOnFailure: () -> Unit
    ) {
        viewModelScope.launch {
            mNotesRepository.updateNote(noteId, noteData, doOnSuccess, doOnFailure).collect()
        }
    }

    @ExperimentalCoroutinesApi
    fun saveNote(note: NoteModel, doOnSuccess: () -> Unit, doOnFailure: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            mNotesRepository.saveNote(note, doOnSuccess, doOnFailure).collect()
        }
    }

    @ExperimentalCoroutinesApi
    fun deleteNote(note: NoteModel, doOnSuccess: () -> Unit, doOnFailure: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            mNotesRepository.deleteNote(note, doOnSuccess, doOnFailure).collect()
        }
    }

    @ExperimentalCoroutinesApi
    fun uploadImage(mPath: Uri?, _listenImage: MutableLiveData<String>) {
        viewModelScope.launch {
            mNotesRepository.uploadImage(mPath).collect {
                _listenImage.postValue(it)
            }
        }
    }

    fun setFirstInstall() {
        mNotesRepository.setIsFirstInstall()
    }

    fun getFirstInstall():Boolean {
       return mNotesRepository.getFirstInsall()
    }


}