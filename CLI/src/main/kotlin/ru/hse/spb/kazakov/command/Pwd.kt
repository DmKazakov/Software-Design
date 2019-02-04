package ru.hse.spb.kazakov.command

import java.nio.file.Paths

/**
 * Representation of pwd command.
 */
class Pwd(prev: PipeCommand?) : PipeCommand(prev) {
    override fun execute(): ExecutionResult = ExecutionResult(Paths.get("").toAbsolutePath().toString())
}