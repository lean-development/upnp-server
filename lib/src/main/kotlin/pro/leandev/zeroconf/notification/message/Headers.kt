package pro.leandev.zeroconf.notification.message

enum class Headers(val value: String) {
    /**
     * The multicast address and port reserved for SSDP by Internet Assigned Numbers Authority (IANA).
     * Shall be 239.255.255.250:1900. If the port number (“:1900”) is omitted, the receiver shall
     * assume the default SSDP port number of 1900.
     *
     * Required in both [NOTIFY] and [SEARCH].
     */
    Host("HOST"),

    /**
     *  Field value shall have the max-age directive (“max-age=”) followed by an integer that specifies the
     *  number of seconds the advertisement is valid. After this duration, control points should assume the
     *  device (or service) is no longer available; as long as a control point has received at least one
     *  advertisement that is still valid from a root device, any of its embedded devices or any of its
     *  services, then the control point can assume that all are available.
     *  The number of seconds should be greater than or equal to 1800 seconds (30 minutes), although exceptions
     *  are defined in the text above.
     *  Specified by UPnP vendor.
     *  Other directives shall NOT be sent and shall be ignored when received.
     * Required in [NOTIFY] with NTS: ssdp:alive.
     * @see NotificationSubType
     */
    CacheControl("CACHE-CONTROL"),

    /**
     * Field value shall be the same as the LOCATION field value that has been sent in previous SSDP messages.
     * Normally the host portion contains a literal IP address rather than a domain name in unmanaged networks.
     * Specified by UPnP vendor.
     * Required in [NOTIFY] with NTS: ssdp:alive
     * Single absolute URL (see RFC 3986).
     * @see NotificationSubType
     */
    Location("LOCATION"),

    /**
     * Field value contains Notification Type. Shall be one of the following:
     *   upnp:rootdevice  - sent once for root device,
     *   uuid:device-UUID - sent once for each device, root or embedded, where device-UUID is
     *                         specified by the UPnP vendor.
     *   urn:schemas-upnp-org:device:deviceType:ver - sent once for each device, root or embedded, where deviceType
     *                          and ver are defined by UPnP Forum working committee, and ver specifies the version
     *                          of the device type.
     *   urn:schemas-upnp-org:service:serviceType:ver - sent once for each service where serviceType and ver are
     *                          defined by UPnP Forum working committee and ver specifies the version of the
     *                          service type.
     *   urn:domain-name:device:deviceType:ver - sent once for each device, root or embedded, where domain-name
     *                          is a Vendor Domain Name, deviceType and ver are defined by the UPnP vendor, and
     *                          ver specifies the version of the device type. Period characters in the Vendor
     *                          Domain Name shall be replaced with hyphens in accordance with RFC 2141.
     *   urn:domain-name:service:serviceType:ver - sent once for each service where domain-name is a Vendor Domain
     *                          Name, serviceType and ver are defined by UPnP vendor, and ver specifies the version
     *                          of the service type. Period characters in the Vendor Domain Name shall be replaced
     *                          with hyphens in accordance with RFC 2141.
     * Required in [NOTIFY].
     * Single URI.
     */
    NotificationType("NT"),

    /**
     * Field value contains Notification Sub-type.
     * Shall be one of the following:
     *   ssdp:alive     - sent when a device is added to the network,
     *   ssdp:byebye    - sent when a device and its services are going to be removed from the network,
     *   ssdp:update    - sent when a new UPnP-enabled interface is added to a multi-homed device.
     * Single URI.
     * Required in [NOTIFY]
     */
    NotificationSubType("NTS"),

    /**
     * Field value shall begin with the following “product tokens” (defined by HTTP/1.1):
     *  - identifier of the operating system in the form "OS_name/OS_version",
     *  - the UPnP version and shall be UPnP/2.0, and
     *  - identifier of the product using the form product name/product version.
     *  For example, “SERVER: unix/5.1 UPnP/2.0 MyProduct/1.0”.
     *  String. Specified by UPnP vendor.
     *  Required in [NOTIFY]
     */
    Server("SERVER"),

