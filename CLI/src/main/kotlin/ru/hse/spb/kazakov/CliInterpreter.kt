package ru.hse.spb.kazakov

import ru.hse.spb.kazakov.command.*
import ru.hse.spb.kazakov.parser.*

/**
 * A class for interpreting [ParsingResult] produced by [ru.hse.spb.kazakov.parser.CliParser].
 */
class CliInterpreter {
    private val scope = HashMap<String, String>().withDefault { "" }
    private var lastCommand: PipeCommand? = null

    /**
     * Returns interpreted from [parsingResult] chain of commands or null if there are no commands to interpret.
     */
    fun interpret(parsingResult: ParsingResult): PipeCommand? {
        reset()
        parsingResult.assignments.forEach { makeAssignment(it) }
        parsingResult.pipeline.forEach { interpretCommand(it) }
        return lastCommand
    }

    private fun reset() {
        lastCommand = null
    }

    private fun makeAssignment(assignment: UnexpandedAssignment) {
        scope[assignment.variable] = expandString(assignment.value)
    }

    private fun interpretCommand(command: UnexpandedCommand) {
        val commandName = expandString(command.commandName)
        val commandArguments = command.arguments.map { expandString(it) }
        lastCommand = buildCommand(commandName, commandArguments, lastCommand)
    }

    private fun expandString(string: UnexpandedString): String =
        string.parts.joinToString(separator = "") {
            when (it.type) {
                Type.EXPANSION -> scope.getValue(it.value)
                else -> it.value
            }
        }

    private fun buildCommand(name: String, arguments: List<String>, previous: PipeCommand?) =
        when (name) {
            "cat" -> Cat(arguments, previous)
            "echo" -> Echo(arguments, previous)
            "wc" -> WC(arguments, previous)
            "pwd" -> Pwd(previous)
            "exit" -> Exit(previous)
            "grep" -> Grep(arguments, previous)
            else -> UserCommand(name, arguments, previous)
        }
}


