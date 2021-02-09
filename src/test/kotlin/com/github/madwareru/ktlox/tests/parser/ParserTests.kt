package com.github.madwareru.ktlox.tests.parser

import com.github.madwareru.ktlox.*
import com.github.madwareru.ktlox.visitors.ExpressionCalculatorVisitor
import com.github.madwareru.ktlox.visitors.SExpressionPrinterVisitor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParserTests {
    @Test
    fun testSExprCases() {
        fun parseString(source: String) = Parser(Scanner(source).scannedTokensIgnoringCommentsAndWhitespaces).parse()

        for(case in TestCases.sExprDatabase) {
            val (source, expectation) = case
            val parsed = parseString(source)
            assert(!parsed.isErr)
            val sExpr = parsed.unwrap().acceptVisitor(SExpressionPrinterVisitor())
            assertEquals(expectation, sExpr, "for \"$source\":")
        }
    }

    @Test
    fun testCalcCases() {
        fun parseString(source: String) = Parser(Scanner(source).scannedTokensIgnoringCommentsAndWhitespaces).parse()

        for(case in TestCases.calcDatabase) {
            val (source, expectation) = case
            val parsed = parseString(source)
            assert(!parsed.isErr)
            val sExpr = parsed.unwrap().acceptVisitor(ExpressionCalculatorVisitor())
            assertEquals(expectation, sExpr, "for \"$source\":")
        }
    }
}

object TestCases {
    val sExprDatabase = arrayOf(
        "2+2" to "(+ 2 2)",
        "(2.0 + 2.0) * 2.0" to "(* (group (+ 2.0 2.0)) 2.0)",
        "2.0 + 2.0 * 2.0" to "(+ 2.0 (* 2.0 2.0))",
    )

    val calcDatabase = arrayOf(
        "2+2" to 4.0,
        "(2.0 + 2.0) * 2.0" to 8.0,
        "2.0 + 2.0 * 2.0" to 6.0,
        "2.0 - 6.0 * 2.0" to -10.0,
        "2.0 - 2.0 - 2.0 - 2.0" to -4.0,
    )
}
