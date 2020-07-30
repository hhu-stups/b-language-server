package b.language.server

import b.language.server.dataStorage.Position
import b.language.server.dataStorage.Problem
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBCommandLineAccess
import com.google.gson.Gson
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Range
import kotlin.test.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProBCommandLineTest{

    @Test
    fun test_readProblems(@TempDir tempPath : File = File("tmp"))
    {
        val file = File(tempPath.path+"/tmp.txt")
        val problemToWrite = Problem(message = "Test", file = "test.mch", reason = "test reason", version = "test",
                start = Position(1,1), end = Position(1,1), type = "test")
        file.writeText(Gson().toJson(problemToWrite))

        val problems = ProBCommandLineAccess().readProblems(file.path)

        val problem = problems.first()

        assertEquals(problemToWrite.version, problem.version)
        assertEquals(problemToWrite.end.col, problem.end.col)
        assertEquals(problemToWrite.end.line, problemToWrite.end.line)
        assertEquals(problemToWrite.start.col, problem.start.col)
        assertEquals(problemToWrite.start.line, problemToWrite.start.line)
        assertEquals(problemToWrite.file, problem.file)
        assertEquals(problemToWrite.message, problem.message)
        assertEquals(problemToWrite.reason, problem.reason)

    }

    @Test
    fun test_buildCommand_everything_activated(@TempDir tempPath : File = File("tmp")){
        val testSettings = Settings()
        val tempFile = File(tempPath.path+"/m.mch")
        val command = ProBCommandLineAccess().buildCommand(testSettings, tempFile, tempPath)
        assertEquals("~/prob_prolog/probcli.sh -p MAX_INITIALISATIONS 0 " +
                "-version  " +
                "-p PERFORMANCE_INFO TRUE  " +
                "-p STRICT_CLASH_CHECKING TRUE " +
                "-p TYPE_CHECK_DEFINITIONS TRUE -lint  " +
                "-wd-check -release_java_parser ${tempFile.path} " +
                "-p NDJSON_ERROR_LOG_FILE $tempPath", command)
    }

    @Test
    fun test_buildCommand_everything_not_strict(@TempDir tempPath : File = File("tmp")){
        val testSettings = Settings(strictChecks = false)
        val tempFile = File(tempPath.path+"/m.mch")
        val command = ProBCommandLineAccess().buildCommand(testSettings, tempFile, tempPath)
        assertEquals("~/prob_prolog/probcli.sh -p MAX_INITIALISATIONS 0 " +
                "-version  " +
                "-p PERFORMANCE_INFO TRUE   " +
                "-wd-check -release_java_parser ${tempFile.path} " +
                "-p NDJSON_ERROR_LOG_FILE $tempPath", command)
    }


    @Test
    fun test_buildCommand_everything_not_wd(@TempDir tempPath : File = File("tmp")){
        val testSettings = Settings(wdChecks = false)
        val tempFile = File(tempPath.path+"/m.mch")
        val command = ProBCommandLineAccess().buildCommand(testSettings, tempFile, tempPath)
        assertEquals("~/prob_prolog/probcli.sh -p MAX_INITIALISATIONS 0 " +
                "-version  " +
                "-p PERFORMANCE_INFO TRUE  " +
                "-p STRICT_CLASH_CHECKING TRUE " +
                "-p TYPE_CHECK_DEFINITIONS TRUE -lint  " +
                "${tempFile.path} " +
                "-p NDJSON_ERROR_LOG_FILE $tempPath", command)
    }


    @Test
    fun test_buildCommand_everything_not_performanceHints(@TempDir tempPath : File = File("tmp")){
        val testSettings = Settings(performanceHints = false)
        val tempFile = File(tempPath.path+"/m.mch")
        val command = ProBCommandLineAccess().buildCommand(testSettings, tempFile, tempPath)
        assertEquals("~/prob_prolog/probcli.sh -p MAX_INITIALISATIONS 0 " +
                "-version   " +
                "-p STRICT_CLASH_CHECKING TRUE " +
                "-p TYPE_CHECK_DEFINITIONS TRUE -lint  " +
                "-wd-check -release_java_parser ${tempFile.path} " +
                "-p NDJSON_ERROR_LOG_FILE $tempPath", command)
    }


    @Test
    fun test_transformProblems_negative_range(){
        val problemFile = "test.mch"
        val message = "Test"
        val version = "test"
        val testProblem =  Problem(message = message, file = problemFile, reason = "test reason", version = version,
                start = Position(-1,-1), end = Position(-1,-1), type = "error")

        val transformedProblem = ProBCommandLineAccess().transformProblems(listOf(testProblem)).first()

        val diagnostic = Diagnostic(Range(
                org.eclipse.lsp4j.Position(1,0),
                org.eclipse.lsp4j.Position(1, Integer.MAX_VALUE)), message,  DiagnosticSeverity.Error, problemFile, " probcli v.$version" )
        assertEquals(diagnostic, transformedProblem)

    }


    @Test
    fun test_transformProblems_error(){
        val problemFile = "test.mch"
        val message = "Test"
        val version = "test"
        val testProblem =  Problem(message = message, file = problemFile, reason = "test reason", version = version,
                start = Position(32,54), end = Position(54,65), type = "error")

        val transformedProblem = ProBCommandLineAccess().transformProblems(listOf(testProblem)).first()

        val diagnostic = Diagnostic(Range(
                org.eclipse.lsp4j.Position(31,54),
                org.eclipse.lsp4j.Position(53, 65)), message,  DiagnosticSeverity.Error, problemFile, " probcli v.$version" )
        assertEquals(diagnostic, transformedProblem)

    }


    @Test
    fun test_transformProblems_warning(){
        val problemFile = "test.mch"
        val message = "Test"
        val version = "test"
        val testProblem =  Problem(message = message, file = problemFile, reason = "test reason", version = version,
                start = Position(32,54), end = Position(54,65), type = "warning")

        val transformedProblem = ProBCommandLineAccess().transformProblems(listOf(testProblem)).first()

        val diagnostic = Diagnostic(Range(
                org.eclipse.lsp4j.Position(31,54),
                org.eclipse.lsp4j.Position(53, 65)), message,  DiagnosticSeverity.Warning, problemFile, " probcli v.$version" )
        assertEquals(diagnostic, transformedProblem)

    }

    @Test
    fun test_transformProblems_information(){
        val problemFile = "test.mch"
        val message = "Test"
        val version = "test"
        val testProblem =  Problem(message = message, file = problemFile, reason = "test reason", version = version,
                start = Position(32,54), end = Position(54,65), type = "information")

        val transformedProblem = ProBCommandLineAccess().transformProblems(listOf(testProblem)).first()

        val diagnostic = Diagnostic(Range(
                org.eclipse.lsp4j.Position(31,54),
                org.eclipse.lsp4j.Position(53, 65)), message,  DiagnosticSeverity.Information, problemFile, " probcli v.$version" )
        assertEquals(diagnostic, transformedProblem)

    }


    @Test
    fun test_transformProblems_hint(){
        val problemFile = "test.mch"
        val message = "Test"
        val version = "test"
        val testProblem =  Problem(message = message, file = problemFile, reason = "test reason", version = version,
                start = Position(32,54), end = Position(54,65), type = "something else")

        val transformedProblem = ProBCommandLineAccess().transformProblems(listOf(testProblem)).first()

        val diagnostic = Diagnostic(Range(
                org.eclipse.lsp4j.Position(31,54),
                org.eclipse.lsp4j.Position(5, 65)), message,  DiagnosticSeverity.Hint, problemFile, " probcli v.$version" )
        assertEquals(diagnostic, transformedProblem)

    }


    @Test
    fun test_createFolders_success(@TempDir tempPath : File = File("tmp")){
        val errorDict = File(tempPath.path+"/tmp")
        val errorPath = File(tempPath.path+"/tmp/hallo.njson")
        val result = ProBCommandLineAccess().createFolder(errorDict, errorPath)
        assertTrue(result)
    }


}