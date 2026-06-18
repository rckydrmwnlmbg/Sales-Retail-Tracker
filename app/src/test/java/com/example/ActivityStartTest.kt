package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class ActivityStartTest {
    @Test
    fun testActivityStarts() {
        Robolectric.buildActivity(MainActivity::class.java).setup().get()
    }
}
