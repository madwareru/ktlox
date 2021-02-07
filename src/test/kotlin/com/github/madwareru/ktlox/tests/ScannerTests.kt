package com.github.madwareru.ktlox.tests

import com.github.madwareru.ktlox.Scanner
import com.github.madwareru.ktlox.TokenType
import com.github.madwareru.ktlox.print
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
        )
    )
}