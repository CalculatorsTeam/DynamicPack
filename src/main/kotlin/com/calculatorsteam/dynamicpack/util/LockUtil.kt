package com.calculatorsteam.dynamicpack.util

import com.calculatorsteam.dynamicpack.accessor.FilePackResourcesAccessor
import com.calculatorsteam.dynamicpack.util.log.Out
import java.io.File

/**
 * Utils for file locked by other process (fixes crashes in Windows).
 */
object LockUtils {
    private val openedZipFiles: MutableSet<FilePackResourcesAccessor> = mutableSetOf()

    /**
     * Create file finalizer Runnable
     */
    @JvmStatic
    fun createFileFinalizer(file: File): Runnable = Runnable { closeFile(file) }

    @JvmStatic
    fun closeFile(file: File) {
        Out.debug("[PackUtil] closeFile $file")

        val toDelete = mutableSetOf<FilePackResourcesAccessor>()
        // Перебор snapshot множества, чтобы безопасно удалять
        for (openedZipFile in openedZipFiles.toTypedArray()) {
            if (openedZipFile.`dynamicpack$getFile`().equals(file)) {
                openedZipFile.`dynamicpack$close`()
                toDelete += openedZipFile
                Out.debug("[PackUtil] - $openedZipFile")
            }
        }
        openedZipFiles.removeAll(toDelete)
    }

    @JvmStatic
    fun addFileToOpened(resources: FilePackResourcesAccessor) {
        openedZipFiles += resources
    }
}