package ru.hse.spb.kazakov.command

import picocli.CommandLine
import picocli.CommandLine.*
import java.io.File
import java.io.PrintStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.IllegalArgumentException
import java.lang.Integer.min
import java.util.*
import java.util.regex.Pattern

/**
 * Representation of grep command.
 */
class Grep(private val arguments: List<String>, prev: PipeCommand?) : PipeCommand(prev) {
    @Option(names = ["-i"], description = ["Ignore case distinctions in both the pattern and the input files."])
    private var ignoreCase = false

    @Option(names = ["-w"], description = ["Select only those lines containing matches that form whole words."])
    private var wordRegexp = false

    @Option(names = ["-A"], description = ["Print specified number of lines of trailing context after matching lines."])
    private var afterContext = 0

    @Option(names = ["-h"], usageHelp = true, description = ["Output a getUsage message and exit."])
    private var usageHelpRequested: Boolean = false

    @Parameters(arity = "1", index = "0", description = ["Basic regular expression"])
    private var pattern = ""

    @Parameters(index = "1..*", description = ["Any number of input files"])
    private var files = mutableListOf<File>()

    override fun execute(): ExecutionResult {
        try {
            CommandLine.populateCommand(this, *arguments.toTypedArray())
        } catch (exception: PicocliException) {
            val message = exception.message
            return ExecutionResult("", if (message == null) emptyList() else listOf(message))
        }

        if (usageHelpRequested) {
            return ExecutionResult(getUsage())
        }

        if (wordRegexp) {
            pattern = "\\b$pattern\\b"
        }
        val compiledPattern = if (ignoreCase) {
            Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        } else {
            Pattern.compile(pattern)
        }

        return if (files.isEmpty()) {
            ExecutionResult(grep(getInput(), compiledPattern), emptyList())
        } else {
            val errors = mutableListOf<String>()
            val output = files.mapNotNull {
                try {
                    grep(it.readText(), compiledPattern)
                } catch (exception: IOException) {
                    errors.add("grep: ${it.name}: No such file")
                    null
                } catch (exception: AfterContextException) {
                    errors.add("grep: invalid context length argument")
                    null
                }
            }.joinToString(separator = "\n")
            ExecutionResult(output, errors)
        }
    }

    private fun grep(text: String, pattern: Pattern): String {
        if (afterContext < 0) {
            throw AfterContextException()
        }

        val result = StringJoiner("\n")
        val lines = text.lines()

        var i = 0
        while (i < lines.size) {
            val matcher = pattern.matcher(lines[i])
            if (matcher.find()) {
                for (j in i..min(i + afterContext, lines.lastIndex)) {
                    result.add(lines[j])
                }
                i += afterContext
            }
            i++
        }

        return result.toString()
    }

    private fun getUsage(): String {
        val byteUsageStream = ByteArrayOutputStream()
        val usageStream = PrintStream(byteUsageStream)
        CommandLine.usage(this, usageStream)
        return byteUsageStream.toString()
    }

    private class AfterContextException : Exception()
}