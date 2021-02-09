package com.github.madwareru.ktlox.visitors

import com.github.madwareru.ktlox.ast.ASTNode
import com.github.madwareru.ktlox.print

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