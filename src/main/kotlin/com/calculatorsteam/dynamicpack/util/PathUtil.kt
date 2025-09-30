package com.calculatorsteam.dynamicpack.util

import com.calculatorsteam.dynamicpack.util.log.FileLog
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.*

/**
 * Utils for working with [Path] and [File].
 */
object PathUtil {

    /** Delete path-file and log */
    @JvmStatic
    @Throws(IOException::class)
    fun delete(path: Path?) {
        val p = path ?: return
        if (Files.exists(p)) {
            Files.delete(p)
            FileLog.deleted(p)
        }
    }

    /** Create path-file and log */
    @JvmStatic
    @Throws(IOException::class)
    fun createFile(path: Path?) {
        val p = path ?: throw IOException("Path is null")
        Files.createFile(p)
        FileLog.created(p)
    }

    @JvmStatic
    fun listFiles(file: File?): Array<File>? = file?.listFiles()

    /** Move a file source to dest place */
    @JvmStatic
    @Throws(IOException::class)
    fun moveFile(source: File?, dest: File?) {
        val s = source ?: throw IOException("Source file is null")
        val d = dest ?: throw IOException("Destination file is null")
        Files.move(s.toPath(), d.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    /** Extract a zipFilePath to dir */
    @JvmStatic
    @Throws(Exception::class)
    fun unzip(zipFilePath: File?, dir: File?) {
        val z = zipFilePath ?: throw IOException("Zip file is null")
        val d = dir ?: throw IOException("Destination dir is null")

        PackUtil.openPackFileSystem(z) { zip ->
            val buffer = mutableSetOf<String>()
            walkScan(buffer, zip)
            buffer.forEach { relative ->
                val path = zip.resolve(relative)
                val toPath = d.toPath().resolve(relative)
                createDirsToFile(toPath)
                Files.copy(path, toPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    /** Force delete a directory */
    @JvmStatic
    fun recursiveDeleteDirectory(file: File?) {
        val f = file ?: return
        try {
            if (!f.isDirectory) throw RuntimeException("File not a directory.")
            FileUtils.deleteDirectory(f)
            FileLog.deleted(f.toPath())
        } catch (e: IOException) {
            throw RuntimeException("Exception while recursive delete dir $f", e)
        }
    }

    private fun nioIsDirExistsAndEmpty(path: Path?): Boolean {
        val p = path ?: return false
        if (Files.isDirectory(p)) {
            Files.list(p).use { return it.count() == 0L }
        }
        return false
    }

    /** Delete path and remove empty parent dirs */
    @JvmStatic
    @Throws(IOException::class)
    fun nioSmartDelete(toDel: Path?) {
        val p = toDel ?: return
        FileLog.deleted(p)
        val par = p.parent
        Files.deleteIfExists(p)
        if (par != null && nioIsDirExistsAndEmpty(par)) {
            nioSmartDelete(par)
        }
    }

    /** Write a text to path */
    @JvmStatic
    fun nioWriteText(path: Path?, text: String) {
        val p = path ?: throw IOException("Path is null")
        try {
            if (Files.exists(p) && !Files.isRegularFile(p)) {
                throw SecurityException("Try to write text to a not regular file.")
            }
            Files.deleteIfExists(p)
            Files.writeString(p, text, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
        } catch (e: IOException) {
            throw RuntimeException("nioWriteText exception!", e)
        }
    }

    /** Read text from file */
    @JvmStatic
    fun nioReadText(path: Path?): String {
        val p = path ?: throw IOException("Path is null")
        try {
            if (!Files.exists(p) || Files.isDirectory(p)) {
                throw RuntimeException("This is not a file. Not found or directory.")
            }
            return Files.readString(p)
        } catch (e: IOException) {
            throw RuntimeException("nioReadText exception!", e)
        }
    }

    /** If paths parent not exists, create dirs to file */
    @JvmStatic
    @Throws(IOException::class)
    fun createDirsToFile(path: Path?) {
        val p = path ?: return
        val par = p.parent
        if (par != null && !Files.exists(par)) {
            Files.createDirectories(par)
        }
    }

    /** Fill buffer with paths to all files (recursively). */
    @JvmStatic
    fun walkScan(buffer: MutableSet<String>, path: Path?) {
        val p = path ?: return
        try {
            Files.walk(p, Int.MAX_VALUE).use { entries ->
                entries.forEach { child ->
                    if (!Files.isDirectory(child)) {
                        buffer.add(p.relativize(child).toString())
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Exception while walkScan", e)
        }
    }

    /** If path exists and is a file */
    @JvmStatic
    fun isPathFileExists(path: Path?): Boolean =
        path != null && Files.exists(path) && !Files.isDirectory(path)

    @JvmStatic
    @Throws(IOException::class)
    fun readString(inputStream: InputStream): String =
        inputStream.readAllBytes().toString(StandardCharsets.UTF_8)

    @JvmStatic
    @Throws(IOException::class)
    fun readString(path: Path?): String {
        val p = path ?: throw IOException("Path is null")
        return Files.readString(p, StandardCharsets.UTF_8)
    }
}