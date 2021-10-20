package ru.hse.spb.kazakov.command

import java.io.File
import java.io.IOException

/**
 * Representation of cat command.
 */
class Cat(private val arguments: List<String>, prev: PipeCommand?) : PipeCommand(prev) {
    override fun execute(): ExecutionResult {
        if (arguments.isEmpty()) {
            return ExecutionResult(getInput())
        }

        val errors = mutableListOf<String>()
        val output = arguments.joinToString(separator = "\n") {
            try {
                File(it).readText()
            } catch (exception: IOException) {
                errors.add("cat: $it: No such file")
                ""
            }
        }

        return ExecutionResult(output, errors)
    }
}