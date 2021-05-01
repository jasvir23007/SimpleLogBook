package com.jasvir.simplelogbook.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class SubNotes(
    val title: String = "",
    val description: String = "",
    val creationDate: Date = Date(),
    val imgPath: ArrayList<String> = ArrayList<String>(),
    val drawingPath:String =""
) : Parcelable