    /**
     * Identifies a unique instance of a device or service. For allowed values
     * @see 1.2.2 Device available - NOTIFY with ssdp:alive at
     *      https://openconnectivity.org/upnp-specs/UPnP-arch-DeviceArchitecture-v2.0-20200417.pdf
     * The prefix (before the double colon) shall match the value of the UDN element in the device description.
     * Single URI.
     *   uuid:device-UUID::upnp:rootdevice - sent once for root device where device-UUID is specified by UPnP vendor.
     *   uuid:device-UUID                  - sent once for every device, root or embedded, where device-UUID is
     *                                          specified by the UPnP vendor.
     *   uuid:device-UUID::urn:schemas-upnp-org:device:deviceType:ver - Sent once for every device, root or embedded,
     *                                          where device-UUID is specified by the UPnP vendor,  deviceType and ver
     *                                          are defined by UPnP Forum working committee and ver specifies version
     *                                          of the device type.
     *   uuid:device-UUID::urn:schemas-upnp-org:service:serviceType:ver - Sent once for every service where device-UUID
     *                                          is specified by the UPnP vendor, serviceType and ver are defined by
     *                                          UPnP Forum working committee and ver specifies version of the device
     *                                          type.
     *   uuid:device-UUID::urn:domain-name:device:deviceType:ver - sent once for every device, root or embedded, where
     *                                          device-UUID, domain-name (a Vendor Domain Name), deviceType and ver are
     *                                          defined by the UPnP vendor and ver specifies the version of the
     *                                          device type. Period characters in the Vendor Domain Name shall be
     *                                          replaced by hyphens in accordance with RFC 2141.
     *   uuid:device-UUID::urn:domain-name:service:serviceType:ver - sent once for every service where device-UUID,
     *                                          domain-name (a Vendor Domain Name), serviceType and ver are defined by
     *                                          the UPnP vendor and ver specifies the version of the service type.
     *                                          Period characters in the Vendor Domain Name shall be replaced by
     *                                          hyphens in accordance with RFC 2141.
     *
     * Required in [NOTIFY] with NTS ssdp:alive or NTS ssdp:update
     * @see NotificationSubType
     */
    UniqueServiceName("USN"),

    /**
     * The BOOTID.UPNP.ORG header field represents the boot instance of the device expressed
     * according to a monotonically increasing value. Its field value shall be a non-negative 31-bit integer; ASCII
     * encoded, decimal, without leading zeros (leading zeroes, if present, shall be ignored by the recipient) that shall
     * be increased on each initial announce of the UPnP device or shall be the same as the field value of the
     * NEXTBOOTID.UPNP.ORG header field in the last sent SSDP update message. Its field value shall remain the
     * same on all periodically repeated announcements. A convenient mechanism is to set this field value to the time
     * that the device sends its initial announcement, expressed as seconds elapsed since midnight January 1, 1970;
     * for devices that have a notion of time, this will not require any additional state to remember or be “flashed”.
     * However, it is perfectly acceptable to use a simple boot counter that is incremented on every initial
     * announcement as a field value of this header field. As such, control points shall NOT view this header field as
     * a timestamp. The BOOTID.UPNP.ORG header field shall be included in all announcements of a root device, its
     * embedded devices and its services. Unless the device explicitly updates its value by sending an SSDP update
     * message, as long as the device remains available in the network, the same BOOTID.UPNP.ORG field value
     * shall be used in all announcements, search responses, update messages and eventually bye-bye messages.
     * Control points can use this header field to detect the case when a device leaves and rejoins the network
     * (“reboots” in UPnP terms). It can be used by control points for a number of purposes such as re-establishing
     * desired event subscriptions, checking for changes to the device state that were not evented since the device
     * was off-line.
     * Required in [NOTIFY]
     */
    BootId("BOOTID.UPNP.ORG"),

    /**
     * Field value contains the new BOOTID.UPNP.ORG field value that the device intends to use in the
     * subsequent device and service announcement messages. Its field value shall be a non-negative 31-bit integer;
     * ASCII encoded, decimal, without leading zeros (leading zeroes, if present, shall be ignored by the recipient)
     * and shall be greater than the field value of the BOOTID.UPNP.ORG header field.
     * Required in [NOTIFY] with NTS ssdp:update
     */
    NextBootId("NEXTBOOTID.UPNP.ORG"),

