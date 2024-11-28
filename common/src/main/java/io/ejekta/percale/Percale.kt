package io.ejekta.percale

import java.io.OutputStream
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter


object Percale {
    val LOGGER = Logger.getLogger("Percale").apply {
        useParentHandlers = false

        val formatter: Formatter = object : SimpleFormatter() {
            private val format = "[%1\$tF %1\$tT.%1\$tL] [%2$-7s] %3\$s %n"

            @Synchronized
            override fun format(lr: LogRecord): String {
                return java.lang.String.format(
                    format, Date(lr.millis), lr.level.localizedName,
                    lr.message
                )
            }
        }

        addHandler(object : ConsoleHandler() {
            init {
                setFormatter(formatter)
            }
            override fun setOutputStream(out: OutputStream?) {
                super.setOutputStream(System.out)
            }
        })

    }

    fun syslog(amt: Int, msg: String) {
        LOGGER.info(lp(amt, msg))
    }

    private fun lp(amt: Int, str: String): String {
        return "\t".repeat(amt) + str
    }

}