package com.github.madwareru.ktlox.tests.parser

import com.github.madwareru.ktlox.*
import com.github.madwareru.ktlox.visitors.ExpressionEvaluatorVisitor
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
            val sExpr = parsed.unwrap().acceptVisitor(ExpressionEvaluatorVisitor())
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
        "2+2" to ok<LoxValue, String> { LoxValue.Number(4.0) },
        "(2.0 + 2.0) * 2.0" to ok { LoxValue.Number(8.0) },
        "2.0 + 2.0 * 2.0" to ok { LoxValue.Number(6.0) },
        "2.0 - 6.0 * 2.0" to ok { LoxValue.Number(-10.0) },
        "2.0 - 2.0 - 2.0 - 2.0" to ok { LoxValue.Number(-4.0) },
        "2 > 3" to ok { LoxValue.Boolean(false) },
        "\"Hello \" + \"world\"" to ok { LoxValue.String("Hello world") },
    )
}
