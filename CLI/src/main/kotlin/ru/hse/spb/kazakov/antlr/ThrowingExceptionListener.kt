package ru.hse.spb.kazakov.antlr

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

/**
 * Listener that throws exception when a lexer error occurs.
 */
class ThrowingExceptionListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException
    ) = throw ParseCancellationException("Error:(${charPositionInLine + 1}) $msg")
}