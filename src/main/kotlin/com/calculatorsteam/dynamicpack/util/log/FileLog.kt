package com.calculatorsteam.dynamicpack.util.log

import java.nio.file.Path

/**
 * Notify this class about all changes in files by mod.
 */
object FileLog {
    private const val PREFIX = "[FilesLog] "

    @JvmField
    var logAllChanges: Boolean = false

    fun deleted(path: Path) {
        if (logAllChanges) {
            Out.debug("$PREFIX-deleted: $path")
        }
    }

    fun created(path: Path) {
        if (logAllChanges) {
            Out.debug("$PREFIX+created: $path")
        }
    }

    fun writtenByUrl(path: Path, url: String) {
        if (logAllChanges) {
            Out.debug("$PREFIX=written: ($url)-> $path")
        }
    }
}