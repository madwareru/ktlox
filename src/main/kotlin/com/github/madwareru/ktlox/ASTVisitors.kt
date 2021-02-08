package com.github.madwareru.ktlox

import java.io.InvalidClassException
import java.lang.StringBuilder

class SExpressionPrinterVisitor : (ASTNode) -> String {
    override fun invoke(expression: ASTNode) : String {
        return when (expression) {
            is ASTNode.Expression.Binary -> parenthesize(
                expression.opToken.type.print(),
                expression.lhs,
                expression.rhs
            )
            is ASTNode.Expression.Grouping -> parenthesize("group", expression.expression)
            is ASTNode.Expression.Literal -> expression.literalToken.lexeme
            is ASTNode.Expression.Unary -> parenthesize(expression.opToken.type.print(), expression.rhs)
        }
    }
    private fun parenthesize(name: String, vararg expressions: ASTNode.Expression): String {
        val sb = StringBuilder()
        sb.append('(').append(name)
        expressions.forEach {
            sb.append(" ").append(it.acceptVisitor(this))
        }
        sb.append(')')
        return sb.toString()
    }
}

class ExpressionCalculatorVisitor : (ASTNode) -> Double {
    override fun invoke(expression: ASTNode): Double {
        return when (expression) {
            is ASTNode.Expression.Binary -> when (expression.opToken.type) {
                TokenType.ArithmeticOperator.Plus ->
                    expression.lhs.acceptVisitor(this) + expression.rhs.acceptVisitor(this)
                TokenType.ArithmeticOperator.Minus ->
                    expression.lhs.acceptVisitor(this) - expression.rhs.acceptVisitor(this)
                TokenType.ArithmeticOperator.Multiplication ->
                    expression.lhs.acceptVisitor(this) * expression.rhs.acceptVisitor(this)
                TokenType.ArithmeticOperator.Division ->
                    expression.lhs.acceptVisitor(this) / expression.rhs.acceptVisitor(this)
                else -> throw InvalidClassException("non arithmetic operation found")
            }
            is ASTNode.Expression.Grouping -> expression.expression.acceptVisitor(this)
            is ASTNode.Expression.Literal -> when (val v = expression.literalToken.value) {
                is TokenValue.Number -> v.value
                else -> throw InvalidClassException("found non numerical token")
            }
            is ASTNode.Expression.Unary -> when (expression.opToken.type) {
                TokenType.ArithmeticOperator.Minus -> -expression.rhs.acceptVisitor(this)
                else -> throw InvalidClassException("found unsupported unary operator")
            }
        }
    }
}