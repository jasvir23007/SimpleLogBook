package com.jasvir.simplelogbook.di

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jasvir.simplelogbook.repository.NotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
class RepositoryModule {


    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }


    @Provides
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }


    @Provides
    fun provideRepository(
        mAuth: FirebaseAuth,
        db: FirebaseFirestore, docName: String
        , mRef: StorageReference ,mSharedPreferences: SharedPreferences
    ): NotesRepository {
        return NotesRepository(mAuth, db, docName, mRef,mSharedPreferences)
    }

    @Provides
    fun provideFirebaseStorageReference(storage: FirebaseStorage): StorageReference {
        return storage.getReference()
    }

    @Provides
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE)
    }


    @Provides
    fun provideFirebaseFireStore(setting: FirebaseFirestoreSettings): FirebaseFirestore {
        return Firebase.firestore.apply {
            firestoreSettings = setting
        }
    }


    @Provides
    fun provideUniqeId(@ApplicationContext context: Context): String {
        return Settings.Secure.getString(
            context.getContentResolver(),
            Settings.Secure.ANDROID_ID
        )
    }


    @Provides
    fun provideFrebaseFireStoreSettings(): FirebaseFirestoreSettings {
        return firestoreSettings {
            isPersistenceEnabled = true
        }

    }



}