/*? if !forge {*/
package tests

import com.calculatorsteam.dynamicpack.pack.dynamicrepo.DynamicRepoSyncBuilder
import com.calculatorsteam.dynamicpack.util.log.Out
import com.calculatorsteam.dynamicpack.util.Urls
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SecurityTrustedUrlsTest {

    @Test
    fun test() {
        Out.USE_SOUT = true

        assertThrows(Exception::class.java) {
            Out.println(Urls.parseTextContent("https://google.com", 1732132132))
        }
        assertThrows(Exception::class.java) {
            Urls.parseTextContent("https://modrinth.com.google.com", 1732132132)
        }
        assertThrows(Exception::class.java) {
            Urls.parseTextContent("https://fakemodrinth.com.com", 1732132132)
        }

        assertDoesNotThrow {
            DynamicRepoSyncBuilder.getAndCheckPath("assets", "minecraft/lang/en_us.json")
            DynamicRepoSyncBuilder.getAndCheckPath("assets/", "minecraft/lang/en_us.json")
            DynamicRepoSyncBuilder.getAndCheckPath("assets", "/minecraft/lang/en_us.json")
            DynamicRepoSyncBuilder.getAndCheckPath("assets", "/minecraft/lang/en_us.json")
            DynamicRepoSyncBuilder.getAndCheckPath("/assets/", "///minecraft/lang/en_us.json")
        }

        repeat(3) {
            assertThrows(Exception::class.java) {
                DynamicRepoSyncBuilder.getAndCheckPath("assets/../../../../", "minecraft/lang/en_us.json")
            }
        }
    }
}
/*?}*/