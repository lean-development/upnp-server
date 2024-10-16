package pro.leandev.zeroconf.notification

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import pro.leandev.zeroconf.core.Logging
import pro.leandev.zeroconf.core.log
import pro.leandev.zeroconf.notification.message.Advertisement
import pro.leandev.zeroconf.notification.message.Headers
import pro.leandev.zeroconf.notification.message.SsdpMessage
import pro.leandev.zeroconf.notification.message.SsdpMessageType
import pro.leandev.zeroconf.notification.message.Update
import java.net.NetworkInterface
import kotlin.coroutines.CoroutineContext

class NotificationService(
    cpName:String,
    interfaces: List<NetworkInterface>,
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob(),
    val pool: INotifyServerPool = NotifyServerPool(cpName, interfaces, coroutineContext)
): CoroutineScope, Logging {

    private val knownNodes = mutableMapOf<String, SsdpMessage>()
    private val _notifications = Channel<Notification>()
    val notifications: ReceiveChannel<Notification> = _notifications

    init{
        launch {
            pool.messages.consumeAsFlow().collect(::process)
        }
        pool.start()
    }

    private suspend fun process(msg: SsdpMessage) {
        when (msg.type) {
            SsdpMessageType.Search -> {}
            SsdpMessageType.SearchResponse,
            SsdpMessageType.NotifyAlive     -> processAdvertisement(msg)
            SsdpMessageType.NotifyUpdate    -> processUpdate(msg)
            SsdpMessageType.NotifyByeBye    -> processByeBye(msg)
        }
    }

    private suspend fun processAdvertisement(msg: SsdpMessage) {
        val ad = Advertisement.fromSsdpMessage(msg)
        log().debug("New advertisement: {}", ad)
        if(!ad.isValid()){
            log().debug("Advertisement expired: {}", ad)
            return
        }
        knownNodes[ad.usn] = msg
        _notifications.send(Notification(
            Notification.Command.Add,
            ad.usn,
            ad.location,
            ad.server,
            ad.ttl
        ))
    }

    private suspend fun processUpdate(msg: SsdpMessage) {
        val upd = Update.fromSsdpMessage(msg)
        log().debug("New update: {}", upd)
        knownNodes[upd.usn]?.let {
            if(it.bootId == upd.bootId){
                it.bootId = upd.nextBootId
                return
            } else {
                // Update's bootId is not equal to the advertised earlier. Remove node
                doRemove(upd.usn)
            }
        }
        // Update's bootId is not equal to the advertised earlier. Request node info anew
        log().debug("Update for unknown device or with unexpected BootID: {}", upd.usn)
        // take the UUID part of the USN and search for it
        val searchTarget = upd.usn.split("::")[0]
        pool.search(searchTarget, upd.inetAddr)
    }

    private suspend fun processByeBye(msg: SsdpMessage) {
        val usn = msg.getRequiredHeader(Headers.UniqueServiceName)
        log().debug("ByeBye message from: {}", usn)
        doRemove(usn)
    }

    private suspend fun doRemove(usn: String){
        knownNodes.remove(usn)
        _notifications.send(Notification(
            Notification.Command.Remove,
            usn
        ))
    }

    fun search() { pool.search() }

    fun stop() {
        pool.stop()
        coroutineContext.cancel()
    }
}