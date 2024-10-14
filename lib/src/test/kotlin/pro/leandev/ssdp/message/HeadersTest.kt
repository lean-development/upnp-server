package pro.leandev.ssdp.message

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class HeadersTest {
    @Test
    fun testParse() {
        val header = Headers.parse("HOST:abc")
        assertEquals(header, Headers.Host to "abc")
    }

    @Test
    fun testFailOnInvalidHeaderKey() {
        assertFailsWith<IllegalArgumentException> {
            Headers.parse("INVALID_HEADER:abc")
        }
    }

    @Test
    fun testFailOnInvalidHeaderFormat() {
        assertFailsWith<IllegalArgumentException> {
            Headers.parse("HOST abc")
        }
    }
}