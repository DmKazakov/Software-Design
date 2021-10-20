package ru.hse.spb.kazakov.parser

/**
 * Contains assignments with unexpanded values and pipeline with unexpanded command names and arguments.
 */
data class ParsingResult(val assignments: List<UnexpandedAssignment>, val pipeline: List<UnexpandedCommand>)

/**
 * Contains variable name and unexpanded value for assignment.
 */
data class UnexpandedAssignment(val variable: String, val value: UnexpandedString)

/**
 * Contains unexpanded command name and arguments.
 */
data class UnexpandedCommand(val commandName: UnexpandedString, val arguments: List<UnexpandedString>)

/**
 * Consists of plain strings and expansions.
 */
data class UnexpandedString(val parts: List<ExpansionPart>)

/**
 * Represents either a plane string or expansion.
 */
data class ExpansionPart(val value: String, val type: Type)

enum class Type {
    STRING, EXPANSION
}
