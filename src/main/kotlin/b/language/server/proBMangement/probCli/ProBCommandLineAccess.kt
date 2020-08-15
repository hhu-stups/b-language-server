package b.language.server.proBMangement.probCli

import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.Problem
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBInterface
import com.google.gson.Gson
import org.eclipse.lsp4j.*
import java.io.*
import java.net.URI


/**
 * Access ProB via command line
 */
class ProBCommandLineAccess(val communicator : CommunicatorInterface) : ProBInterface {
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
     * Produces something like
     * [/home/sebastian/prob_prolog/probcli,  -p, MAX_INITIALISATIONS, 0, -version ,  -p, NDJSON_ERROR_LOG_FILE ,...]
     * @param settings the settings for the document
     * @param fileToCheck the current documents address
     * @param errorPath the path to dump the ndjson message
     * @return the execution ready command
     */
    fun buildCommand(settings : Settings, fileToCheck : File, errorPath : File) : ProcessBuilder{
        val additionalArgument = "-p"
        val version = "-version"
        val configuration = "MAX_INITIALISATIONS"
        val maxInitAmount = "0"
        val ndjson = "NDJSON_ERROR_LOG_FILE"
        val releaseJavaParser = "-release_java_parser"
        val wdCheck = "-wd-check"
        val strictClashChecking = "STRICT_CLASH_CHECKING"
        val typeCheckDefinitions = "TYPE_CHECK_DEFINITIONS"
        val lint = "-lint"
        val tRUE = "TRUE"
        val performanceHints = "" +
                "PERFORMANCE_INFO"


        val command = mutableListOf<String>()

        command.add(settings.probHome.absolutePath)
        command.add(additionalArgument)
        command.add(configuration)
        command.add(maxInitAmount)
        command.add(version)
        command.add(additionalArgument)
        command.add(ndjson)
        command.add(errorPath.absolutePath)

        if (settings.wdChecks){
            command.add(wdCheck)
            command.add(releaseJavaParser)
        }

        if(settings.strictChecks){
            command.add(additionalArgument)
            command.add(strictClashChecking)
            command.add(tRUE)
            command.add(additionalArgument)
            command.add(typeCheckDefinitions)
            command.add(tRUE)
            command.add(additionalArgument)
            command.add(lint)
        }


        if(settings.performanceHints) {
            command.add(additionalArgument)
            command.add(performanceHints)
            command.add(tRUE)
        }

        command.add(fileToCheck.absolutePath)


        communicator.sendDebugMessage("creating cli call $command", MessageType.Info)

        try {
            return ProcessBuilder()
                    .command(command)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)

        }catch (e : IllegalArgumentException){
            communicator.sendDebugMessage("illegal argument exception", MessageType.Info)
        }
        return ProcessBuilder()
    }


    /**
     * Creates the tmp folder and an empty ndjson file; will recreate an empty ndjson file to ensure clean messages
     * @param errorDict the path of the tmp dict
     * @param errorPath the path to the error file
     * @return success of the action
     */
    fun createFolder(errorDict : File, errorPath: File) : Boolean{
        communicator.sendDebugMessage("creating errorDict $errorDict and errorFile $errorPath", MessageType.Info)
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
    fun performActionOnDocument(command : ProcessBuilder) {

        communicator.sendDebugMessage("execution + ${command.command()}", MessageType.Info)

        val process: Process = command.start()


        val outputAsString = readFromStream(process.inputStream)
        readFromStream(process.errorStream) //just void error


       // process.waitFor() //we must wait here to ensure correct behavior when reading an error
        val exitStatus = process.waitFor()
        communicator.sendDebugMessage("exit status of execution: $exitStatus", MessageType.Info)
        if(!outputAsString.contains("ProB Command Line Interface")){
            throw CommandCouldNotBeExecutedException("Error when trying to call probcli with command $command")
        }
    }

    private fun readFromStream(stream: InputStream) : String{
        val reader = BufferedReader(InputStreamReader(stream))
        val builder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            builder.append(line)
            builder.append(System.getProperty("line.separator"))
        }
        return builder.toString()
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