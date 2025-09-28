package com.calculatorsteam.dynamicpack.pack.dynamicrepo

import java.nio.file.Path

/**
 * File descriptor in DynamicRepo.
 *
 * @param size size from remote! May be Int.MAX_VALUE if remote deprecated!
 */
data class DynamicFile(
    val url: String,
    val path: String,
    val size: Int,
    val hash: String,
    var downloadedPath: Path? = null
) {
    override fun toString(): String = buildString {
        append("DynamicFile(")
        append("url=").append(url)
        append(", path=").append(path)
        append(", size=").append(size)
        append(", hash=").append(hash)
        append(", tempPath=").append(downloadedPath)
        append(")")
    }
}