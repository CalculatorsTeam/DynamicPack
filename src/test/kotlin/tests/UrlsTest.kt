/*? if !forge {*/
package tests

import com.calculatorsteam.dynamicpack.DynamicPackMod
import com.calculatorsteam.dynamicpack.util.log.Out
import com.calculatorsteam.dynamicpack.util.Urls
import org.junit.jupiter.api.Test

class UrlsTest {

    @Test
    fun valid() {
        Out.USE_SOUT = true
        DynamicPackMod.addAllowedHosts("ubuntu.com", this)

        repeat(100) {
            Urls.downloadFileToTemp(
                "https://cdn.modrinth.com/data/UQBo9Yss/versions/fYk7cLrO/BetterTables.zip",
                "test",
                ".temp",
                32_965_550_080L, // читается лучше с подчёркивателями
                null
            )
        }
    }
}
/*?}*/