package pro.leandev.zeroconf.networking

import java.io.IOException
import java.net.*

/**
 * Checks if [NetworkInterface] has IPv4/IPv6 address that can communicate with the network
 */
fun NetworkInterface.isAvailableInterface(): Boolean =
    isConnectedToNetwork() && (hasV4Address() || hasV6Address())

/**
 * Checks if [NetworkInterface] has IPv4 address that can communicate with the network
 */
fun NetworkInterface.isAvailableV4Interface(): Boolean =
    isConnectedToNetwork() && hasV4Address()

/**
 * Checks if [NetworkInterface] has IPv6 address that can communicate with the network
 */
fun NetworkInterface.isAvailableV6Interface(): Boolean =
    isConnectedToNetwork() && hasV6Address()

/**
 * Checks if [NetworkInterface] has address that can communicate with the network
 */
private fun NetworkInterface.isConnectedToNetwork(): Boolean = try {
    !isLoopback && isUp && supportsMulticast()
} catch (_: SocketException) {
    false
}

/**
 * Checks if [NetworkInterface] has IPv4 address
 */
private fun NetworkInterface.hasV4Address(): Boolean = interfaceAddresses
    .any { it.address is Inet4Address }

/**
 * Checks if [NetworkInterface] has IPv6 address
 */
private fun NetworkInterface.hasV6Address(): Boolean =
    interfaceAddresses.any { it.address is Inet6Address }

/**
 * Perform close processing with null check and exception catch
 */
internal fun Socket.closeQuietly() {
    try { this.close() } catch (_: IOException) { }
}

/**
 * Perform close processing with null check and exception catch
 */
internal fun MulticastSocket.closeQuietly() {
    try { this.close() } catch (_: IOException) { }
}

fun getSsdpAddress(ni: NetworkInterface): InetAddress = if(ni.isAvailableV4Interface()){
    Inet4Address.getByName(SSDP_ADDRESS_V4)
} else {
    Inet6Address.getByName(SSDP_ADDRESS_V6)
}

fun getSsdpSocketAddress(ni: NetworkInterface): SocketAddress = InetSocketAddress(getSsdpAddress(ni), SSDP_PORT)

fun getEventAddress(ni: NetworkInterface): InetAddress {
    return if (ni.isAvailableV4Interface()) {
        Inet4Address.getByName(EVENT_ADDRESS_V4)
    } else {
        Inet6Address.getByName(EVENT_ADDRESS_V6)
    }
}