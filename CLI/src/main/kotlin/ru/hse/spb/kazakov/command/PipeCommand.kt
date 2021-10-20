package ru.hse.spb.kazakov.command

/**
 * Representation of a CLI command.
 */
abstract class PipeCommand(private val prev: PipeCommand?) {
    private val result: ExecutionResult by lazy { execute() }

    /**
     * Returns command output.
     */
    fun getOutput(): String = result.output.also { prev?.result }

    /**
     * Returns all errors in pipe occurred so far.
     */
    fun getErrors(): List<String> = (prev?.getErrors() ?: emptyList()) + result.errors

    protected abstract fun execute(): ExecutionResult

    protected fun getInput() = prev?.getOutput() ?: ""

    protected data class ExecutionResult(val output: String, val errors: List<String> = emptyList())
}