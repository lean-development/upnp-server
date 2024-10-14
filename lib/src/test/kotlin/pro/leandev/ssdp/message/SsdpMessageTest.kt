package pro.leandev.ssdp.message

import java.lang.Thread.sleep
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.net.DatagramPacket
import java.net.InetAddress
import kotlin.test.assertFalse

class SsdpMessageTest {

    private lateinit var ssdpMessage: SsdpMessage

    @BeforeTest
    fun setUp() {
        val payload =  """NOTIFY * HTTP/1.1
                |HOST: 239.255.255.250
                |CACHE-CONTROL: max-age=1800
                |LOCATION: URL for UPnP description for root device
                |NT: upnp:rootdevice
                |NTS: ssdp:alive
                |USN: uuid:23456789-1234-1010-8000-00AA00389B71::upnp:rootdevice
                |BOOTID.UPNP.ORG: 1
                |CONFIGID.UPNP.ORG: 12              
                |SEARCHPORT.UPNP.ORG: 2323
            """.trimMargin().toByteArray()
        ssdpMessage = SsdpMessage.fromDatagram(DatagramPacket(
            payload,
            0,
            payload.size,
            InetAddress.getByName("239.255.255.250"),
            1900
        ))
    }

    @Test
    fun testInitialization() {
        assertNotNull(ssdpMessage)
        assertEquals("max-age=1800", ssdpMessage.getRequiredHeader(Headers.CacheControl))
    }

    @Test
    fun testHeaderManipulation() {
        ssdpMessage.setHeader(Headers.Server, "TestServer")
        assertEquals("TestServer", ssdpMessage.getHeader(Headers.Server))

        val requiredHeader = ssdpMessage.getRequiredHeader(Headers.CacheControl)
        assertEquals("max-age=1800", requiredHeader)
    }

    @Test
    fun testExpiration() {
        ssdpMessage.setHeader(Headers.CacheControl, "3")
        assertFalse(ssdpMessage.isExpired())                // must happen before expiration
        sleep(5)                                            // wait 5 ms
        assertTrue(ssdpMessage.isExpired())                 // must happen after expiration
    }

    @Test
    fun testHeadersFromDatagram() {
        assertNotNull(ssdpMessage)
        assertEquals("max-age=1800", ssdpMessage.getHeader(Headers.CacheControl))
        assertEquals(1, ssdpMessage.bootId)
        assertEquals(InetAddress.getByName("239.255.255.250"), ssdpMessage.address)
        assertEquals(1900, ssdpMessage.port)
    }
}