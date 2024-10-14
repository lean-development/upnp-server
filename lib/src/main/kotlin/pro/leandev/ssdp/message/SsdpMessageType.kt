package pro.leandev.ssdp.message

import pro.leandev.ssdp.message.Headers.Companion.NOTIFY
import pro.leandev.ssdp.message.Headers.Companion.NOTIFY_ALIVE
import pro.leandev.ssdp.message.Headers.Companion.NOTIFY_BYE
import pro.leandev.ssdp.message.Headers.Companion.NOTIFY_UPDATE
import pro.leandev.ssdp.message.Headers.Companion.OK
import pro.leandev.ssdp.message.Headers.Companion.SEARCH
import pro.leandev.ssdp.message.Headers.Companion.SSDP_MAN

private val searchHeaders = mutableMapOf(
    Headers.ManagedAwarenessNumber to SSDP_MAN,
    Headers.MaxWaitTime to "1",
    Headers.SearchTarget to "ssdp:all"
)

private fun notifyHeadersForNTS(@Suppress("SameParameterValue") nts: String) = mutableMapOf(
    Headers.NotificationSubType to nts
)

/**
 * Defines types of the SSDP message
 */
enum class SsdpMessageType(val method: String, val headers: MutableMap<Headers, String>) {
    Search(SEARCH, searchHeaders),
    SearchResponse(OK, mutableMapOf<Headers, String>()),
    NotifyAlive(NOTIFY, notifyHeadersForNTS(NOTIFY_ALIVE)),
    NotifyByeBye(NOTIFY, notifyHeadersForNTS(NOTIFY_BYE)),
    NotifyUpdate(NOTIFY, notifyHeadersForNTS(NOTIFY_UPDATE));

    companion object {
        /**
         * @return [SsdpMessageType] which corresponds to [firstLine] and NTS header from [hdrs] if
         * it's a NOTIFY message
         */
        fun from(firstLine: String, hdrs: Map<Headers, String>): SsdpMessageType {
            when (firstLine) {
                SEARCH -> return Search
                OK -> return SearchResponse
                NOTIFY -> {
                    val nts = hdrs[Headers.NotificationSubType]
                        ?: throw IllegalArgumentException("SSDP Notify message must have NTS header")
                    return when (nts) {
                        NOTIFY_ALIVE -> NotifyAlive
                        NOTIFY_BYE -> NotifyByeBye
                        NOTIFY_UPDATE -> NotifyUpdate
                        else -> throw IllegalArgumentException("Unknown SSDP message sub type: $nts")
                    }
                }
                else -> throw IllegalArgumentException("Unknown SSDP message type: $firstLine")
            }
        }
    }
}