package com.github.madwareru.ktlox

class Scanner(private val source: String) {
    private val tokens = ArrayList<Token>()
    private var currentLine = 0
    private var currentLineStart = 0
    private var offset = 0
    private var start = 0
    private var currentCharacter: Char = '\u0000'
    private var multilineCommentNesting = 0
    private lateinit var currentStartPosition: CharacterPosition

    val scannedTokens by lazy {
        scanTokensInternal();
        tokens
    }

    val scannedTokensIgnoringCommentsAndWhitespaces by lazy {
        scannedTokens.filter {
            it.type !is TokenType.WhiteSpace &&
            it.type !is TokenType.NewLine &&
            it.type !is TokenType.SingleLineComment
        }
    }

    private fun scanTokensInternal() {
        multilineCommentNesting = 0
        currentLine = 0
        currentLineStart = 0
        offset = 0
        tokens.clear()
        while (!isAtEnd()) {
            start = offset
            currentStartPosition = CharacterPosition(currentLine, offset - currentLineStart)
            scanNextToken()
        }
        val startPosition = CharacterPosition(currentLine, offset - currentLineStart)
        val endPosition = CharacterPosition(currentLine, offset - currentLineStart)
        tokens.add(
            Token(
                startPosition,
                endPosition,
                offset,
                offset,
                TokenType.Eof
            )
        )
    }

    private fun scanNextToken() {
        advance()
        currentStartPosition = CharacterPosition(0, 0)
        currentCharacter?.let { c ->
            val token = when(val singleCharacterToken = c.matchSingleCharacterToken()) {
                null -> scanDeepToken(c)
                else -> singleCharacterToken
            }
            val endPosition = CharacterPosition(currentLine, offset - currentLineStart)
            tokens.add(
                Token(
                    currentStartPosition,
                    endPosition,
                    start,
                    offset,
                    token
                )
            )
        }
    }

    private fun scanDeepToken(startCharacter: Char): TokenType {
        fun matchLookahead(test: Char, trueChoice: TokenType, falseChoice: TokenType) =
            if (lookAhead() == test) { advance(); trueChoice } else { falseChoice }

        return when (startCharacter) {
            '!' -> matchLookahead('=',
                TokenType.BooleanOperator.NotEqual,
                TokenType.BooleanOperator.Not
            )
            '<' -> matchLookahead('=',
                TokenType.BooleanOperator.LesserEqual,
                TokenType.BooleanOperator.Lesser
            )
            '>' -> matchLookahead('=',
                TokenType.BooleanOperator.GreaterEqual,
                TokenType.BooleanOperator.Greater
            )
            '=' -> matchLookahead('=',
                TokenType.BooleanOperator.Equal,
                TokenType.AssignmentOperator
            )
            in whiteSpaceChars -> {
                while (!isAtEnd() && lookAhead() in whiteSpaceChars) { advance() }
                TokenType.WhiteSpace
            }
            '\n' -> {
                advanceLine()
                TokenType.NewLine
            }
            '"' -> scanStringToken()
            in digits -> scanNumberToken()
            in upperCaseLetters, in lowerCaseLetters -> scanIdentifier()
            '/' -> {
                when (lookAhead()) {
                    '/' -> {
                        advanceUntilTheEndOfLine();
                        TokenType.SingleLineComment
                    }
                    '*' -> {
                        multilineCommentNesting++
                        advance()
                        advance()
                        advanceUntilTheEndOfMultilineComment()
                        TokenType.SingleLineComment
                    }
                    else -> TokenType.ArithmeticOperator.Division
                }
            }
            else -> TokenType.Error.BadCharacter
        }
    }

    private fun advanceUntilTheEndOfMultilineComment() {
        while(!isAtEnd() && multilineCommentNesting > 0 ) {
            when {
                currentCharacter == '/' && lookAhead() == '*' -> {
                    advance()
                    advance()
                    multilineCommentNesting++
                }
                currentCharacter == '*' && lookAhead() == '/' -> {
                    advance()
                    multilineCommentNesting--
                    if (multilineCommentNesting > 0) {
                        // In the case when we need to continue nested comment counting we need to
                        // step through next character immediately. But in the case when comment braces
                        // are balanced already we should not do it, because tokenizer will call advance()
                        // itself before scanning next token
                        advance()
                    }
                }
                currentCharacter == '\n' -> advanceLine()
                else -> advance()
            }
        }
    }

    private fun advanceUntilTheEndOfLine() {
        while (!isAtEnd() && lookAhead() != '\n') { advance() }
    }

    private fun scanIdentifier(): TokenType {
        while(!isAtEnd() && (lookAhead() in digits
            || lookAhead() in upperCaseLetters
            || lookAhead() in lowerCaseLetters
        )) { advance() }
        return when {
            currentIdentifierMatches("false") -> TokenType.Literal.Boolean
            currentIdentifierMatches("true") -> TokenType.Literal.Boolean
            currentIdentifierMatches("or") -> TokenType.Keyword.Or
            currentIdentifierMatches("and") -> TokenType.Keyword.And
            currentIdentifierMatches("class") -> TokenType.Keyword.Class
            currentIdentifierMatches("else") -> TokenType.Keyword.Else
            currentIdentifierMatches("fun") -> TokenType.Keyword.Fun
            currentIdentifierMatches("for") -> TokenType.Keyword.For
            currentIdentifierMatches("if") -> TokenType.Keyword.If
            currentIdentifierMatches("nil") -> TokenType.Keyword.Nil
            currentIdentifierMatches("print") -> TokenType.Keyword.Print
            currentIdentifierMatches("return") -> TokenType.Keyword.Return
            currentIdentifierMatches("super") -> TokenType.Keyword.Super
            currentIdentifierMatches("this") -> TokenType.Keyword.This
            currentIdentifierMatches("var") -> TokenType.Keyword.Var
            currentIdentifierMatches("while") -> TokenType.Keyword.While
            else -> TokenType.Identifier
        }
    }

    private fun currentIdentifierMatches(match: String) : Boolean {
        if(match.length != offset - start) return false
        for(i in match.indices) {
            if (source[start+i] != match[i]) return false
        }
        return true
    }

    private fun scanNumberToken(): TokenType {
        while (!isAtEnd() && lookAhead() in digits) { advance() }
        if(!isAtEnd() && lookAhead() == '.' && lookAhead2() in digits) {
            advance()
            while (!isAtEnd() && lookAhead() in digits) { advance() }
        }
        return TokenType.Literal.Number
    }

    private fun scanStringToken() : TokenType {
        while (!isAtEnd() && lookAhead() != '"') {
            if (lookAhead() == '\n') {
                advance()
                return TokenType.Error.UnterminatedString
            }
            advance()
        }
        if (isAtEnd()) return TokenType.Error.UnterminatedString
        advance()
        return TokenType.Literal.String
    }

    private fun advance() {
        if(isAtEnd()) {
            currentCharacter = '\u0000'
        } else {
            currentCharacter = source[offset]
            offset++
        }
    }

    private fun advanceLine() {
        advance()
        currentLineStart = offset
        currentLine++
    }

    private fun lookAhead() = if (isAtEnd()) '\u0000' else source[offset]
    private fun lookAhead2() = if (offset + 1 < source.length) source[offset + 1] else '\u0000'

    private fun isAtEnd() = offset >= source.length

    companion object {
        val whiteSpaceChars = arrayOf('\r', ' ', '\t')
        val digits = '0'..'9'
        val upperCaseLetters = 'A'..'Z'
        val lowerCaseLetters = 'a'..'z'
    }
}