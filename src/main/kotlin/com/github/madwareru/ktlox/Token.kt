package com.github.madwareru.ktlox

data class CharacterPosition(val row: Int, val col: Int)

data class Token(
    private val owningScanner: Scanner,
    val startPosition: CharacterPosition,
    val endPosition: CharacterPosition,
    val startOffset: Int,
    val endOffset: Int,
    val type: TokenType
) {
    val lexeme: String by lazy { owningScanner.getLexeme(this) }
    val value: TokenValue by lazy {
        when (type) {
            TokenType.Identifier -> TokenValue.IdentifierName(lexeme)
            TokenType.Literal.String -> TokenValue.String(lexeme.trim('"'))
            TokenType.Literal.Number -> TokenValue.Number(lexeme.toDouble())
            TokenType.Literal.Boolean -> TokenValue.Boolean(lexeme.toBoolean())
            TokenType.Literal.Nil -> TokenValue.NilLiteral
            else -> TokenValue.None
        }
    }
}

sealed class TokenValue {
    data class IdentifierName(val value: kotlin.String) : TokenValue()
    data class String(val value: kotlin.String) : TokenValue()
    data class Number(val value: Double) : TokenValue()
    data class Boolean(val value: kotlin.Boolean) : TokenValue()
    object NilLiteral : TokenValue()
    object None : TokenValue()
}

sealed class TokenType {
    object Identifier : TokenType()
    sealed class Error : TokenType() {
        object BadCharacter : Error()
        object UnterminatedString : Error()
    }
    sealed class Literal : TokenType() {
        object String : Literal()
        object Boolean : Literal()
        object Number : Literal()
        object Nil : Literal()
    }
    sealed class Keyword : TokenType() {
        object Class : Keyword()
        object Else : Keyword()
        object Fun : Keyword()
        object For : Keyword()
        object If : Keyword()
        object Print : Keyword()
        object Return : Keyword()
        object Super : Keyword()
        object This : Keyword()
        object Var : Keyword()
        object While : Keyword()
    }
    sealed class Brace : TokenType() {
        object LParen : Brace()
        object RParen : Brace()
        object LCurly : Brace()
        object RCurly : Brace()
    }
    sealed class ArithmeticOperator : TokenType() {
        object Plus : ArithmeticOperator()
        object Minus : ArithmeticOperator()
        object Division : ArithmeticOperator()
        object Multiplication : ArithmeticOperator()
    }
    object AssignmentOperator : TokenType()
    sealed class BooleanOperator : TokenType() {
        object Not : BooleanOperator()
        object NotEqual : BooleanOperator()
        object Lesser : BooleanOperator()
        object LesserEqual : BooleanOperator()
        object Greater : BooleanOperator()
        object GreaterEqual : BooleanOperator()
        object Equal : BooleanOperator()
        object And : BooleanOperator()
        object Or : BooleanOperator()
    }
    sealed class Delimiter : TokenType() {
        object Comma : Delimiter()
        object Dot : Delimiter()
        object Semicolon : Delimiter()
    }
    object SingleLineComment: TokenType()
    object MultiLineComment: TokenType()
    object NewLine: TokenType()
    object WhiteSpace: TokenType()
    object Eof : TokenType()
}

fun Char.matchSingleCharacterToken() : TokenType? =
    when(this) {
        '(', ')', '{', '}' -> matchBraceToken()
        '-', '+', '*' -> matchArithmeticOperatorToken()
        ',', '.', ';' -> matchDelimiterToken()
        else -> null
    }

private fun Char.matchDelimiterToken(): TokenType.Delimiter? =
    when(this) {
        ',' -> TokenType.Delimiter.Comma
        '.' -> TokenType.Delimiter.Dot
        ';' -> TokenType.Delimiter.Semicolon
        else -> null
    }

