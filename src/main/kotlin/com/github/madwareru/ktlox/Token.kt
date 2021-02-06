package com.github.madwareru.ktlox

data class Token(
    val line: Int,
    val startColumn: Int,
    val endColumn: Int,
    val startOffset: Int,
    val endOffset: Int,
    val type: TokenType
)

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
    }
    sealed class Keyword : TokenType() {
        object And : Keyword()
        object Class : Keyword()
        object Else : Keyword()
        object Fun : Keyword()
        object For : Keyword()
        object If : Keyword()
        object Nil : Keyword()
        object Or : Keyword()
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
    }
    sealed class Delimiter : TokenType() {
        object Comma : Delimiter()
        object Dot : Delimiter()
        object Semicolon : Delimiter()
    }
    object Comment: TokenType()
    object NewLine: TokenType()
    object WhiteSpace: TokenType()
    object Eof : TokenType()
}

fun Char.matchSingleCharacterToken() : TokenType? =
    when(this) {
        '(', ')', '{', '}' -> matchBraceToken()
        '-', '+', '/', '*' -> matchArithmeticOperatorToken()
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
        '+' -> TokenType.ArithmeticOperator.Plus
        '-' -> TokenType.ArithmeticOperator.Minus
        '/' -> TokenType.ArithmeticOperator.Division
        '*' -> TokenType.ArithmeticOperator.Multiplication
        else -> null
    }

fun TokenType.print() : String = when(this) {
    TokenType.Identifier -> "IDENTIFIER"
    TokenType.Error.BadCharacter -> "ERROR:BAD_CHAR"
    TokenType.Error.UnterminatedString -> "ERROR:UNTERMINATED_STRING"
    TokenType.Comment -> "COMMENT"
    TokenType.NewLine -> "NEWLINE"
    TokenType.WhiteSpace -> "WHITESPACE"
    TokenType.Literal.String -> "STRING_LITERAL"
    TokenType.Literal.Boolean -> "BOOLEAN_LITERAL"
    TokenType.Literal.Number -> "NUMBER_LITERAL"
    TokenType.Keyword.And -> "KW_AND"
    TokenType.Keyword.Class -> "KW_CLASS"
    TokenType.Keyword.Else -> "KW_ELSE"
    TokenType.Keyword.Fun -> "KW_FUN"
    TokenType.Keyword.For -> "KW_FOR"
    TokenType.Keyword.If -> "KW_IF"
    TokenType.Keyword.Nil -> "KW_NIL"
    TokenType.Keyword.Or -> "KW_OR"
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
    TokenType.Delimiter.Comma -> "COMMA"
    TokenType.Delimiter.Dot -> "DOT"
    TokenType.Delimiter.Semicolon -> "SEMICOLON"
    TokenType.Eof -> "EOF"
}