/*? if !forge {*/
package tests

import com.calculatorsteam.dynamicpack.util.PackUtil
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class PackUtilTest {

    private val projectRoot: File = File(System.getProperty("user.dir"))
        .let { dir ->
            generateSequence(dir) { it.parentFile }
                .firstOrNull { File(it, "settings.gradle.kts").exists() }
                ?: dir
        }

    private fun resourceFile(name: String): File =
        File(projectRoot, "src/test/kotlin/tests_files/$name")

    @Test
    fun testDir() {
        PackUtil.openPackFileSystem(resourceFile("filedir")) { path: Path ->
            val resolve = path.resolve("file.txt")
            println(Files.readString(resolve))
        }
    }

    @Test
    fun testZip() {
        PackUtil.openPackFileSystem(resourceFile("filezip.zip")) { path: Path ->
            val resolve = path.resolve("file.txt")
            println(Files.readString(resolve))
        }
    }
}
/*?}*/