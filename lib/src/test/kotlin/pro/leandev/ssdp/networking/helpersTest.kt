package pro.leandev.ssdp.networking

import org.mockito.Mockito.*
import pro.leandev.networking.EVENT_ADDRESS_V4
import pro.leandev.networking.EVENT_ADDRESS_V6
import pro.leandev.networking.SSDP_ADDRESS_V4
import pro.leandev.networking.SSDP_ADDRESS_V6
import pro.leandev.networking.closeQuietly
import pro.leandev.networking.getEventAddress
import pro.leandev.networking.getSsdpAddress
import pro.leandev.networking.isAvailableInterface
import pro.leandev.networking.isAvailableV4Interface
import pro.leandev.networking.isAvailableV6Interface
import java.net.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkUtilsTest {

    @Test
    fun `test NetworkInterface isAvailableInterface with IPv4`() {
        // Mock NetworkInterface
        val networkInterface = mock(NetworkInterface::class.java)
        val interfaceAddress = mock(InterfaceAddress::class.java)

        // Mock the IPv4 address and behaviors
        `when`(networkInterface.isLoopback).thenReturn(false)
        `when`(networkInterface.isUp).thenReturn(true)
        `when`(networkInterface.supportsMulticast()).thenReturn(true)

        // Mock the interface addresses to contain an IPv4 address
        val inet4Address = Inet4Address.getByName("192.168.0.1")
        `when`(interfaceAddress.address).thenReturn(inet4Address)
        `when`(networkInterface.interfaceAddresses).thenReturn(listOf(interfaceAddress))

        // Verify that the interface is available and has IPv4
        assertTrue(networkInterface.isAvailableInterface())
        assertTrue(networkInterface.isAvailableV4Interface())
        assertFalse(networkInterface.isAvailableV6Interface())
    }

    @Test
    fun `test NetworkInterface isAvailableInterface with IPv6`() {
        // Mock NetworkInterface
        val networkInterface = mock(NetworkInterface::class.java)
        val interfaceAddress = mock(InterfaceAddress::class.java)

        // Mock the IPv6 address and behaviors
        `when`(networkInterface.isLoopback).thenReturn(false)
        `when`(networkInterface.isUp).thenReturn(true)
        `when`(networkInterface.supportsMulticast()).thenReturn(true)

        // Mock the interface addresses to contain an IPv6 address
        val inet6Address = Inet6Address.getByName("fe80::1")
        `when`(interfaceAddress.address).thenReturn(inet6Address)
        `when`(networkInterface.interfaceAddresses).thenReturn(listOf(interfaceAddress))

        // Verify that the interface is available and has IPv6
        assertTrue(networkInterface.isAvailableInterface())
        assertFalse(networkInterface.isAvailableV4Interface())
        assertTrue(networkInterface.isAvailableV6Interface())
    }

    @Test
    fun `test NetworkInterface not available due to being loopback`() {
        // Mock NetworkInterface
        val networkInterface = mock(NetworkInterface::class.java)

        // Mock that it's a loopback interface
        `when`(networkInterface.isLoopback).thenReturn(true)
        `when`(networkInterface.isUp).thenReturn(true)
        `when`(networkInterface.supportsMulticast()).thenReturn(true)

        // Verify that it's not available because it's a loopback
        assertFalse(networkInterface.isAvailableInterface())
        assertFalse(networkInterface.isAvailableV4Interface())
        assertFalse(networkInterface.isAvailableV6Interface())
    }

    @Test
    fun `test NetworkInterface is not available due to network being down`() {
        // Mock NetworkInterface
        val networkInterface = mock(NetworkInterface::class.java)

        // Mock that the interface is down
        `when`(networkInterface.isLoopback).thenReturn(false)
        `when`(networkInterface.isUp).thenReturn(false)
        `when`(networkInterface.supportsMulticast()).thenReturn(true)

        // Verify that interface is not available because it's down
        assertFalse(networkInterface.isAvailableInterface())
    }

    @Test
    fun `test Socket closeQuietly`() {
        val socket = mock(Socket::class.java)

        // Call the extension function
        socket.closeQuietly()

        // Verify close method was called
        verify(socket).close()
    }

    @Test
    fun `test MulticastSocket closeQuietly`() {
        val multicastSocket = mock(MulticastSocket::class.java)

        // Call the extension function
        multicastSocket.closeQuietly()

        // Verify close method was called
        verify(multicastSocket).close()
    }

    @Test
    fun `test getSsdpAddress with IPv4`() {
        val networkInterface = mock(NetworkInterface::class.java)
        val interfaceAddress = mock(InterfaceAddress::class.java)

        // Mock that the interface is IPv4
        `when`(networkInterface.isLoopback).thenReturn(false)
        `when`(networkInterface.isUp).thenReturn(true)
        `when`(networkInterface.supportsMulticast()).thenReturn(true)

        // Mock the interface addresses to contain an IPv4 address
        val inet4Address = Inet4Address.getByName("192.168.0.1")
        `when`(interfaceAddress.address).thenReturn(inet4Address)
        `when`(networkInterface.interfaceAddresses).thenReturn(listOf(interfaceAddress))

        val ssdpAddress = getSsdpAddress(networkInterface)

        // Validate that the correct IPv4 address is returned
        assertEquals(Inet4Address.getByName(SSDP_ADDRESS_V4), ssdpAddress)
    }

    @Test
    fun `test getSsdpAddress with IPv6`() {
        val networkInterface = mock(NetworkInterface::class.java)

        // Mock that the interface is IPv6
        `when`(networkInterface.isAvailableV4Interface()).thenReturn(false)
        val ssdpAddress = getSsdpAddress(networkInterface)

        // Validate that the correct IPv6 address is returned
        assertEquals(Inet6Address.getByName(SSDP_ADDRESS_V6), ssdpAddress)
    }

    @Test
    fun `test getEventAddress with IPv4`() {
        val networkInterface = mock(NetworkInterface::class.java)
        val interfaceAddress = mock(InterfaceAddress::class.java)

        // Mock that the interface is IPv4
        `when`(networkInterface.isLoopback).thenReturn(false)
        `when`(networkInterface.isUp).thenReturn(true)
        `when`(networkInterface.supportsMulticast()).thenReturn(true)

        // Mock the interface addresses to contain an IPv4 address
        val inet4Address = Inet4Address.getByName("192.168.0.1")
        `when`(interfaceAddress.address).thenReturn(inet4Address)
        `when`(networkInterface.interfaceAddresses).thenReturn(listOf(interfaceAddress))

        val eventAddress = getEventAddress(networkInterface)

        // Validate that the correct IPv4 event address is returned
        assertEquals(Inet4Address.getByName(EVENT_ADDRESS_V4), eventAddress)
    }

    @Test
    fun `test getEventAddress with IPv6`() {
        val networkInterface = mock(NetworkInterface::class.java)

        // Mock that the interface is IPv6
        `when`(networkInterface.isAvailableV4Interface()).thenReturn(false)
        val eventAddress = getEventAddress(networkInterface)

        // Validate that the correct IPv6 event address is returned
        assertEquals(Inet6Address.getByName(EVENT_ADDRESS_V6), eventAddress)
    }
}
