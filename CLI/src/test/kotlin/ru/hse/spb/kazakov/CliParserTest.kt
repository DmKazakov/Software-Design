package ru.hse.spb.kazakov

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus
import org.antlr.v4.runtime.CharStreams
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.support.io.TempDirectory
import ru.hse.spb.kazakov.antlr.ThrowingExceptionListener
import ru.hse.spb.kazakov.command.*
import ru.hse.spb.kazakov.parser.CliParser
import ru.hse.spb.kazakov.parser.ParsingException
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

@ExtendWith(TempDirectory::class)
class CliParserTest {
    @Test
    fun testConstructor() {
        CliParser()
    }

    @Test
    fun testEchoNoArguments() {
        val command = getCommands("echo")
        assertTrue(command is Echo)
        assertEquals("", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testEchoOneArgument() {
        val command = getCommands("echo /e*t5f.")
        assertTrue(command is Echo)
        assertEquals("/e*t5f.", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testEchoMultipleArgument() {
        val command = getCommands("echo /e*t5f. 2 fds")
        assertTrue(command is Echo)
        assertEquals("/e*t5f. 2 fds", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testEchoMultipleArgumentMultipleWS() {
        val command = getCommands("echo     /e*t5f.  2     fds")
        assertTrue(command is Echo)
        assertEquals("/e*t5f. 2 fds", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testEchoIgnoreStdin() {
        val command = getCommands("echo 543 2 | echo 1")
        assertTrue(command is Echo)
        assertEquals("1", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testUndefinedVariableValue() {
        val command = getCommands("echo \$x")
        assertEquals("", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testAssignment() {
        val command = getCommands("_re1f=fdgg5 echo \$_re1f")
        assertEquals("fdgg5", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testAssignments() {
        val command = getCommands("_re1f=fdgg5 x=3 echo \$_re1f   \$x")
        assertEquals("fdgg5 3", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testVariablesValuesStoring() {
        val lexer = CliLexer(CharStreams.fromString("x=3\n"))
        val parser = CliParser()
        val interpreter = CliInterpreter()

        var parsingResult = parser.parseCommands(lexer.allTokens)
        interpreter.interpret(parsingResult)?.getOutput()

        lexer.inputStream = CharStreams.fromString("echo \$x\n")
        parsingResult = parser.parseCommands(lexer.allTokens)
        val command = interpreter.interpret(parsingResult)

        assertEquals("3", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testCatOneArgument(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "content ")
        val command = getCommands("cat $file")
        assertTrue(command is Cat)
        assertEquals("content ", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testCatMultipleArguments(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "content ")
        val anotherFile = createFile(tempDir, "file2", "content 2")
        val command = getCommands("cat $file $anotherFile")
        assertTrue(command is Cat)
        assertEquals("content \ncontent 2", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testCatStdin() {
        val command = getCommands("echo 423 | cat")
        assertTrue(command is Cat)
        assertEquals("423", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testCatIgnoreStdin(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "content ")
        val command = getCommands("echo 1 | cat $file ")
        assertTrue(command is Cat)
        assertEquals("content ", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testPwd() {
        val command = getCommands("pwd")
        assertTrue(command is Pwd)
        assertEquals(Paths.get("").toAbsolutePath().toString(), command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testPwdIgnoreArguments() {
        val command = getCommands("pwd frd sa")
        assertTrue(command is Pwd)
        assertEquals(Paths.get("").toAbsolutePath().toString(), command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testPwdIgnoreStdin() {
        val command = getCommands("echo 3 | pwd")
        assertTrue(command is Pwd)
        assertEquals(Paths.get("").toAbsolutePath().toString(), command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testPwdIgnoreStdinAndArguments() {
        val command = getCommands("echo 3 | pwd fsd q f")
        assertTrue(command is Pwd)
        assertEquals(Paths.get("").toAbsolutePath().toString(), command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWCOneArgument(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "1 line\n 2line .")
        val command = getCommands("wc $file")
        assertTrue(command is WC)
        assertEquals("2 4 15", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWCMultipleArguments(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "1 line\n 2line .")
        val anotherFile = createFile(tempDir, "file2", "P P R")
        val command = getCommands("wc $file $anotherFile")
        assertTrue(command is WC)
        assertEquals("2 4 15\n1 3 5", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWCStdin() {
        val command = getCommands("echo fdsf gfdg fdg | wc")
        assertTrue(command is WC)
        assertEquals("1 3 13", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWCMultipleWS() {
        val command = getCommands("echo \"   .  []  ds      \" | wc")
        assertTrue(command is WC)
        assertEquals("1 3 18", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWCIgnoreStdin(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "1 line\n 2line .")
        val command = getCommands("echo fdsf gfdg fdg | wc $file")
        assertTrue(command is WC)
        assertEquals("2 4 15", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWCFail() {
        val command = getCommands("wc file_name")
        assertTrue(command is WC)
        assertEquals("", command?.getOutput())
        assertIterableEquals(listOf("wc: file_name: No such file"), command?.getErrors())
    }

    @Test
    fun testWCFailAndSuccess(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file_name", "1 line\n 2line .")
        val command = getCommands("wc $file fd")
        assertTrue(command is WC)
        assertEquals("2 4 15", command?.getOutput())
        assertIterableEquals(listOf("wc: fd: No such file"), command?.getErrors())
    }

    @Test
    fun testWCFailInPipe(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file_name", "1 line\n 2line .")
        val command = getCommands("echo 1 | wc $file fd | echo 3")
        assertEquals("3", command?.getOutput())
        assertIterableEquals(listOf("wc: fd: No such file"), command?.getErrors())
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    fun testExit() {
        val command = getCommands("exit")
        assertTrue(command is Exit)
        command?.getOutput()
    }

    @Test
    @ExpectSystemExitWithStatus(0)
    fun testExitInPipe() {
        val command = getCommands("echo 1 | exit | echo 4")
        command?.getOutput()
    }

    @Test
    fun testUserCommand(@TempDirectory.TempDir tempDir: Path) {
        val dirToDelete = createDir(tempDir, "dir")
        val command = getCommands("rmdir ${dirToDelete.absolutePath}")
        assertTrue(command is UserCommand)
        assertTrue(dirToDelete.exists())
        command?.getOutput()
        assertFalse(dirToDelete.exists())
    }

    @Test
    fun testExpansionsConcatenation() {
        val command = getCommands("x=ec y=ho \$x\$y /$/3d")
        assertEquals("/$/3d", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testFullQuoting() {
        val command = getCommands("echo \'\$x\'")
        assertEquals("\$x", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWeakQuoting() {
        val command = getCommands("x=1 echo \"\$x\"")
        assertEquals("1", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testQuotingConcatenation() {
        val command = getCommands("\"e\"ch'o' 2")
        assertEquals("2", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testWholeCommandQuoting() {
        val command = getCommands("'ec'h'o 2'")
        assertEquals("", command?.getOutput())
        assertIterableEquals(command?.getErrors(), listOf("echo 2: command not found"))
    }

    @Test
    fun testPipeWS() {
        val command = getCommands("echo 4|cat")
        assertEquals("4", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testMissingQuote() {
        val exception = assertThrows(ParsingException::class.java, { getCommands("echo \"") })
        assertEquals("Error:(6) unexpected token: \"", exception.message)
    }

    @Test
    fun testSinglePipe() {
        val exception = assertThrows(ParsingException::class.java, { getCommands("echo hi |") })
        assertEquals("Error:(9) unexpected token: |", exception.message)
    }

    @Test
    fun testRepeatedExecution(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file_name", "1 line\n 2line .")
        val command = getCommands("echo 1 | wc $file fd | echo 3")
        assertEquals("3", command?.getOutput())
        assertIterableEquals(listOf("wc: fd: No such file"), command?.getErrors())
        assertEquals("3", command?.getOutput())
        assertIterableEquals(listOf("wc: fd: No such file"), command?.getErrors())
    }

    private fun getCommands(input: String): PipeCommand? {
        val lexer = CliLexer(CharStreams.fromString(input + '\n'))
        val parser = CliParser()
        val interpreter = CliInterpreter()
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingExceptionListener())
        val parsingResult = parser.parseCommands(lexer.allTokens)
        return interpreter.interpret(parsingResult)
    }

    private fun createFile(@TempDirectory.TempDir tempDir: Path, name: String, content: String): String {
        val file = tempDir.resolve(name).toFile()
        file.writeText(content)
        return file.absolutePath
    }

    private fun createDir(@TempDirectory.TempDir parent: Path, name: String): File {
        val dir = parent.resolve(name).toFile()
        dir.delete()
        dir.mkdir()
        return dir
    }
}
