package com.github.madwareru.ktlox

import java.lang.StringBuilder

sealed class ASTNode {
    sealed class Expression {
        data class Binary(val lhs: Expression, val opToken: Token, val rhs: Expression) : Expression()
        data class Grouping(val expression: Expression) : Expression()
        data class Literal(val literalToken: Token) : Expression()
        data class Unary(val opToken: Token, var rhs: Expression) : Expression()
        fun<R> acceptVisitor(visitor: (Expression) -> R) = visitor(this)
    }
}

class ExpressionPrinterVisitor : (ASTNode.Expression) -> String {
    override fun invoke(expression: ASTNode.Expression) : String {
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