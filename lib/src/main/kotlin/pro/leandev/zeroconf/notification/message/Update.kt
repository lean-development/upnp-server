package pro.leandev.zeroconf.notification.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pro.leandev.zeroconf.serializer.InetAddressSerializer
import java.net.InetAddress

/**
 * Device or service update message
 * Used to validate provided fields and stringify data
 */
@Serializable
data class Update(
    val location: String,
    val usn: String,
    val bootId: Int,
    val configId: Int,
    val searchPort: Int?,
    val nextBootId: Int,
    @Serializable(with = InetAddressSerializer::class)
    val inetAddr: InetAddress?
){
    companion object{
        fun fromSsdpMessage(message: SsdpMessage): Update = Update(
            message.getRequiredHeader(Headers.Location),
            message.getRequiredHeader(Headers.UniqueServiceName),
            message.getRequiredHeader(Headers.BootId).toInt(),
            message.getRequiredHeader(Headers.ConfigId).toInt(),
            message.getHeader(Headers.SearchPort)?.toInt(),
            message.getRequiredHeader(Headers.NextBootId).toInt(),
            message.address
        )
    }
    override fun toString() = Json.encodeToString(this)
}
