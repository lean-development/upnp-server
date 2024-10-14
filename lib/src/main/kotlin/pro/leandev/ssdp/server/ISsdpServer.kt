package pro.leandev.ssdp.server

import kotlinx.coroutines.CoroutineScope
import pro.leandev.ssdp.message.SsdpMessage
import java.net.InetAddress
import java.net.NetworkInterface

/**
 * Interface for working with UDP Socket
 */

interface ISsdpServer: CoroutineScope {
    val networkInterface: NetworkInterface

    // Friendly name of the control point for CPFN.UPNP.ORG header
    val cpName : String

    /**
     * Set [filter] for inbound datagrams. If null - reset filter (accept all)
     */
    fun setFilter(filter: ((SsdpMessage) -> Boolean)?)

    /**
     * Start receiving loop
     */
    fun start(): ISsdpServer?

    /**
     * Stop receiving loop
     */
    fun stop()

    /**
     * Send [message] to [address] and [port] or broadcast if [address] is null
     */
    fun send(message: SsdpMessage, address: InetAddress? = null, port: Int? = null)

    /**
     * Check if server is active
     */
    fun isActive(): Boolean
}