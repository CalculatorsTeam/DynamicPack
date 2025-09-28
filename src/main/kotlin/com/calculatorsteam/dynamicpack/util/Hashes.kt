package com.calculatorsteam.dynamicpack.util

import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

object Hashes {
    @JvmStatic
    @Throws(IOException::class)
    fun sha1sum(file: File): String = sha1sum(file.toPath())

    @JvmStatic
    @Throws(IOException::class)
    fun sha1sum(path: Path): String = Files.newInputStream(path).use { DigestUtils.sha1Hex(it) }

    @JvmStatic
    @Throws(IOException::class)
    fun sha1sum(inputStream: InputStream): String = inputStream.use { DigestUtils.sha1Hex(it) }

    @JvmStatic
    fun sha1sum(bytes: ByteArray): String = DigestUtils.sha1Hex(bytes)
}