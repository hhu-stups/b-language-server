package b.language.server

import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import java.io.File
import java.io.FileWriter
import java.io.InputStream

/**
 * Interacts with ProB via Commandline
 * @param probHome the target ProbCli
 * @param fileToCheck the main machine to start evaluating
 * @param errorDict the path to store errors
 * @param wdActive is wd checks enabled?
 * @param strictActive is strict enabled?
 * @param performanceHintsActive are performance hints activated?
 * @param server the server used to connect with
 */
class ProBInterface(probHome : File,
                    fileToCheck : File,
                    val errorDict : File,
                    val errorPath : File,
                    wdActive : Boolean = false,
                    strictActive : Boolean = false,
                    performanceHintsActive : Boolean = false,
                    private val server : Server){


    private val configuration : String = " -p MAX_INITIALISATIONS 0 -version "
    private val ndjson : String = " -p NDJSON_ERROR_LOG_FILE "
    private val wd : String = if(wdActive){
        " -wd-check -release_java_parser "
    }else{
        " "
    }
    private val strict : String = if(strictActive){
        " -p STRICT_CLASH_CHECKING TRUE -p TYPE_CHECK_DEFINITIONS TRUE -lint "
    }else{
        " "
    }
    private val performanceHints : String = if(performanceHintsActive){
        " -p PERFORMANCE_INFO TRUE "
    }else{
        " "
    }

    private val command : String

    init {
        command = probHome.path +
                configuration +
                performanceHints +
                strict +
                wd +
                fileToCheck.absoluteFile +
                ndjson +
                errorPath.absoluteFile
    }


    fun createFolder() : Boolean{
        errorDict.mkdirs()
        FileWriter(errorPath, false).close()
        return errorDict.mkdirs()
    }

    /**
     * Executes the command and returns and sends an error message if something fails
     */
    fun performActionOnDocument() {
        val process : Process = Runtime.getRuntime().exec(command)
        val output : InputStream = process.inputStream

        process.waitFor() //we must wait here to ensure correct behavior when reading an error

        val outputAsString  = String(output.readAllBytes())
        if(!outputAsString.contains("ProB Command Line Interface")){
            server.languageClient.showMessage(
                    MessageParams(MessageType.Error, "Something went wrong when calling probcli $command"))
        }
    }
}