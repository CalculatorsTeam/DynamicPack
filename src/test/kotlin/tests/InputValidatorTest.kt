/*? if !forge {*/
package tests

import com.calculatorsteam.dynamicpack.InputValidator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class InputValidatorTest {

    @Test
    fun testContentId() {
        assertFalse(InputValidator.isDynamicContentIdValid(""))
        assertFalse(InputValidator.isDynamicContentIdValid(" "))
        assertFalse(InputValidator.isDynamicContentIdValid("  "))
        assertFalse(InputValidator.isDynamicContentIdValid("   32"))
        assertFalse(InputValidator.isDynamicContentIdValid("test\ntest"))

        assertTrue(InputValidator.isDynamicContentIdValid("__"))
        assertTrue(InputValidator.isDynamicContentIdValid("_-"))
        assertTrue(InputValidator.isDynamicContentIdValid("pack:megapack"))
        assertTrue(InputValidator.isDynamicContentIdValid("1234567890"))
        assertTrue(InputValidator.isDynamicContentIdValid("01"))
        assertTrue(InputValidator.isDynamicContentIdValid("test_pack"))
        assertTrue(InputValidator.isDynamicContentIdValid("super:mega_puper:"))
    }

    @Test
    fun testRemoteName() {
        assertFalse(InputValidator.isDynamicPackNameValid("\n"))
        assertTrue(InputValidator.isDynamicPackNameValid("__"))
    }

    @Test
    fun testUrls() {
        assertTrue(InputValidator.isUrlValid("https://google.com"))
        assertTrue(InputValidator.isUrlValid("https://324234.github.io/"))
        assertTrue(InputValidator.isUrlValid("https://32-4234.github.io/"))
        assertTrue(InputValidator.isUrlValid("https://google.com/f"))
        assertTrue(
            InputValidator.isUrlValid(
                "https://google.net/fi/1/%s_1234567890-+.json?x=81723731+343+1"
            )
        )
        assertFalse(InputValidator.isUrlValid("https://google.com`"))
        assertFalse(InputValidator.isUrlValid("https://google.com "))
        assertFalse(InputValidator.isUrlValid("https://google.com /"))
        assertFalse(InputValidator.isUrlValid("https://google.com*!@#$%^&*()"))
    }

    @Test
    fun testPaths() {
        // корректный кейс
        InputValidator.throwIsPathInvalid("/file/p.txt")

        // некорректные
        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid("") }
        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid(" ") }
        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid("!/file/p.txt") }
        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid(null) }
        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid("\$@#") }
        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid("!\"/file/p.txt") }
        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid("!/fil*&^%544e/p.txt") }

        // случай с рандомными байтами
        assertThrows(SecurityException::class.java) {
            val b = ByteArray(128).apply { Random().nextBytes(this) }
            InputValidator.throwIsPathInvalid(String(b))
        }

        try {
            val b = ByteArray(128).apply { Random().nextBytes(this) }
            InputValidator.throwIsPathInvalid(String(b))
        } catch (e: Exception) {
            println(e)
        }

        assertThrows(SecurityException::class.java) { InputValidator.throwIsPathInvalid("~") }

        assertThrows(SecurityException::class.java) {
            InputValidator.throwIsPathInvalid(
                "()*)*&" + "Y".repeat(300) + ")()*)*&" + "Y".repeat(300)
            )
        }
    }
}
/*?}*/