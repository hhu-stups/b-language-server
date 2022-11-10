package b.language.server

import b.language.server.communication.CommunicatorInterface
import b.language.server.proBMangement.ProBInterface
import b.language.server.proBMangement.prob.CouldNotFindProBHomeException
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
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

        communicator.sendDebugMessage("document ${params!!.textDocument.uri} was saved", MessageType.Info)
        checkDocument(URI(params.textDocument.uri))

    }

    /**
     * checks a document via prob and the set options
     * @param currentUri the uri to perform actions on
     */
    fun checkDocument(currentUri : URI){



        val clientSettings = server.getDocumentSettings(currentUri.path)
        communicator.sendDebugMessage("waiting for document settings", MessageType.Info)

        clientSettings.thenAccept{ settings ->
            communicator.setDebugMode(settings.debugMode)
            communicator.sendDebugMessage("settings are $settings", MessageType.Info)

            try{
                val diagnostics: List<Diagnostic> = proBInterface.checkDocument(currentUri, settings)

                val sortedDiagnostic = diagnostics.groupBy { it.source }
                sortedDiagnostic.forEach { entry -> communicator.publishDiagnostics(entry.key, entry.value)}
                communicator.showMessage("Evaluation done: ${diagnostics.size} problem(s)", MessageType.Log)

                val filesWithProblems = sortedDiagnostic.keys.toList()
                val invalidFiles = calculateToInvalidate(currentUri.path, filesWithProblems)
                invalidFiles.forEach{uri -> communicator.publishDiagnostics(uri, listOf())}
                communicator.sendDebugMessage("invalidating old files $invalidFiles", MessageType.Info)
                issueTracker[currentUri.path] = filesWithProblems.toSet()

            }catch (e : CouldNotFindProBHomeException){
                communicator.sendDebugMessage(e.message!!, MessageType.Info)
                communicator.showMessage(e.message, MessageType.Error)
            }
        }

    }


    /**
     * Gets all uris that are no longer contain problems
     * @param currentUri the uri of the current main file
     * @param filesWithProblems uris of files containing problems
     */
    private fun calculateToInvalidate(currentUri : String, filesWithProblems : List<String>) : List<String>{
        val currentlyDisplayed = issueTracker[currentUri].orEmpty()
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
        communicator.sendDebugMessage("document ${params!!.textDocument.uri} was closed - removing meta data", MessageType.Info)
        server.removeDocumentSettings(params.textDocument.uri)
    }

    /**
     * The document change notification is sent from the client to the server to
     * signal changes to a text document.
     *
     * Registration Options: TextDocumentChangeRegistrationOptions
     */
    override fun didChange(params: DidChangeTextDocumentParams?) {
        communicator.sendDebugMessage("document ${params!!.textDocument.uri} was changed", MessageType.Info)
        checkDocument(URI(params.textDocument.uri))
    }

}