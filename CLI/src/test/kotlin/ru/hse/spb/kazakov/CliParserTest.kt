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
    fun testGrepPipeInputMatch() {
        val command = getCommands("echo 'some text' | grep text")
        assertTrue(command is Grep)
        assertEquals("some text", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testGrepPipeInputNotMatch() {
        val command = getCommands("echo 'some text' | grep ttext")
        assertTrue(command is Grep)
        assertEquals("", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testGrepNotExistingFile() {
        val command = getCommands("grep text file")
        assertTrue(command is Grep)
        assertEquals("", command?.getOutput())
        assertIterableEquals(listOf("grep: file: No such file"), command?.getErrors())
    }

    @Test
    fun testGrepNoPattern() {
        val command = getCommands("grep -w")
        assertTrue(command is Grep)
        assertEquals("", command?.getOutput())
        assertIterableEquals(listOf("Missing required parameter: <pattern>"), command?.getErrors())
    }

    @Test
    fun testGrepNotSpecifiedAfterContext() {
        val command = getCommands("grep -A pattern file")
        assertTrue(command is Grep)
        assertEquals("", command?.getOutput())
        assertIterableEquals(listOf("Invalid value for option '-A': 'pattern' is not an int"), command?.getErrors())
    }

    @Test
    fun testGrepMultipleMatch(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "text match text\nnew line mAtch\nmatch")
        val command = getCommands("grep atch $file")
        assertTrue(command is Grep)
        assertEquals("text match text\nmatch", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testGrepMultipleFiles(@TempDirectory.TempDir tempDir: Path) {
        val firstFile = createFile(tempDir, "file1", "text match text\nnew line\nmatch")
        val secondFile = createFile(tempDir, "file2", "atch\nnew line\nnatch.")
        val command = getCommands("grep atch $firstFile $secondFile")
        assertTrue(command is Grep)
        assertEquals("text match text\nmatch\natch\nnatch.", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testGrepCaseInsensitive(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "text match text\nnew line mAtch\nmatch\n...")
        val command = getCommands("grep -i atch $file")
        assertTrue(command is Grep)
        assertEquals("text match text\nnew line mAtch\nmatch", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testGrepWholeWord(@TempDirectory.TempDir tempDir: Path) {
        val file = createFile(tempDir, "file", "match in $\nws match  \nNot_match \nin ^ match")
        val command = getCommands("grep -w match $file")
        assertTrue(command is Grep)
        assertEquals("match in $\nws match  \nin ^ match", command?.getOutput())
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testGrepAfterContext(@TempDirectory.TempDir tempDir: Path) {
        val content = "Filler .\n" +
                "a ma tch ..\n" +
                "not_ma tch\n" +
                "not_ma tch again\n" +
                "not_ma tch again again\n" +
                "new ma tch\n" +
                "ma tch again"
        val file = createFile(tempDir, "file", content)
        val command = getCommands("grep -w -A 2 'ma tch' $file")
        assertTrue(command is Grep)
        assertEquals(
            "a ma tch ..\n" +
                    "not_ma tch\n" +
                    "not_ma tch again\n" +
                    "new ma tch\n" +
                    "ma tch again", command?.getOutput()
        )
        assertTrue(command?.getErrors()?.isEmpty() ?: false)
    }

    @Test
    fun testGrepNegativeAfterContext(@TempDirectory.TempDir tempDir: Path) {
        val content = "Filler .\n" +
                "a ma tch ..\n" +
                "not_ma tch\n" +
                "not_ma tch again\n" +
                "not_ma tch again again\n" +
                "new ma tch\n" +
                "ma tch again"
        val file = createFile(tempDir, "file", content)
        val command = getCommands("grep -w -A -2 'ma tch' $file")
        assertTrue(command is Grep)
        assertTrue(command?.getOutput()?.isEmpty() ?: false)
        assertIterableEquals(listOf("grep: invalid context length argument"), command?.getErrors())
    }

    @Test
    fun testGrepHelp() {
        val command = getCommands("grep -h")
        val expectedOutput = """
            |Usage: <main class> [-hiw] [-A=<afterContext>] <pattern> [<files>...]
            |      <pattern>        Basic regular expression
            |      [<files>...]     Any number of input files
            |  -A=<afterContext>    Print specified number of lines of trailing context after
            |                         matching lines.
            |  -h                   Output a getUsage message and exit.
            |  -i                   Ignore case distinctions in both the pattern and the input
            |                         files.
            |  -w                   Select only those lines containing matches that form whole
            |                         words.
            |
            """.trimMargin()

        assertTrue(command is Grep)
        assertEquals(expectedOutput, command?.getOutput())
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
