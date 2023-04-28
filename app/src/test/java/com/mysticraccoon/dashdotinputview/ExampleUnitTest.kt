package com.mysticraccoon.dashdotinputview

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        var testMorseText = "//.../---/..."
        var alphaText = translateMorseToAlpha(testMorseText)
        assertEquals("SOS", alphaText)
        testMorseText = "/.../---/...|.-/-.../-.-."
        alphaText = translateMorseToAlpha(testMorseText)
        assertEquals("SOS ABC", alphaText)
    }
}