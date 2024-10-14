package pro.leandev.ssdp.server

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import org.junit.Assert.assertFalse
import org.mockito.Mockito
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import pro.leandev.networking.SSDP_ADDRESS_V6
import pro.leandev.ssdp.message.SsdpMessage
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SsdpServerDelegateTest {

    private lateinit var networkInterface: NetworkInterface
    private lateinit var address: InetAddress
    private lateinit var receivingChannel: SendChannel<SsdpMessage>
    private lateinit var multicastSocket: MulticastSocket

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val testCoroutineContext = newSingleThreadContext("TestContext")

    private lateinit var ssdpServerDelegate: SsdpServerDelegate

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() {
        multicastSocket = mock(MulticastSocket::class.java)

        networkInterface = NetworkInterface.getByName("lo0")    // loopback interface for testing
        address = InetAddress.getByName(SSDP_ADDRESS_V6)        // standard SSDP multicast address
        receivingChannel = Channel()
        ssdpServerDelegate = Mockito.spy(
            SsdpServerDelegate(
                address,
                networkInterface,
                bindPort = 1900,
                receivingChannel = receivingChannel,
                cpName = "TestCP",
                coroutineContext = testCoroutineContext
            )
        )
    }

    @Test
    fun `test initialised correctly`() {
        assertNotNull(ssdpServerDelegate)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test start successfully`() = runBlocking {

        ssdpServerDelegate = Mockito.spy(
            SsdpServerDelegate(
                address,
                networkInterface,
                bindPort = 0,                           // to avoid joining the socket group
                receivingChannel = receivingChannel,
                cpName = "TestCP",
                coroutineContext = testCoroutineContext
            )
        )

        // Mock socket creation
        `when`(ssdpServerDelegate.createMulticastSocket(0)).doReturn(multicastSocket)

        // Start server
        ssdpServerDelegate.start()

        // Verify socket created, joined group, and receiveLoop started
        verify(ssdpServerDelegate).receiveLoop(multicastSocket)
    }

    @Test
    fun `test server starts and stops correctly`() = runBlocking {
        ssdpServerDelegate.start()
        assertTrue(ssdpServerDelegate.isActive())

        ssdpServerDelegate.stop()
        assertFalse(ssdpServerDelegate.isActive())
    }

    @Test
    fun `test send and receive correctly`() = runBlocking {
        ssdpServerDelegate.start()

        val payload = ("NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250\r\nCACHE-CONTROL: max-age=1800\r\n"+
                "LOCATION: 192.168.178.1\r\nNT: upnp:rootdevice\r\nNTS: ssdp:alive\r\n"+
                "USN: uuid:23456789-1234-1010-8000-00AA00389B71::upnp:rootdevice\r\n"+
                "BOOTID.UPNP.ORG: 1\r\nCONFIGID.UPNP.ORG: 12\r\nSEARCHPORT.UPNP.ORG: 2323\r\n").toByteArray()
        val testMessage = SsdpMessage.fromDatagram(DatagramPacket(payload, payload.size, address, 1900))

        ssdpServerDelegate.send(testMessage)

        val receivedMessage = withTimeoutOrNull(500) { (receivingChannel as Channel).receive() }
        assertNotNull(receivedMessage)
        assertEquals(testMessage.type, receivedMessage.type)
        assertEquals(testMessage.bootId, receivedMessage.bootId)

        ssdpServerDelegate.stop()
    }

        @Test
        fun `test start failure due to exception`() = runBlocking {
            // Mock exception during socket creation
            doThrow(IOException("Cannot create socket")).`when`(ssdpServerDelegate).createMulticastSocket(1900)

            // Attempt to start server, which should fail
            ssdpServerDelegate.start()

            // Verify the socket was added to group (meaning it's null)
            verify(multicastSocket, never()).joinGroup(org.mockito.kotlin.any(), org.mockito.kotlin.any())
        }
}