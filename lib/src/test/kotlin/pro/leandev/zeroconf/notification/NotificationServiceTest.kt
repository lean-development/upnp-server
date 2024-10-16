package pro.leandev.zeroconf.notification

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.test.*
import org.junit.Assert.assertFalse
import org.mockito.Mockito.*
import pro.leandev.zeroconf.networking.SSDP_ADDRESS_V4
import pro.leandev.zeroconf.notification.message.Headers
import pro.leandev.zeroconf.notification.message.SsdpMessage
import pro.leandev.zeroconf.notification.message.SsdpMessageType
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NotificationServiceTest {

    private lateinit var networkInterface: NetworkInterface
    private lateinit var notifyServerPool: INotifyServerPool
    private lateinit var notificationService: NotificationService
    private lateinit var testScope: TestScope
    private lateinit var messagesChannel: Channel<SsdpMessage>

    @BeforeTest
    fun setUp() {
        // Mock the NetworkInterface and INotifyServerPool
        networkInterface = mock(NetworkInterface::class.java).apply {
            `when`(displayName).thenReturn("eth0")
        }

        // Use TestCoroutineDispatcher for controlled coroutine execution
        testScope = TestScope()

        messagesChannel = Channel<SsdpMessage>(10)
        notifyServerPool = spy(object : INotifyServerPool {
            override val messages: ReceiveChannel<SsdpMessage> = messagesChannel
            override fun start(interfaceNames: List<String>?) {}
            override fun stop(interfaceNames: List<String>?) {}
            override fun search(target: String?, inetAddr: InetAddress?) {}
        })

        // Instantiate the NotificationService with the mocked NotifyServerPool
        notificationService = NotificationService(
            "testCPName",
            listOf(networkInterface),
            pool = notifyServerPool
        )
    }

    @Test
    fun `test service initializes and starts the pool`() = runTest {
        // Verify that the pool was started on initialization
        verify(notifyServerPool).start()
    }

    @Test
    fun `test search triggers pool search`() = runTest {
        // Call search on the service
        notificationService.search()

        // Verify that the pool's search function was called
        verify(notifyServerPool).search()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test processing advertisement message`() = runTest {

        val payload = ("NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=1800\r\nLOCATION: http://192.168.1.100:8080/device-description.xml\r\n" +
                "NT: urn:schemas-upnp-org:device:Basic:1\r\nNTS: ssdp:alive\r\nSERVER: Custom/1.0 UPnP/1.1 MyDevice/1.0\r\n" +
                "USN: uuid:device-unique-id::urn:schemas-upnp-org:device:Basic:1\r\nBOOTID.UPNP.ORG: 1\r\n" +
                "CONFIGID.UPNP.ORG: 1\r\nNEXTBOOTID.UPNP.ORG: 2\r\n").toByteArray()

        // Prepare an SsdpMessage of type NotifyAlive
        val ssdpMessage = SsdpMessage.fromDatagram(
            DatagramPacket(
                payload,
                payload.size,
                InetAddress.getByName(SSDP_ADDRESS_V4),
                1900
            )
        )

        // Send the message into the channel
        messagesChannel.send(ssdpMessage)

        // Simulate processing by advancing time
        advanceUntilIdle()

        // Check the notification channel for the expected notification
        val notification = notificationService.notifications.receive()

        assertNotNull(notification)
        assertEquals(Notification.Command.Add, notification.command)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `test processing update message`() = runTest {
        val payload = ("NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=1800\r\nLOCATION: http://192.168.1.100:8080/device-description.xml\r\n" +
                "NT: urn:schemas-upnp-org:device:Basic:1\r\nNTS: ssdp:update\r\nSERVER: Custom/1.0 UPnP/1.1 MyDevice/1.0\r\n" +
                "USN: uuid:device-unique-id::urn:schemas-upnp-org:device:Basic:1\r\nBOOTID.UPNP.ORG: 1\r\n" +
                "CONFIGID.UPNP.ORG: 1\r\nNEXTBOOTID.UPNP.ORG: 2\r\n").toByteArray()

        // Prepare an SsdpMessage of type NotifyAlive
        val ssdpMessage = SsdpMessage.fromDatagram(
            DatagramPacket(
                payload,
                payload.size,
                InetAddress.getByName(SSDP_ADDRESS_V4),
                1900
            )
        )

        // Send the message into the channel
        messagesChannel.send(ssdpMessage)

        // Simulate processing by advancing time
        advanceUntilIdle()

        runBlocking { delay(100) }

        // Verify that the message was processed and a search was triggered
        verify(notifyServerPool).search(eq("uuid:device-unique-id"), any())

    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `test processing byebye message`() = runTest {
        // Prepare an SsdpMessage of type NotifyByeBye
        val ssdpMessage = mock(SsdpMessage::class.java).apply {
            `when`(type).thenReturn(SsdpMessageType.NotifyByeBye)
            `when`(getRequiredHeader(Headers.UniqueServiceName)).thenReturn("unique-service-name")
        }

        // Send the message into the channel
        messagesChannel.send(ssdpMessage)

        // Simulate processing by advancing time
        advanceUntilIdle()

        // Verify that the message was processed and a remove notification was sent
        val notification = notificationService.notifications.receive()
        assertNotNull(notification)
        assertEquals(Notification.Command.Remove, notification.command)
        assertEquals("unique-service-name", notification.usn)
    }

    @Test
    fun `test stop stops pool and cancels context`() = runTest {
        // Call stop on the service
        notificationService.stop()

        // Verify that the pool was stopped
        verify(notifyServerPool).stop()

        // Verify that the coroutine context was cancelled
        assertFalse(notificationService.coroutineContext.isActive)
    }
}