private fun Char.matchBraceToken(): TokenType.Brace? =
    when(this) {
        '(' -> TokenType.Brace.LParen
        ')' -> TokenType.Brace.RParen
        '{' -> TokenType.Brace.LCurly
        '}' -> TokenType.Brace.RCurly
        else -> null
    }

private fun Char.matchArithmeticOperatorToken(): TokenType.ArithmeticOperator? =
    when(this) {
        // Division is not represented here since '/' could be a part of "//"
        // marking the beginning of a single line comment or even part of "/*"
        // marking the beginning of a multiline comment
        '+' -> TokenType.ArithmeticOperator.Plus
        '-' -> TokenType.ArithmeticOperator.Minus
        '*' -> TokenType.ArithmeticOperator.Multiplication
        else -> null
    }

fun TokenType.print() : String = when(this) {
    TokenType.Identifier -> "IDENTIFIER"
    TokenType.Error.BadCharacter -> "ERROR:BAD_CHAR"
    TokenType.Error.UnterminatedString -> "ERROR:UNTERMINATED_STRING"
    TokenType.NewLine -> "NEWLINE"
    TokenType.WhiteSpace -> "WHITESPACE"
    TokenType.Literal.String -> "STRING_LITERAL"
    TokenType.Literal.Boolean -> "BOOLEAN_LITERAL"
    TokenType.Literal.Number -> "NUMBER_LITERAL"
    TokenType.Literal.Nil -> "NIL_LITERAL"
    TokenType.Keyword.Class -> "KW_CLASS"
    TokenType.Keyword.Else -> "KW_ELSE"
    TokenType.Keyword.Fun -> "KW_FUN"
    TokenType.Keyword.For -> "KW_FOR"
    TokenType.Keyword.If -> "KW_IF"
    TokenType.Keyword.Print -> "KW_PRINT"
    TokenType.Keyword.Return -> "KW_RETURN"
    TokenType.Keyword.Super -> "KW_SUPER"
    TokenType.Keyword.This -> "KW_THIS"
    TokenType.Keyword.Var -> "KW_VAR"
    TokenType.Keyword.While -> "KW_WHILE"
    TokenType.Brace.LParen -> "BRACE_L_PAREN"
    TokenType.Brace.RParen -> "BRACE_R_PAREN"
    TokenType.Brace.LCurly -> "BRACE_L_CURLY"
    TokenType.Brace.RCurly -> "BRACE_L_CURLY"
    TokenType.ArithmeticOperator.Plus -> "PLUS_OPERATOR"
    TokenType.ArithmeticOperator.Minus -> "MINUS_OPERATOR"
    TokenType.ArithmeticOperator.Division -> "DIVISION_OPERATOR"
    TokenType.ArithmeticOperator.Multiplication -> "MULTIPLICATION_OPERATOR"
    TokenType.AssignmentOperator -> "ASSIGNMENT_OPERATOR"
    TokenType.BooleanOperator.Not -> "NOT_OPERATOR"
    TokenType.BooleanOperator.NotEqual -> "NEQ_OPERATOR"
    TokenType.BooleanOperator.Lesser -> "LT_OPERATOR"
    TokenType.BooleanOperator.LesserEqual -> "LTEQ_OPERATOR"
    TokenType.BooleanOperator.Greater -> "GT_OPERATOR"
    TokenType.BooleanOperator.GreaterEqual -> "GTEQ_OPERATOR"
    TokenType.BooleanOperator.Equal -> "EQ_OPERATOR"
    TokenType.BooleanOperator.And -> "AND_OPERATOR"
    TokenType.BooleanOperator.Or -> "OR_OPERATOR"
    TokenType.Delimiter.Comma -> "COMMA"
    TokenType.Delimiter.Dot -> "DOT"
    TokenType.Delimiter.Semicolon -> "SEMICOLON"
    TokenType.Eof -> "EOF"
    TokenType.MultiLineComment -> "MULTI_LINE_COMMENT"
    TokenType.SingleLineComment -> "SINGLE_LINE_COMMENT"
}