    /**
     * The CONFIGID.UPNP.ORG field value shall be a non-negative, 31-bit integer, ASCII encoded,
     * decimal, without leading zeros (leading zeroes, if present, shall be ignored by the recipient) that shall
     * represent the configuration number of a root device. UPnP 2.0 devices are allowed to be freely assign configid
     * numbers from 0 to 16777215 (2^24-1). Higher numbers are reserved for future use, and can be assigned by
     * the Technical Committee. The configuration of a root device consists of the following information: the DDD of
     * the root device and all its embedded devices, and the SCPDs of all the contained services. If any part of the
     * configuration changes, the CONFIGID.UPNP.ORG field value shall be changed. The CONFIGID.UPNP.ORG
     * header field shall be included in all announcements of a root device, its embedded devices and its services.
     * The configuration number that is present in a CONFIGID.UPNP.ORG field value shall satisfy the following rule:
     * • if a device sends out two messages with a CONFIGID.UPNP.ORG header field with the same field value K,
     * the configuration shall be the same at the moments that these messages were sent.
     * Whenever a control point receives a CONFIGID.UPNP.ORG header field with a field value K, and subsequently
     * downloads the configuration information, this configuration information is associated with K. As an additional
     * safeguard, the device shall include a configId attribute with value K in the returned description (see clause 2,
     * “Description”). The following caching rules for control points supersede the caching rules that are defined in
     * UPnP 1.0:
     * • Control points are allowed to ignore the CONFIGID.UPNP.ORG header field and use the caching rules that
     * are based on advertisement expirations as defined in Clause 2, Description: as long as at least one of the
     * discovery advertisements from a root device, its embedded devices and its services have not expired, a
     * control point is allowed to assume that the root device and all its embedded devices and all its services
     * are available. The device and service descriptions are allowed to be retrieved at any point since the
     * device and service descriptions are static as long as the device and its services are available.
     * • If no configuration number is included in a received SSDP message, control points should cache based on
     * advertisement expirations as defined in Clause 2 Description.
     * • If a CONFIGID.UPNP.ORG header field with field value K is included in a received SSDP message, and a
     * control point has already cached information associated with field value K, the control point is allowed to
     * use this cached information as the current configuration of the device. Otherwise, a control point should
     * assume it has not cached the current configuration of the device and needs to send new description query
     * messages.
     * The CONFIGID.UPNP.ORG header field reduces peak loads on UPnP devices during startup and during
     * network hiccups. Only if a control point receives an announcement of an unknown configuration is downloading
     * required.
     *
     * Required in [NOTIFY]
     */
    ConfigId("CONFIGID.UPNP.ORG"),

    /**
     * If a device does not send the SEARCHPORT.UPNP.ORG header field, it shall respond to unicast MSEARCH messages on
     * port 1900. Only if port 1900 is unavailable it is allowed for a device select a different
     * port to respond to unicast M-SEARCH messages. If a device sends the SEARCHPORT.UPNP.ORG header
     * field, its field value shall be an ASCII encoded integer, decimal, without leading zeros (leading zeroes, if
     * present, shall be ignored by the recipient), in the range 49152-65535 (RFC 4340). The device shall respond to
     * unicast M-SEARCH messages that are sent to the advertised port.
     *
     * Allowed in [NOTIFY] with NTS ssdp:alive or NTS ssdp:update.
     * @see NotificationSubType
     */
    SearchPort("SEARCHPORT.UPNP.ORG"),

    /**
     * The SECURELOCATION.UPNP.ORG header shall provide a base URL with “https:” for the scheme component
     * and indicate the correct “port” subcomponent in the “authority” component for a TLS connection. Because the
     * scheme and authority components are not included in relative URLs, these components are obtained from the
     * base URL provided by either LOCATION or SECURELOCATION.UPNP.ORG. See for more information Ref
     * DEVICEPROTECTION
     *
     * Allowed in [NOTIFY] with NTS ssdp:alive or NTS ssdp:update. Required when Device Protection is implemented.
     * @see NotificationSubType
     */
    SecureLocation("SECURELOCATION.UPNP.ORG"),


    /**
     * Required by HTTP Extension Framework. Defines the scope (namespace) of the extension.
     * Unlike the NTS and ST field values, the field value of the MAN header field is enclosed in double quotes.
     * Shall be "ssdp:discover".
     *
     * Required in [SEARCH]
     * @see SSDP_MAN
     */
    ManagedAwarenessNumber("MAN"),

    /**
     * Maximum wait time in seconds. Shall be greater than or equal to 1 and should be less than 5 inclusive.
     * Device responses should be delayed a random duration between 0 and this many seconds to balance load for
     * the control point when it processes responses. This value is allowed to be increased if a large number of
     * devices are expected to respond. The MX field value should NOT be increased to accommodate network
     * characteristics such as latency or propagation delay.
     * Integer. Specified by UPnP vendor.
     *
     * Required in [SEARCH]
     */
    MaxWaitTime("MX"),

