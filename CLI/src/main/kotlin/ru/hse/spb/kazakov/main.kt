package ru.hse.spb.kazakov

import org.antlr.v4.runtime.CharStreams
import ru.hse.spb.kazakov.antlr.ThrowingExceptionListener
import ru.hse.spb.kazakov.parser.CliParser
import ru.hse.spb.kazakov.parser.ParsingException
import java.lang.IllegalArgumentException

/**
 * Request-response cycle.
 */
fun main() {
    val lexer = CliLexer(CharStreams.fromString(""))
    lexer.removeErrorListeners()
    lexer.addErrorListener(ThrowingExceptionListener())
    val parser = CliParser()
    val interpreter = CliInterpreter()

    while (true) {
        print("> ")
        val userInput = readLine()
        lexer.inputStream = CharStreams.fromString(userInput + '\n')

        val commands = try {
            val parseResult = parser.parseCommands(lexer.allTokens)
            interpreter.interpret(parseResult) ?: continue
        } catch (exception: IllegalArgumentException) {
            println(exception.message)
            continue
        } catch (exception: ParsingException) {
            println(exception.message)
            continue
        }

        val output = commands.getOutput()
        if (output != "") {
            println(output)
        }
        commands.getErrors().forEach { println(it) }
    }
}