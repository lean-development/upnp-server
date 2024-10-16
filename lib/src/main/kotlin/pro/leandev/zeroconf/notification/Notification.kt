package pro.leandev.zeroconf.notification

data class Notification(
    val command: Command,
    val usn: String,
    val location: String? = null,
    val server: String? = null,
    val ttl: Long? = null
){
    enum class Command{ Add, Remove }
}