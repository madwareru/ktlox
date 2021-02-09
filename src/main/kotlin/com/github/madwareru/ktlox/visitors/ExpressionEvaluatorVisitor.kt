package com.github.madwareru.ktlox.visitors

import com.github.madwareru.ktlox.*
import com.github.madwareru.ktlox.ast.ASTNode

class ExpressionEvaluatorVisitor : (ASTNode) -> Result<LoxValue, String> {
    override fun invoke(astNode: ASTNode): Result<LoxValue, String> =
        when (astNode) {
            is ASTNode.Expression.Literal -> ok { astNode.literalToken.value }
            is ASTNode.Expression.Unary -> evaluateUnaryExpression(astNode)
            is ASTNode.Expression.Binary -> evaluateBinaryExpression(astNode)
            is ASTNode.Expression.Grouping -> astNode.expression.acceptVisitor(this)
        }

    private fun evaluateBinaryExpression(binaryExpression: ASTNode.Expression.Binary) =
        binaryExpression.lhs.acceptVisitor(this).flatMap { lhsRes ->
            val tokenStart = binaryExpression.opToken.startPosition
            when (binaryExpression.opToken.type) {
                TokenType.BooleanOperator.And,
                TokenType.BooleanOperator.Or -> {
                    when (lhsRes) {
                        !is LoxValue.Boolean -> err {
                            "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                                    "Left operand is not a boolean"
                        }
                        else -> when {
                            lhsRes.value && binaryExpression.opToken.type == TokenType.BooleanOperator.Or -> ok {
                                LoxValue.Boolean(true)
                            }
                            !lhsRes.value && binaryExpression.opToken.type == TokenType.BooleanOperator.And -> ok {
                                LoxValue.Boolean(false)
                            }
                            else -> binaryExpression.rhs.acceptVisitor(this).flatMap { rhsRes ->
                                evaluateBooleanCombinator(lhsRes, binaryExpression.opToken, rhsRes)
                            }
                        }
                    }
                }
                else -> binaryExpression.rhs.acceptVisitor(this).flatMap { rhsRes ->
                    when (binaryExpression.opToken.type) {
                        is TokenType.ArithmeticOperator ->
                            evaluateArithmeticBinOp(lhsRes, binaryExpression.opToken, rhsRes)

                        TokenType.BooleanOperator.Equal,
                        TokenType.BooleanOperator.NotEqual ->
                            evaluateEqualityOperator(lhsRes, binaryExpression.opToken, rhsRes)

                        TokenType.BooleanOperator.Lesser,
                        TokenType.BooleanOperator.LesserEqual,
                        TokenType.BooleanOperator.GreaterEqual,
                        TokenType.BooleanOperator.Greater ->
                            evaluateComparisonCombinator(lhsRes, binaryExpression.opToken, rhsRes)

                        else -> err {
                            "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                                    "Unknown binary operator found: ${binaryExpression.opToken.type.print()}"
                        }
                    }
                }
            }
        }