    /**
     * Required. Field value contains Search Target. Shall be one of the following:
     *   ssdp:all                   - search for all devices and services.
     *   upnp:rootdevice            - search for root devices only.
     *   uuid:device-UUID           - search for a particular device. device-UUID specified by UPnP vendor.
     *   urn:schemas-upnp-org:device:deviceType:ver - search for any device of this type where deviceType and ver
     *                              are defined by the UPnP Forum working committee.
     *   urn:schemas-upnp-org:service:serviceType:ver - search for any service of this type where serviceType and
     *                              ver are defined by the UPnP Forum working committee.
     *   urn:domain-name:device:deviceType:ver - search for any device of this typewhere domain-name (a Vendor
     *                              Domain Name), deviceType and ver are defined by the UPnP vendor and ver specifies
     *                              the highest specifies the highest supported version of the device type. Period
     *                              characters in the Vendor Domain Name shall be replaced with hyphens in
     *                              accordance with RFC 2141.
     *   urn:domain-name:service:serviceType:ver - search for any service of this type. Where domain-name (a Vendor
     *                              Domain Name), serviceType and ver are defined by the UPnP vendor and ver specifies
     *                              the highest specifies the highest supported version of the service type. Period
     *                              characters in the Vendor Domain Name shall be replaced with hyphens in accordance
     *                              with RFC 2141.
     *
     * Single URI.
     * Required in [SEARCH]
     *
     * @see NotificationType
     */
    SearchTarget("ST"),

    /**
     * Field value shall begin with the following “product tokens” (defined by HTTP/1.1):
     *  - identifier of the operating system in the form OS name/OS version,
     *  - the UPnP version and shall be UPnP/2.0, and
     *  - identifier of the product using the form product name/product version.
     * For example, “USER-AGENT: unix/5.1 UPnP/2.0 MyProduct/1.0”.
     *
     * String. Specified by UPnP vendor.
     *
     * Allowed in [SEARCH]
     */
    UserAgent("USER-AGENT"),

    /**
     * A control point can request that a device replies to a TCP port on the control point. When this header
     * is present it identifies the TCP port on which the device can reply to the search. If a control point sends the
     * TCPPORT.UPNP.ORG header field, its field value shall be an ASCII encoded integer, decimal, without leading
     * zeros (leading zeroes, if present, shall be ignored by the recipient), in the range 49152-65535 (RFC 4340).
     * The device shall respond to unicast M-SEARCH messages similar to sending the response to the originating
     * UDP port except that the notification messages are sent to the advertised TCPPORT.UPNP.ORG port over
     * TCP instead of UDP.
     *
     * Allowed in [SEARCH]
     */
    TcpPort("TCPPORT.UPNP.ORG"),

    /**
     * The friendly name of the control point. The friendly name is vendor specific. When Device
     * Protection is implemented the cpfn.upnp.org shall be the same as the <Name> of Device Protection unless the
     * Device Protection <Alias> is defined, in which case it shall use the <Alias>.
     *
     * Required in [SEARCH]
     */
    ControlPointFriendlyName("CPFN.UPNP.ORG"),

    /**
     * UUID of the control point. When the control point is implemented in a UPnP device it is recommended
     * to use the UDN of the co-located UPnP device. When implemented, all specified requirements for uuid usage
     * in devices also apply for control points.See section 1.1.4. Note that when Device Protection is implemented
     * the CPUUID.UPNP.ORG shall be the same as the uuid used in Device Protection.
     * Allowed in [SEARCH]
     */
    ControlPointUUID(""),
    /**
     * Field value contains date when response was generated. “rfc1123-date” as defined in RFC
     * 2616.
     *
     * Recommended in [SEARCH]
     */
    Date("DATE");

    companion object {
        const val SEARCH = "M-SEARCH * HTTP/1.1"
        const val NOTIFY = "NOTIFY * HTTP/1.1"
        const val OK = "HTTP/1.1 200 OK"
        const val NOTIFY_ALIVE = "ssdp:alive"
        const val NOTIFY_BYE = "ssdp:byebye"
        const val NOTIFY_UPDATE = "ssdp:update"
        const val SSDP_MAN = "\"ssdp:discover\""

        /**
         * Translates [hdr] string value into a proper [Headers] item or return null if the corresponding
         * item was not found
         */
        private fun fromString(hdr: String): Headers? = entries.firstOrNull { it.value == hdr }

        /**
         * Parses a raw [headerString] of format "key:val" into a [Pair] containing [Headers] item and its value.
         */
        @Throws(IllegalArgumentException::class)
        fun parse(headerString: String): Pair<Headers, String> {
            val separator = headerString.indexOf(':')
            if(separator == -1) throw IllegalArgumentException("Invalid header format: $headerString")

            val key = headerString.substring(0, separator).trim()
            val value = headerString.substring(separator + 1).trim()

            fromString(key)
                ?.let { hdr -> return hdr to value }
                ?:throw IllegalArgumentException("Unknown header: $headerString")
        }
    }
}