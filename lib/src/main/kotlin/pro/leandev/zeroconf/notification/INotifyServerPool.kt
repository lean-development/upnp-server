package pro.leandev.zeroconf.notification

import kotlinx.coroutines.channels.ReceiveChannel
import pro.leandev.zeroconf.notification.message.SsdpMessage
import java.net.InetAddress

interface INotifyServerPool {
    val messages: ReceiveChannel<SsdpMessage>
    fun start(interfaceNames: List<String>? = null)
    fun stop(interfaceNames: List<String>? = null)
    fun search(target: String? = null, inetAddr: InetAddress? = null)
}