package pro.leandev.ssdp.server

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import pro.leandev.core.Logging
import pro.leandev.core.log
import pro.leandev.networking.closeQuietly
import pro.leandev.networking.getSsdpSocketAddress
import pro.leandev.ssdp.message.SsdpMessage
import java.io.IOException
import java.net.*
import kotlin.coroutines.CoroutineContext

/**
 * Implements common [pro.leandev.ssdp.server.ISsdpServer] functionality
 * @param networkInterface  - network interface to bind server to
 * @param bindPort          - port number
 * @param address           - multicast address
 * @param coroutineContext  - context to use
 * @param cpName            - name of the control point for CPFN.UPNP.ORG header
 */
class SsdpServerDelegate(
    private val address: InetAddress,
    override val networkInterface: NetworkInterface,
    private val bindPort: Int = 0,
    private val receivingChannel: SendChannel<SsdpMessage>,
    override val cpName: String,
    override val coroutineContext: CoroutineContext,
) : ISsdpServer, Logging {
    private var socket: MulticastSocket? = null
    private var loop: Job? = null
    private var inboundFilter: (SsdpMessage) -> Boolean = { true }         // accept all

    override fun setFilter(filter: ((SsdpMessage) -> Boolean)?) {
        inboundFilter = filter ?: { true }
    }

    override fun start(): ISsdpServer? {
        println("startin")
        if (!this.coroutineContext.isActive) return null

        try {
            val socket = createMulticastSocket(bindPort)
            this.socket = socket
            if (bindPort != 0) {
                socket.joinGroup(getSsdpSocketAddress(socket.networkInterface), socket.networkInterface)
            }
            receiveLoop(socket)
        } catch (ex: Exception) {
            log().error("Cannot start SSDP server", ex)
            this.socket?.closeQuietly()
            this.socket = null
        }
        return this
    }

    @Throws(IOException::class)
    internal fun createMulticastSocket(port: Int) = MulticastSocket(port).also {
        it.networkInterface = networkInterface
        it.timeToLive = 4
    }

    @Throws(IOException::class)
    internal fun receiveLoop(socket: MulticastSocket) {
        this.loop = launch(coroutineContext) {
            val buf = ByteArray(1500)
            while (this.coroutineContext.isActive) {
                try {
                    val dp = DatagramPacket(buf, buf.size)
                    socket.receive(dp)
                    val msg = SsdpMessage.fromDatagram(dp)
                    if (inboundFilter.invoke(msg)) {
                        receivingChannel.send(msg)
                    }
                } catch (ex: Exception) {
                    log().error("Failed to receive UDP packet", ex)
                }
            }
        }
    }

    override fun stop() {
        this.socket?.closeQuietly()
        this.socket = null
        this.loop?.cancel()
    }

    override fun send(message: SsdpMessage, address: InetAddress?, port: Int?) {
        val payload = message.asByteArray()
        this.socket?.send(
            DatagramPacket(
                payload,
                payload.size,
                address ?: this.address,
                port ?: bindPort
            )
        )
    }

    override fun isActive() = this.loop?.isActive == true
}