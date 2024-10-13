package pro.leandev.core

import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

interface Logging

inline fun <reified T : Logging> T.log(): Logger = getLogger(T::class.java)
