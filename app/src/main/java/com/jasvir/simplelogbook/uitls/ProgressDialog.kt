package com.jasvir.simplelogbook.uitls

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.jasvir.simplelogbook.R

class ProgressDialog {
    companion object {

        var dialog: Dialog? = null
        fun showProgress(context: Context) {
             dialog = progressDialog(context)
            dialog?.show()
        }

        fun hideProgress() {
            dialog?.dismiss()
        }

       private fun progressDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            dialog.setContentView(inflate)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
            return dialog
        }
    }
}