    private fun evaluateComparisonCombinator(lhs: LoxValue, opToken: Token, rhs: LoxValue) : Result<LoxValue, String> =
        when {
            lhs is LoxValue.Number && rhs is LoxValue.Number -> {
                ok {
                    LoxValue.Boolean(
                        when (opToken.type) {
                            TokenType.BooleanOperator.Lesser -> lhs.value < rhs.value
                            TokenType.BooleanOperator.LesserEqual -> lhs.value <= rhs.value
                            TokenType.BooleanOperator.GreaterEqual -> lhs.value >= rhs.value
                            else -> lhs.value > rhs.value
                        }
                    )
                }
            }
            lhs is LoxValue.String && rhs is LoxValue.String -> {
                ok {
                    LoxValue.Boolean(
                        when (opToken.type) {
                            TokenType.BooleanOperator.Lesser -> lhs.value < rhs.value
                            TokenType.BooleanOperator.LesserEqual -> lhs.value <= rhs.value
                            TokenType.BooleanOperator.GreaterEqual -> lhs.value >= rhs.value
                            else -> lhs.value > rhs.value
                        }
                    )
                }
            }
            lhs is LoxValue.Boolean && rhs is LoxValue.Boolean -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Boolean operands do not support comparisons"
                }
            }
            lhs is LoxValue.IdentifierName || rhs is LoxValue.IdentifierName -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Identifier evaluation is not implemented yet"
                }
            }
            else -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Operand types are not match"
                }
            }
        }

    private fun evaluateEqualityOperator(lhs: LoxValue, opToken: Token, rhs: LoxValue) : Result<LoxValue, String> =
        when {
            lhs is LoxValue.Boolean && rhs is LoxValue.Boolean -> {
                ok {
                    LoxValue.Boolean(
                        if (opToken.type == TokenType.BooleanOperator.Equal) {
                            lhs.value == rhs.value
                        } else {
                            lhs.value != rhs.value
                        }
                    )
                }
            }
            lhs is LoxValue.Number && rhs is LoxValue.Number -> {
                ok {
                    LoxValue.Boolean(
                        if (opToken.type == TokenType.BooleanOperator.Equal) {
                            lhs.value == rhs.value
                        } else {
                            lhs.value != rhs.value
                        }
                    )
                }
            }
            lhs is LoxValue.String && rhs is LoxValue.String -> {
                ok {
                    LoxValue.Boolean(
                        if (opToken.type == TokenType.BooleanOperator.Equal) {
                            lhs.value == rhs.value
                        } else {
                            lhs.value != rhs.value
                        }
                    )
                }
            }
            lhs is LoxValue.IdentifierName || rhs is LoxValue.IdentifierName -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Identifier evaluation is not implemented yet"
                }
            }
            else -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Operand types are not match"
                }
            }
        }

    private fun evaluateBooleanCombinator(lhs: LoxValue, opToken: Token, rhs: LoxValue) : Result<LoxValue, String> =
        when {
            lhs is LoxValue.Boolean && rhs is LoxValue.Boolean -> {
                ok {
                    LoxValue.Boolean(
                        if (opToken.type == TokenType.BooleanOperator.And) {
                            lhs.value && rhs.value
                        } else {
                            lhs.value || rhs.value
                        }
                    )
                }
            }
            else -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Right operand is not a boolean"
                }
            }
        }

    private fun evaluateArithmeticBinOp(lhs: LoxValue, opToken: Token, rhs: LoxValue) : Result<LoxValue, String> =
        when {
            lhs is LoxValue.Number && rhs is LoxValue.Number -> {
                ok {
                    LoxValue.Number(
                        when (opToken.type) {
                            TokenType.ArithmeticOperator.Plus -> lhs.value + rhs.value
                            TokenType.ArithmeticOperator.Minus -> lhs.value - rhs.value
                            TokenType.ArithmeticOperator.Multiplication -> lhs.value * rhs.value
                            else -> lhs.value / rhs.value
                        }
                    )
                }
            }
            lhs is LoxValue.String && rhs is LoxValue.String -> {
                val tokenStart = opToken.startPosition
                when (opToken.type) {
                    TokenType.ArithmeticOperator.Plus -> ok {
                        LoxValue.String(lhs.value + rhs.value)
                    }
                    else -> err {
                        "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                                "String operands do not support arithmetic operators with except to concatenation"
                    }
                }
            }
            lhs is LoxValue.Boolean && rhs is LoxValue.Boolean -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Boolean operands do not support arithmetic operators"
                }
            }
            lhs is LoxValue.IdentifierName || rhs is LoxValue.IdentifierName -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Identifier evaluation is not implemented yet"
                }
            }
            else -> {
                val tokenStart = opToken.startPosition
                err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Operand types are not match"
                }
            }
        }

    private fun evaluateUnaryExpression(unaryExpression: ASTNode.Expression.Unary) =
        unaryExpression.rhs.acceptVisitor(this).flatMap { rhsRes ->
            val tokenStart = unaryExpression.opToken.startPosition
            when (unaryExpression.opToken.type) {
                TokenType.ArithmeticOperator.Minus -> {
                    when (rhsRes) {
                        is LoxValue.Number -> ok { LoxValue.Number(-rhsRes.value) }
                        is LoxValue.IdentifierName -> err {
                            "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                                    "Identifier evaluation is not implemented yet"
                        }
                        else -> err {
                            "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                                    "Operand is not a number"
                        }
                    }
                }
                TokenType.BooleanOperator.Not -> {
                    when (rhsRes) {
                        is LoxValue.Boolean -> ok { LoxValue.Boolean(!rhsRes.value) }
                        is LoxValue.IdentifierName -> err {
                            "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                                    "Identifier evaluation is not implemented yet"
                        }
                        else -> err {
                            "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                                    "Operand is not a boolean"
                        }
                    }
                }
                else -> err {
                    "[${tokenStart.row},${tokenStart.col}] Parse error:\n" +
                            "Unknown unary operator found: ${unaryExpression.opToken.type.print()}"
                }
            }
        }
}