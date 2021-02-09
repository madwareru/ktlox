package com.github.madwareru.ktlox

import com.github.madwareru.ktlox.ast.*

/*
 * Grammar:
 *
 * expression -> booleanTerms
 *
 * booleanTerms -> booleanFactors ("or" booleanFactors)*
 *
 * booleanFactors -> booleanEq ("and" booleanEqs)*
 *
 * booleanEqs -> booleanComparisons (("!=" | "==") booleanComparisons)*
 *
 * booleanComparisons -> arithmeticTerms (("<" | "<=" | ">=" | ">") arithmeticTerms)*
 *
 * arithmeticTerms -> arithmeticFactors (("+" | "-") arithmeticFactors)*
 *
 * arithmeticFactors -> unary (("*" | "/") unary)*
 *
 * unary ->
 *     ("!" | "-") unary
 *     | primary
 *
 * primary ->
 *     NUMBER_LITERAL
 *     | STRING_LITERAL
 *     | BOOLEAN_LITERAL
 *     | identifier
 *     | "nil"
 *     | "(" expression ")"
 */

class Parser(private val tokens: List<Token>) {
    private var currentOffset = 0

    private val currentToken get() =
        if (currentOffset < tokens.size) {
            some { tokens[currentOffset] }
        } else {
            none()
        }

    private fun isAtEnd() =
        currentToken.isNone ||
        currentToken.unwrap().type == TokenType.Eof

    private fun advance() {
        if (!isAtEnd()) {
            currentOffset++
        }
    }

    private fun match2(vararg tokenTypes: TokenType): Option<Token> {
        return currentToken.map {
            for (tokenType in tokenTypes) {
                if (it.type == tokenType) {
                    return some { it }
                }
            }
            return none()
        }
    }

    private fun match(tokenTypes: Array<out TokenType>): Option<Token> {
        return currentToken.map {
            for (tokenType in tokenTypes) {
                if (it.type == tokenType) {
                    return some { it }
                }
            }
            return none()
        }
    }

    private inline fun separatedSeries(
        vararg tokenTypes: TokenType,
        subElementCtr: () -> Result<ASTNode.Expression, String>
    ): Result<ASTNode.Expression, String> {
        var expr = subElementCtr()
        when(expr) {
            is Result.Err -> return err { (expr as Result.Err).reason }
            is Result.Ok -> {
                var matched = match(tokenTypes)
                while (!expr.isErr && matched is Option.Some) {
                    val op = matched.value
                    advance()
                    expr = subElementCtr().map { rhs -> op.asBinExp { (expr as Result.Ok).value to rhs } }
                    matched = match(tokenTypes)
                }
            }
        }
        return expr
    }

    fun parse(): Result<ASTNode, String> = expression()

    private fun expression(): Result<ASTNode.Expression, String> {
        return booleanTerms()
    }

    private fun booleanTerms(): Result<ASTNode.Expression, String> {
        return separatedSeries(TokenType.BooleanOperator.Or) { booleanFactors() }
    }

    private fun booleanFactors(): Result<ASTNode.Expression, String> {
        return separatedSeries(TokenType.BooleanOperator.And) { booleanEqs() }
    }

    private fun booleanEqs(): Result<ASTNode.Expression, String> {
        return separatedSeries(
            TokenType.BooleanOperator.Equal,
            TokenType.BooleanOperator.NotEqual
        ) { booleanComparisons() }
    }

    private fun booleanComparisons(): Result<ASTNode.Expression, String> {
        return separatedSeries(
            TokenType.BooleanOperator.Lesser, TokenType.BooleanOperator.LesserEqual,
            TokenType.BooleanOperator.GreaterEqual, TokenType.BooleanOperator.Greater
        ) { arithmeticTerms() }
    }

    private fun arithmeticTerms(): Result<ASTNode.Expression, String> {
        return separatedSeries(
            TokenType.ArithmeticOperator.Plus,
            TokenType.ArithmeticOperator.Minus
        ) { arithmeticFactors() }
    }

    private fun arithmeticFactors(): Result<ASTNode.Expression, String> {
        return separatedSeries(
            TokenType.ArithmeticOperator.Multiplication,
            TokenType.ArithmeticOperator.Division
        ) { unary() }
    }

    private fun unary(): Result<ASTNode.Expression, String> {
        return when (val unaryOpToken = match2(TokenType.BooleanOperator.Not, TokenType.ArithmeticOperator.Minus)) {
            is Option.Some -> {
                advance()
                unary().map { rhs -> unaryOpToken.value.asUnaryExp { rhs } }
            }
            else -> primary()
        }
    }

    private fun primary(): Result<ASTNode.Expression, String> {
        if (isAtEnd() || currentToken.unwrap().type == TokenType.Eof) {
            return err { "Reached end of file" }
        }
        return currentToken
            .castToErr("Reached end of file")
            .flatMap { when (it.type) {
                TokenType.Literal.String,
                TokenType.Literal.Number,
                TokenType.Literal.Boolean,
                TokenType.Literal.Nil,
                TokenType.Identifier -> { advance(); ok{ literalExp { it } } }
                TokenType.Brace.LParen -> {
                    advance()
                    expression().flatMap { expr ->
                        if(currentToken.isNone || currentToken.unwrap().type != TokenType.Brace.RParen) {
                            val token = currentToken.unwrap()
                            err {
                                "Line ${token.startPosition.row}, column ${token.startPosition.row}:\n"+
                                "Expected: ${TokenType.Brace.RParen.print()}, but found ${token.type.print()}"
                            }
                        } else {
                            advance()
                            ok { groupingExp { expr } }
                        }
                    }
                }
                else -> {
                    val token = currentToken.unwrap()
                    val expectedTokens = arrayOf(
                        TokenType.Literal.String,
                        TokenType.Literal.Number,
                        TokenType.Literal.Boolean,
                        TokenType.Literal.Nil,
                        TokenType.Identifier,
                        TokenType.Brace.LParen
                    ).joinToString { tokenType -> tokenType.print() }
                    err {
                        "Line ${token.startPosition.row}, column ${token.startPosition.row}:\n"+
                        "Expected one of: [$expectedTokens], but found ${token.type.print()}"
                    }
                }
            }
        }
    }
}