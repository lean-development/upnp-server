package pro.leandev.ssdp.message

import java.net.DatagramPacket
import java.net.InetAddress

/**
 * Simple Service Discovery Protocol (SSDP) message of specified [type]
 */
data class SsdpMessage(
    val type: SsdpMessageType,
    val ts: Long = System.currentTimeMillis(),
    var address: InetAddress? = null,
    var port: Int? = null,
    var bootId: Int = 0
) {
    private val headers: MutableMap<Headers, String> = type.headers

    /**
     * @return the value of [header]
     */
    fun getHeader(header: Headers): String? = headers[header]

    fun getRequiredHeader(header: Headers): String = headers[header]
        ?: throw IllegalStateException("SSDP message has no required header $header")

    /**
     * Set the [value] of [header] and @return current instance of the message
     */
    fun setHeader(header: Headers, value: String): SsdpMessage {
        headers[header] = value
        return this
    }

    /**
     * @return true if SSDP message is expired
     */
    fun isExpired(): Boolean = getHeader(Headers.CacheControl)
        ?.let { ttl -> (ttl.toInt() + ts) < System.currentTimeMillis() } == true

    /**
     * Write message as byte array
     */
    fun asByteArray(): ByteArray = (
        "${type.method}\r\n"
        + headers.map { (k, v) -> "${k.value}:$v" }.joinToString("\r\n")
        + "\r\n\r\n"
    ).toByteArray()

    companion object {
        /**
         * @return SSDP message from [datagram]
         */
        fun fromDatagram(datagram: DatagramPacket): SsdpMessage {
            val rawData = datagram.data
                .decodeToString(datagram.offset, datagram.length - datagram.offset, true)

            val msg = rawData.split("\r\n")
                .filter { it.isNotBlank() }
            val method = msg[0]
            val hdrs = try {
                msg.drop(1).associate(Headers.Companion::parse)
            } catch (ex: Exception) {
                throw IllegalArgumentException(ex.message)
            }
            val res =  SsdpMessage(
                type = SsdpMessageType.from(method, hdrs),
                address = datagram.address,
                port = datagram.port,
                bootId = hdrs[Headers.BootId]?.toInt()
                    ?: throw IllegalArgumentException("SSDP message must have BOOTID header")
            )
            res.headers.putAll(hdrs)
            return res
        }
    }
}