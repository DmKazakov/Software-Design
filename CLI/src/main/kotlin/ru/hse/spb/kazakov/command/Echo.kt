package ru.hse.spb.kazakov.command

/**
 * Representation of echo command.
 */
class Echo(private val arguments: List<String>, prev: PipeCommand?) : PipeCommand(prev) {
    override fun execute(): ExecutionResult = ExecutionResult(arguments.joinToString(separator = " "))
}