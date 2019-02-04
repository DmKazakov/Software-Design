package ru.hse.spb.kazakov.command

import kotlin.system.exitProcess

/**
 * Representation of exit command.
 */
class Exit(prev: PipeCommand?) : PipeCommand(prev) {
    override fun execute(): Nothing {
        exitProcess(0)
    }
}