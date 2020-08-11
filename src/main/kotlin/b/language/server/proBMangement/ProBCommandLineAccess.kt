package b.language.server.proBMangement

import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.Problem
import b.language.server.dataStorage.Settings
import com.google.gson.Gson
import org.eclipse.lsp4j.*
import java.io.*
import java.net.URI


/**
 * Access ProB via command line
 */
class ProBCommandLineAccess(val communicator : CommunicatorInterface) : ProBInterface{
    /**
     * Checks the given document with the help of ProB; Will setup all needed steps to ensure a clean process
     * @param uri the source to check
     * @return a list of all problems found
     */
    override fun checkDocument(uri : String, settings: Settings): List<Diagnostic> {
        communicator.sendDebugMessage("checking document ($uri) with proB", MessageType.Info )
        val realUri = URI(uri)
        val path = File(realUri.path)
        val errorPath = File(path.parent + "/tmp/_error.json")
        val errorDict = File(path.parent + "/tmp")

        val result = createFolder(errorDict, errorPath)
         if(!result){
            throw PathCouldNotBeCreatedException("The Path leading to $errorPath has not been created due some issue.")
        }
        communicator.sendDebugMessage("creation successful", MessageType.Info)
        val command = buildCommand(settings, path, errorPath)
        communicator.sendDebugMessage("sending command <$command> to proB", MessageType.Info)

        performActionOnDocument(command)

        val problems = readProblems(errorPath.path)
        communicator.sendDebugMessage("found the following problems: $problems", MessageType.Info)

        return transformProblems(problems)
    }


    /**
     * Constructs the commandline call to proB depending on the given settings
     * @param settings the settings for the document
     * @param fileToCheck the current documents address
     * @param errorPath the path to dump the ndjson message
     * @return the execution ready command
     */
    fun buildCommand(settings : Settings, fileToCheck : File, errorPath : File) : String{
        val configuration = " -p MAX_INITIALISATIONS 0 -version "
        val ndjson = " -p NDJSON_ERROR_LOG_FILE "
        val wd : String = if(settings.wdChecks){
            " -wd-check -release_java_parser "
        }else{
            " "
        }
        val strict : String = if(settings.strictChecks){
            " -p STRICT_CLASH_CHECKING TRUE -p TYPE_CHECK_DEFINITIONS TRUE -lint "
        }else{
            " "
        }
        val performanceHints : String = if(settings.performanceHints){
            " -p PERFORMANCE_INFO TRUE "
        }else{
            " "
        }

        val probHome = settings.probHome

        return probHome.path +
                configuration +
                performanceHints +
                strict +
                wd +
                fileToCheck.absoluteFile +
                ndjson +
                errorPath.absoluteFile

    }


    /**
     * Creates the tmp folder and an empty ndjson file; will recreate an empty ndjson file to ensure clean messages
     * @param errorDict the path of the tmp dict
     * @param errorPath the path to the error file
     * @return success of the action
     */
    fun createFolder(errorDict : File, errorPath: File) : Boolean{
        communicator.sendDebugMessage("creating -----huhu----- errorDict $errorDict and errorFile $errorPath", MessageType.Info)
        errorDict.mkdirs()
        FileWriter(errorPath, false).close()
        return errorDict.exists() && errorPath.exists()
    }

    /**
     * Executes the given command
     * @param command to execute
     * @throws CommandCouldNotBeExecutedException probcli failed to execute the given command
     * @throws IOException failed to reach probcli
     */
    fun performActionOnDocument(command : String) {

        val process: Process = Runtime.getRuntime().exec(command)

        val output : InputStream = process.inputStream


       // process.waitFor() //we must wait here to ensure correct behavior when reading an error
        val exitStatus = process.onExit()
        val outputAsString  = String(output.readAllBytes())
        communicator.sendDebugMessage("output of execution + ${exitStatus.isCompletedExceptionally}", MessageType.Info)
        if(!outputAsString.contains("ProB Command Line Interface")){
            throw CommandCouldNotBeExecutedException("Error when trying to call probcli with command $command")
        }
    }


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

        val strings =  jsonStrings.toList().map { string -> Gson().fromJson(string, Problem::class.java) }
        return strings

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
            if (problem.start.line == -1 && problem.start.col == -1 &&
                    problem.end.line == -1 && problem.end.col == -1)
            {
                print(Integer.MAX_VALUE)
                Problem(problem.type,
                        problem.message,
                        problem.reason,
                        problem.file,
                        b.language.server.dataStorage.Position(1,0),
                        b.language.server.dataStorage.Position(1, Integer.MAX_VALUE),
                        problem.version)
            }
            else{
                /**
                 * Lines are off by one when they get out of vscode
                 */
                problem.start.line = problem.start.line - 1
                problem.end.line = problem.end.line - 1

                problem
            }
        }
                .map { problem -> Diagnostic(
                        Range(
                                Position(problem.start.line, problem.start.col),
                                Position(problem.end.line, problem.end.col)),

                        problem.message,

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
                        problem.file
                        //code =  " probcli v.${problem.version}"
                        )
                }
    }
}