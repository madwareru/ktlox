package com.github.madwareru.ktlox.ast

import com.github.madwareru.ktlox.Token

sealed class ASTNode {
    sealed class Expression : ASTNode() {
        data class Grouping(val expression: Expression) : Expression()
        data class Binary(val lhs: Expression, val opToken: Token, val rhs: Expression) : Expression()
        data class Literal(val literalToken: Token) : Expression()
        data class Unary(val opToken: Token, var rhs: Expression) : Expression()
    }
    fun<R> acceptVisitor(visitor: (ASTNode) -> R) = visitor(this)
}

inline fun groupingExp(ctr: () -> ASTNode.Expression): ASTNode.Expression = ASTNode.Expression.Grouping(ctr())
inline fun literalExp(ctr: () -> Token): ASTNode.Expression = ASTNode.Expression.Literal(ctr())
inline fun Token.asBinExp(ctr: () -> Pair<ASTNode.Expression, ASTNode.Expression>): ASTNode.Expression {
    val ops = ctr()
    return ASTNode.Expression.Binary(ops.first, this, ops.second)
}
inline fun Token.asUnaryExp(ctr: () -> ASTNode.Expression): ASTNode.Expression {
    val ops = ctr()
    return ASTNode.Expression.Unary(this, ops)
}