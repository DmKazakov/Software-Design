package ru.hse.spb.kazakov.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import ru.hse.spb.kazakov.CliLexer.*
import ru.hse.spb.kazakov.ExpandingLexer

/**
 * A class for parsing tokens produced by [ru.hse.spb.kazakov.CliLexer].
 */
class CliParser {
    private var tokens: List<Token> = emptyList()
    private var currentPosition = 0
    private val currentToken
        get() = tokens[currentPosition]
    private val nextToken
        get() = tokens[currentPosition + 1]
    private val assignments = mutableListOf<UnexpandedAssignment>()
    private val commands = mutableListOf<UnexpandedCommand>()

    /**
     * Returns parsed from [tokens] [ParsingResult].
     */
    fun parseCommands(tokens: List<Token>): ParsingResult {
        this.tokens = tokens
        reset()

        while (parseAssignment()) {
            skipWS()
        }
        skipWS()
        parseCommands()

        if (currentPosition != tokens.lastIndex) {
            throw ParsingException("Error:(${currentToken.charPositionInLine + 1}) unexpected token: ${currentToken.text}")
        }

        return ParsingResult(assignments, commands)
    }

    private fun reset() {
        currentPosition = 0
        assignments.clear()
        commands.clear()
    }

    private fun skipWS() {
        if (currentToken.type == WS) {
            currentPosition++
        }
    }

    private fun parseAssignment(): Boolean {
        if (currentToken.type == IDENTIFIER && nextToken.type == ASSIGN) {
            val variable = currentToken.text
            currentPosition += 2
            val value = parseUnexpandedString()
            assignments.add(UnexpandedAssignment(variable, value))
            return true
        }

        return false
    }

    private fun parseUnexpandedString(): UnexpandedString {
        val parts = mutableListOf<ExpansionPart>()

        while (currentToken.isTextPart()) {
            parts.addAll(when (currentToken.type) {
                WQ -> parseWeakQuoting()
                FQ -> parseFullQuoting()
                else -> parseWord()
            })

        }

        return UnexpandedString(parts)
    }

    private fun Token.isTextPart(): Boolean =
        type == WQ || type == FQ || isWordPart()

    private fun parseWeakQuoting(): List<ExpansionPart> =
        expandVariables(currentToken.text.drop(1).dropLast(1)).also { currentPosition++ }

    private fun parseFullQuoting(): List<ExpansionPart> =
        listOf(ExpansionPart(currentToken.text.drop(1).dropLast(1), Type.STRING).also { currentPosition++ })

    private fun parseWord(): List<ExpansionPart> {
        val stringBuilder = StringBuilder()

        while (currentToken.isWordPart()) {
            stringBuilder.append(currentToken.text)
            currentPosition++
        }

        return expandVariables(stringBuilder.toString())
    }

    private fun Token.isWordPart(): Boolean =
        type == WORD || type == ASSIGN || type == PIPE || type == EXPANSION || type == IDENTIFIER

    private fun expandVariables(text: String): List<ExpansionPart> {
        val expandingLexer = ExpandingLexer(CharStreams.fromString(text))
        return expandingLexer.allTokens.map { token ->
            when (token.type) {
                ExpandingLexer.EXPANSION -> ExpansionPart(token.text.drop(1), Type.EXPANSION)
                else -> ExpansionPart(token.text, Type.STRING)
            }
        }
    }

    private fun parseCommands() {
        parseCommand()

        while (currentToken.type == PIPE) {
            val pipeToken = currentToken
            currentPosition++
            skipWS()
            if (!parseCommand()) {
                throw ParsingException("Error:(${pipeToken.charPositionInLine + 1}) unexpected token: ${pipeToken.text}")
            }
            skipWS()
        }
    }

    private fun parseCommand(): Boolean {
        val name = parseCommandCallPart()
        if (name.parts.isEmpty()) {
            return false
        }

        val arguments = mutableListOf<UnexpandedString>()
        skipWS()
        var argument = parseCommandCallPart()
        while (argument.parts.isNotEmpty()) {
            arguments.add(argument)
            skipWS()
            argument = parseCommandCallPart()
        }

        commands.add(UnexpandedCommand(name, arguments))
        return true
    }

    private fun parseCommandCallPart(): UnexpandedString {
        val parts = mutableListOf<ExpansionPart>()

        while (currentToken.isTextPart() && currentToken.type != PIPE) {
            parts.addAll(when (currentToken.type) {
                WQ -> parseWeakQuoting()
                FQ -> parseFullQuoting()
                else -> parseArgument()
            })

        }

        return UnexpandedString(parts)
    }

    private fun parseArgument(): List<ExpansionPart> {
        val stringBuilder = StringBuilder()

        while (currentToken.isWordPart() && currentToken.type != PIPE) {
            stringBuilder.append(currentToken.text)
            currentPosition++
        }

        return expandVariables(stringBuilder.toString())
    }
}

