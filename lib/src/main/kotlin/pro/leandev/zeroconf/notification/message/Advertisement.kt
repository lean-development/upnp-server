package pro.leandev.zeroconf.notification.message

import kotlinx.datetime.format.DateTimeComponents
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Device or service advertisement
 * Used to validate provided fields and stringify data
 */
@Serializable
data class Advertisement(
    val location: String,
    val type: String,
    val server: String,
    val usn: String,
    val bootId: Int,
    val configId: Int,
    val searchPort: Int?,
    val ttl: Long
){
    companion object{
        fun fromSsdpMessage(message: SsdpMessage): Advertisement = Advertisement(
            message.getRequiredHeader(Headers.Location),
            message.getRequiredHeader(Headers.NotificationType),
            message.getRequiredHeader(Headers.Server),
            message.getRequiredHeader(Headers.UniqueServiceName),
            message.getRequiredHeader(Headers.BootId).toInt(),
            message.getRequiredHeader(Headers.ConfigId).toInt(),
            message.getHeader(Headers.SearchPort)?.toInt(),
            (message.getHeader(Headers.Date)
                ?.let { DateTimeComponents.Formats.RFC_1123.parse(it).toInstantUsingOffset().toEpochMilliseconds() }
                ?: message.ts
            ) + message.getRequiredHeader(Headers.CacheControl).substringAfter("max-age=").toLong()
        )
    }

    fun isValid() = ttl > System.currentTimeMillis()
    override fun toString() = Json.encodeToString(this)
}
