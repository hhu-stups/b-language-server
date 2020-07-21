package b.language.server

import b.language.server.dataStorage.Problem
import com.google.gson.Gson
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.io.*



/**
 * Responsible for reading Errors from the corresponding JSON File and sending transforming them into a object
 */
class ProblemHandler {

    /**
     * Reads the errors and transforms them to java objects
     * @param path the path to the error document
     * @return the list of read errors
     */
    fun readProblems(path : String): List<Problem> {
        val jsonStrings : ArrayList<String> = ArrayList()
        BufferedReader(FileReader(path)).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                jsonStrings.add(line!!)
            }
        }


        return jsonStrings.toList().map { string -> Gson().fromJson(string, Problem::class.java) }
    }

    /**
     * Transforms errors to error messages
     * @param problems the list of errors to transform
     * @return the transformed errors
     */
    fun transformProblems(problems : List<Problem>): List<Diagnostic> {
        return problems.
                /**
                 * Some errors from prob_cli have negative values when there is no exact error location - we set them
                 * to the first line
                 */
                map{ problem ->
                    if (problem.start.line == -1 && problem.start.character == -1 &&
                            problem.end.line == -1 && problem.end.character == -1)
                    {
                        Problem(problem.type,
                                problem.message,
                                problem.reason,
                                problem.file,
                                Position(1, 0),
                                Position(1, Integer.MAX_VALUE),
                                problem.version)
                    }
                    else{
                        problem
                    }
                }
                .map { problem -> Diagnostic(Range(problem.start, problem.end), problem.message,
                when (problem.type) {
                    "error" -> {
                        DiagnosticSeverity.Error
                    }
                    "warning" -> {
                        DiagnosticSeverity.Warning
                    }
                    "information" -> {
                        DiagnosticSeverity.Information
                    }
                    else -> {
                        DiagnosticSeverity.Hint
                    }
                },
                problem.file,
                "probcli v.${problem.version}")}
    }

}