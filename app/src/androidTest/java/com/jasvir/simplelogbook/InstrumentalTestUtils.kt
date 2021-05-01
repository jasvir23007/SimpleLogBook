package com.jasvir.simplelogbook

import android.R.id
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import org.hamcrest.Matcher


class InstrumentalTestUtils {

    class ClickOnView(val id: Int) : ViewAction {
        internal var click = ViewActions.click()

        override fun getConstraints(): Matcher<View> {
            return click.constraints
        }

        override fun getDescription(): String {
            return " click on custom button view"
        }

        override fun perform(uiController: UiController, view: View) {
            val v: View = view.findViewById(id)
            v.performClick()
        }
    }


}