package pro.leandev.zeroconf.networking

import java.net.NetworkInterface

/**
 * Specifies Internet Protocol (IP) stack to use
 */
enum class IpStack {
    IPv4Only{
        override fun getAvailableInterfaces(): List<NetworkInterface> = getAvailableInterfaces()
            .filter(NetworkInterface::isAvailableV4Interface)
    },
    IPv6Only{
        override fun getAvailableInterfaces(): List<NetworkInterface> = getAvailableInterfaces()
            .filter(NetworkInterface::isAvailableV6Interface)
    },

    DualStack{
        override fun getAvailableInterfaces(): List<NetworkInterface> = getAvailableInterfaces()
            .filter(NetworkInterface::isAvailableInterface)
    };

    /**
     * Returns the NetworkInterface available for that protocol stack.
     */
    internal abstract fun getAvailableInterfaces(): List<NetworkInterface>
}