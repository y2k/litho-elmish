package y2k.litho.elmish

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import y2k.litho.elmish.examples.Functions

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun test() = runBlocking {
        val context = InstrumentationRegistry.getTargetContext()
        assertEquals(listOf("..."), Functions.findExamples(context))
    }
}