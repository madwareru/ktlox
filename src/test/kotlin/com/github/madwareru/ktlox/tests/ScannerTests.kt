package com.github.madwareru.ktlox.tests

import com.github.madwareru.ktlox.Scanner
import com.github.madwareru.ktlox.print
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ScannerTests {
    @Test
    fun testScannerCases() {
        fun scanString(source: String) = Scanner(source)
            .scannedTokensIgnoringCommentsAndWhitespaces
            .map { it.type.print() }
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
            "NUMBER_LITERAL",
            "PLUS_OPERATOR",
            "NUMBER_LITERAL",
            "EOF"
        ),
        "(2+2)/*+2+2+2*/+2" to arrayOf(
            "BRACE_L_PAREN",
            "NUMBER_LITERAL",
            "PLUS_OPERATOR",
            "NUMBER_LITERAL",
            "BRACE_R_PAREN",
            "PLUS_OPERATOR",
            "NUMBER_LITERAL",
            "EOF"
        )
    )
}