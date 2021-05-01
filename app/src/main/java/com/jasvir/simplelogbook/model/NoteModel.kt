package com.jasvir.simplelogbook.model

import android.os.Parcelable
import com.jasvir.simplelogbook.uitls.getRandomNumber
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class NoteModel(
    val id : Int = getRandomNumber(),
    val title : String = "",
    val note : String = "",
    val color : String = "#ffffff",
    val creationDate : Date = Date(),
    val modifiedDate : Date = Date(),val subnotesList: ArrayList<SubNotes> = ArrayList()
) : Parcelable{

}


