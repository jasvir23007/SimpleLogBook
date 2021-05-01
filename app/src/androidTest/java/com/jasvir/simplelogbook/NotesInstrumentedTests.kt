package com.jasvir.simplelogbook

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NotesInstrumentedTests {

    @get:Rule
    var mainActivityTestRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun test_recycler_view_displayed() {
        onView(
            Matchers.allOf(
                isDisplayed(),
                withId(R.id.rv_note)
            )
        )
    }

    @Test
    fun test_add_button_click() {
        onView(
            withId(R.id.fab_new)
        ).perform(ViewActions.click())

        onView(Matchers.allOf(isDisplayed(), withId(R.id.noteAddFragment)))
    }



    @Test
    fun test_add_item_to_list() = runBlocking<Unit> {
        onView(
            withId(R.id.fab_new)
        ).perform(ViewActions.click())

        onView(Matchers.allOf(isDisplayed(), withId(R.id.noteAddFragment)))

        onView(Matchers.allOf(isDisplayed(), withId(R.id.et_title)))
            .perform(
                ViewActions.click(),
                ViewActions.typeText("test"),
                ViewActions.closeSoftKeyboard()
            )

        onView(
            withId(R.id.img_save)
        ).perform(ViewActions.click())


        var activity: Activity? = null

        mainActivityTestRule.scenario.onActivity {
            activity = it
        }

        val recyclerView: RecyclerView = activity!!.findViewById(R.id.rv_note)
        onView(Matchers.allOf(isDisplayed(), withId(R.id.rv_note)))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(recyclerView.adapter!!.itemCount - 1))

    }









    @Test
    fun test_on_list_itemclick(){
        onView(Matchers.allOf(isDisplayed(), withId(R.id.rv_note)))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

    }



    @Test
    fun test_sub_list_item_add(){
        onView(Matchers.allOf(isDisplayed(), withId(R.id.rv_note)))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        onView(
            withId(R.id.fab_new)
        ).perform(ViewActions.click())

        onView(Matchers.allOf(isDisplayed(), withId(R.id.noteAddFragment)))

        onView(Matchers.allOf(isDisplayed(), withId(R.id.et_title)))
            .perform(
                ViewActions.click(),
                ViewActions.typeText("test"),
                ViewActions.closeSoftKeyboard()
            )


        onView(Matchers.allOf(isDisplayed(), withId(R.id.et_note)))
            .perform(
                ViewActions.click(),
                ViewActions.typeText("test"),
                ViewActions.closeSoftKeyboard()
            )



        onView(
            withId(R.id.img_save)
        ).perform(ViewActions.click())


        var activity: Activity? = null

        mainActivityTestRule.scenario.onActivity {
            activity = it
        }

        Thread.sleep(500)
        val recyclerView: RecyclerView = activity!!.findViewById(R.id.rv_note_sublist)
        onView(Matchers.allOf(isDisplayed(), withId(R.id.rv_note_sublist)))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(recyclerView.adapter!!.itemCount - 1))




    }



    @Test
   fun test_delete_item_from_list() = runBlocking<Unit>{
        onView(withId(R.id.rv_note)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0,
                InstrumentalTestUtils.ClickOnView(R.id.iv_move)
            ));

        onView(withText(R.string.delete))
        .inRoot(isDialog())
            .perform(click())

    }






}