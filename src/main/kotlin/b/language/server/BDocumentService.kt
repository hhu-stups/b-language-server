package b.language.server

import b.language.server.communication.CommunicatorInterface
import b.language.server.proBMangement.ProBInterface
import b.language.server.proBMangement.prob.CouldNotFindProBHomeException
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class BDocumentService(private val server: ServerInterface,
                       private val communicator: CommunicatorInterface,
                       private val proBInterface: ProBInterface) : TextDocumentService {

    private val documents = ConcurrentHashMap<String, String>()
    private val issueTracker : ConcurrentHashMap<String, Set<String>> = ConcurrentHashMap()

    /**
     * The document open notification is sent from the client to the server to
     * signal newly opened text documents. The document's truth is now managed
     * by the client and the server must not try to read the document's truth
     * using the document's uri.
     *
     * Registration Options: TextDocumentRegistrationOptions
     */
    override fun didOpen(params: DidOpenTextDocumentParams?) {
        documents[params?.textDocument!!.uri] = params.textDocument!!.text
    }

    /**
     * The document save notification is sent from the client to the server when
     * the document for saved in the client.
     *
     * Registration Options: TextDocumentSaveRegistrationOptions
     */
    override fun didSave(params: DidSaveTextDocumentParams?) {

        communicator.sendDebugMessage("document ${File(URI(params!!.textDocument.uri).path)} was saved", MessageType.Info)
        checkDocument(File(URI(params.textDocument.uri).path))

    }

    /**
     * checks a document via prob and the set options
     * @param file the uri to perform actions on
     */
    fun checkDocument(file : File){



        val clientSettings = server.getDocumentSettings(file.absolutePath)
        communicator.sendDebugMessage("waiting for document settings", MessageType.Info)

        clientSettings.thenAccept{ settings ->
            communicator.setDebugMode(settings.debugMode)
            communicator.sendDebugMessage("settings are $settings", MessageType.Info)

            try{
                val diagnostics: List<Diagnostic> = proBInterface.checkDocument(file, settings)

                val sortedDiagnostic = diagnostics.groupBy { it.source }
                sortedDiagnostic.forEach { entry -> communicator.publishDiagnostics(entry.key, entry.value)}
                communicator.showMessage("Evaluation done: ${diagnostics.size} problem(s)", MessageType.Log)

                val filesWithProblems = sortedDiagnostic.keys.toList()
                val invalidFiles = calculateToInvalidate(file, filesWithProblems)
                invalidFiles.forEach{uri -> communicator.publishDiagnostics(uri, listOf())}
                communicator.sendDebugMessage("invalidating old files $invalidFiles", MessageType.Info)
                issueTracker[file.absolutePath] = filesWithProblems.toSet()

            }catch (e : CouldNotFindProBHomeException){
                
                communicator.sendDebugMessage(e.localizedMessage, MessageType.Info)
                communicator.showMessage(e.localizedMessage, MessageType.Error)
            }
        }

    }


    /**
     * Gets all uris that are no longer contain problems
     * @param file the uri of the current main file
     * @param filesWithProblems uris of files containing problems
     */
    private fun calculateToInvalidate(file : File, filesWithProblems : List<String>) : List<String>{
        val currentlyDisplayed = issueTracker[file.absolutePath].orEmpty()
        return currentlyDisplayed.subtract(filesWithProblems).toList()
    }

    /**
     * The document close notification is sent from the client to the server
     * when the document got closed in the client. The document's truth now
     * exists where the document's uri points to (e.g. if the document's uri is
     * a file uri the truth now exists on disk).
     *
     * Registration Options: TextDocumentRegistrationOptions
     */
    override fun didClose(params: DidCloseTextDocumentParams?) {
        communicator.sendDebugMessage("document ${URI(params!!.textDocument.uri).path} was closed - removing meta data", MessageType.Info)
        server.removeDocumentSettings((URI(params.textDocument.uri).path))
    }

    /**
     * The document change notification is sent from the client to the server to
     * signal changes to a text document.
     *
     * Registration Options: TextDocumentChangeRegistrationOptions
     */
    override fun didChange(params: DidChangeTextDocumentParams?) {

        communicator.sendDebugMessage("document ${URI(params!!.textDocument.uri).path} was changed", MessageType.Info)
        checkDocument(File(URI(params.textDocument.uri).path))
    }

}