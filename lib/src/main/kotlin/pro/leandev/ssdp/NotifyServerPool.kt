package pro.leandev.ssdp

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import pro.leandev.networking.*
import pro.leandev.ssdp.message.SsdpMessage
import pro.leandev.ssdp.server.SsdpServerDelegate
import java.net.InetAddress
import java.net.NetworkInterface
import kotlin.coroutines.CoroutineContext

/**
 * Pool of NotifyServers that serves different network interfaces (one server per interface)
 * @param interfaces - list of network interfaces to bind servers to
 * @param cpName - name of the control point for CPFN.UPNP.ORG header
 */
class NotifyServerPool(
    cpName: String,
    interfaces: List<NetworkInterface>,
    coroutineContext: CoroutineContext
) {
    // 1000 buffered notifications must be enough for the majority of use-cases
    private val messages = Channel<SsdpMessage>(1000)
    val notifications: ReceiveChannel<SsdpMessage> = messages

    private var pool = interfaces.associateBy({ it.displayName }, { it ->
        NotifyServer(
            SsdpServerDelegate(
                getSsdpAddress(it),
                it,
                SSDP_PORT,
                messages,
                cpName,
                coroutineContext
            )
        )
    })

    fun start(interfaceNames: List<String>? = null) = interfaceNames.executeOnPool { it.start() }
    fun stop(interfaceNames: List<String>? = null) = interfaceNames.executeOnPool { it.stop() }
    fun search(target: String? = null, interAddr: InetAddress? = null) = interAddr
        ?.let { NetworkInterface.getByInetAddress(it)?.displayName }
        ?.let { pool[it]?.search(target) }
        ?: pool.values.forEach { it.search(target) }

    // for testing purposes
    internal fun getPool() = pool
    internal fun setPool(testPool: Map<String, NotifyServer>) { pool = testPool}

    private fun List<String>?.executeOnPool(action: (NotifyServer) -> Unit) = this
        ?.forEach { pool[it]?.let(action) }
        ?: pool.values.forEach { action(it) }
}