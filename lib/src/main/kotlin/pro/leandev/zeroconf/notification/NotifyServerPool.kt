package pro.leandev.zeroconf.notification

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import pro.leandev.zeroconf.networking.*
import pro.leandev.zeroconf.notification.message.SsdpMessage
import pro.leandev.zeroconf.notification.server.SsdpServerDelegate
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
): INotifyServerPool {
    // 1000 buffered notifications must be enough for the majority of use-cases
    private val _messages = Channel<SsdpMessage>(1000)
    override val messages: ReceiveChannel<SsdpMessage> = _messages

    private var pool = interfaces.associateBy({ it.displayName }, { it ->
        NotifyServer(
            SsdpServerDelegate(
                getSsdpAddress(it),
                it,
                SSDP_PORT,
                _messages,
                cpName,
                coroutineContext
            )
        )
    })

    override fun start(interfaceNames: List<String>?) = interfaceNames.executeOnPool { it.start() }
    override fun stop(interfaceNames: List<String>?) = interfaceNames.executeOnPool { it.stop() }
    override fun search(target: String?, inetAddr: InetAddress?) = inetAddr
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