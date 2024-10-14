package pro.leandev.ssdp

import pro.leandev.ssdp.message.Headers
import pro.leandev.ssdp.message.SsdpMessageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pro.leandev.networking.SSDP_ADDRESS_V4
import pro.leandev.networking.SSDP_ADDRESS_V6
import pro.leandev.networking.SSDP_PORT
import pro.leandev.networking.isAvailableV4Interface
import pro.leandev.ssdp.message.SsdpMessage
import pro.leandev.ssdp.server.ISsdpServer
import pro.leandev.ssdp.server.SsdpServerDelegate

const val repeatSearch = 3          // how many times to resend search request (required by UPnP spec)

class NotifyServer(
    delegate: SsdpServerDelegate
) : ISsdpServer by delegate {

    /**
     * When a control point desires to search the network for devices, it shall send a multicast
     * request with method M-SEARCH in the following format. Control points that know the address
     * of a specific device are allowed to also use a similar format to send unicast requests with
     * method M-SEARCH.
     * Format:
     *      M-SEARCH * HTTP/1.1
     *      HOST: 239.255.255.250:1900
     *      MAN: "ssdp:discover"
     *      MX: seconds to delay response
     *      ST: search target
     *      USER-AGENT: OS/version UPnP/2.0 product/version
     *      CPFN.UPNP.ORG: friendly name of the control point
     *      CPUUID.UPNP.ORG: uuid of the control point
     *
     * Due to the unreliable nature of UDP, control points should send each M-SEARCH message
     * more than once. As a fallback, to guard against the possibility that a device might not receive
     * the M-SEARCH message from a control point, a device should re-send its advertisements
     * periodically (see CACHE-CONTROL header field in NOTIFY with ssdp:alive above).
     *
     * @see "https://openconnectivity.org/upnp-specs/UPnP-arch-DeviceArchitecture-v2.0-20200417.pdf" ch.1.3.2
     *
     * @param target        - search target.
     * @param maxWaitTime   - maximum wait time in seconds. shall be greater than or equal to 1 and should
     *                          be less than 5 inclusive. Device responses should be delayed a random duration
     *                          between 0 and this many seconds to balance load for the control point when it
     *                          processes responses.
     */
    fun search(target: String? = null, maxWaitTime: Int = 1) {
        val msg = SsdpMessage(
            SsdpMessageType.Search
        ).also {
            val host = if (this.networkInterface.isAvailableV4Interface()) SSDP_ADDRESS_V4 else "[$SSDP_ADDRESS_V6]"

            it.setHeader(Headers.Host, "$host:$SSDP_PORT")
            it.setHeader(Headers.SearchTarget, target?: "ssdp:all")
            it.setHeader(Headers.MaxWaitTime, maxWaitTime.toString())
            it.setHeader(Headers.ControlPointFriendlyName, this.cpName)
        }

        launch {
            1.rangeTo(repeatSearch).forEach { _ ->
                send(msg)
                runBlocking { delay(maxWaitTime * 1000L) }
            }
        }
    }
}