package com.github.madwareru.ktlox.visitors

import com.github.madwareru.ktlox.LoxValue
import com.github.madwareru.ktlox.TokenType
import com.github.madwareru.ktlox.ast.ASTNode
import java.io.InvalidClassException

class ExpressionCalculatorVisitor : (ASTNode) -> Double {
    override fun invoke(expression: ASTNode): Double {
        return when (expression) {
            is ASTNode.Expression.Literal -> when (val v = expression.literalToken.value) {
                is LoxValue.Number -> v.value
                else -> throw InvalidClassException("found non numerical token")
            }
            is ASTNode.Expression.Unary -> calcUnaryExpression(expression)
            is ASTNode.Expression.Binary -> calcBinaryExpression(expression)
            is ASTNode.Expression.Grouping -> expression.expression.acceptVisitor(this)
        }
    }

    private fun calcUnaryExpression(expression: ASTNode.Expression.Unary) =
        when (expression.opToken.type) {
            TokenType.ArithmeticOperator.Minus -> -expression.rhs.acceptVisitor(this)
            else -> throw InvalidClassException("found unsupported unary operator")
        }

    private fun calcBinaryExpression(expression: ASTNode.Expression.Binary) =
        when (expression.opToken.type) {
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
}
