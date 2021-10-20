package ru.hse.spb.kazakov.command

import org.openqa.selenium.os.ExecutableFinder

/**
 * Representation of a user command.
 */
class UserCommand(
    private val name: String,
    private val arguments: List<String>,
    prev: PipeCommand?
) : PipeCommand(prev) {

    override fun execute(): ExecutionResult =
        if (ExecutableFinder().find(name) != null) {
            val commandCall = name + ' ' + arguments.joinToString(separator = " ")
            val process = Runtime.getRuntime().exec(commandCall)

            process.outputStream.bufferedWriter().use { it.write(getInput()) }
            val output = process.inputStream.bufferedReader().use { it.readText() }
            val error = process.errorStream.bufferedReader().use { it.readText() }

            ExecutionResult(output, if (error != "") listOf(error) else emptyList())
        } else {
            ExecutionResult("", listOf("$name: command not found"))
        }


}