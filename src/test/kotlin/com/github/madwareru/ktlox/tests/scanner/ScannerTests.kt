package com.github.madwareru.ktlox.tests.scanner

import com.github.madwareru.ktlox.Scanner
import com.github.madwareru.ktlox.TokenType
import com.github.madwareru.ktlox.LoxValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScannerTests {
    @Test
    fun testScannerCases() {
        fun scanString(source: String) = Scanner(source)
            .scannedTokensIgnoringCommentsAndWhitespaces
            .map { it.type }
            .toTypedArray()

        for(case in TestCases.database) {
            val (source, expectation) = case
            val scanned = scanString(source)
            assertEquals(expectation.size, scanned.size, "for \"$source\": Incorrect result size")
            val zipped = expectation.zip(scanned)
            for(i in zipped.indices) {
                val (expected, actual) = zipped[i]
                assertEquals(expected, actual, "for \"$source\": Incorrect token at position $i")
            }
        }
    }

    @Test
    fun testTokenValues() {
        val tokens = Scanner(""" 123.12 10 false true nil "string" for fooBar """)
            .scannedTokensIgnoringCommentsAndWhitespaces

        when (val v = tokens[0].value) {
            is LoxValue.Number -> assertEquals(123.12, v.value)
            else -> assert(false)
        }

        when (val v = tokens[1].value) {
            is LoxValue.Number -> assertEquals(10.0, v.value)
            else -> assert(false)
        }

        when (val v = tokens[2].value) {
            is LoxValue.Boolean -> assertEquals(false, v.value)
            else -> assert(false)
        }

        when (val v = tokens[3].value) {
            is LoxValue.Boolean -> assertEquals(true, v.value)
            else -> assert(false)
        }

        assert(tokens[4].value is LoxValue.NilLiteral)

        when (val v = tokens[5].value) {
            is LoxValue.String -> assertEquals("string", v.value)
            else -> assert(false)
        }

        assert(tokens[6].value is LoxValue.None)

        when (val v = tokens[7].value) {
            is LoxValue.IdentifierName -> assertEquals("fooBar", v.value)
            else -> assert(false)
        }
    }
}

object TestCases {
    val database = arrayOf(
        "2+2" to arrayOf(
            TokenType.Literal.Number,
            TokenType.ArithmeticOperator.Plus,
            TokenType.Literal.Number,
            TokenType.Eof
        ),
        "(2+2)/*+2+2+2*/+2" to arrayOf(
            TokenType.Brace.LParen,
            TokenType.Literal.Number,
            TokenType.ArithmeticOperator.Plus,
            TokenType.Literal.Number,
            TokenType.Brace.RParen,
            TokenType.ArithmeticOperator.Plus,
            TokenType.Literal.Number,
            TokenType.Eof
        ),
        """println("Hello world")""" to arrayOf(
            TokenType.Identifier,
            TokenType.Brace.LParen,
            TokenType.Literal.String,
            TokenType.Brace.RParen,
            TokenType.Eof
        )
    )
}
