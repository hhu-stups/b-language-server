package b.language.server

import b.language.server.communication.CommunicatorInterface
import b.language.server.proBMangement.ProBFactory
import b.language.server.proBMangement.ProBInterface
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class BDocumentService(private val server: Server,
                       private val communicator: CommunicatorInterface,
                       private val proBFactory: ProBFactory = ProBFactory()) : TextDocumentService {

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
        val currentUri = params.textDocument.uri
        checkDocument(currentUri)

    }

    /**
     * checks a document via prob an the set options
     * @param currentUri uri to perform actions on
     */
    fun checkDocument(currentUri : String){

        val clientSettings = server.getDocumentSettings(currentUri)
        communicator.sendDebugMessage("waiting for document settings", MessageType.Info)

        clientSettings.thenAccept{ settings ->
            communicator.setDebugMode(settings.debugMode)
            val prob : ProBInterface = proBFactory.getProBAccess(communicator)
            communicator.sendDebugMessage("settings are $settings", MessageType.Info)

            try{
                val diagnostics: List<Diagnostic> = prob.checkDocument(currentUri, settings)
                communicator.sendDebugMessage("created diagnostics $diagnostics", MessageType.Info)
                communicator.publishDiagnostics(PublishDiagnosticsParams(currentUri, diagnostics))
                val filesWithProblems = diagnostics.map { diagnostic -> diagnostic.source }
                val invalidFiles = calculateToInvalidate(currentUri, filesWithProblems)
                invalidFiles.forEach{uri -> communicator.publishDiagnostics(PublishDiagnosticsParams(uri, listOf()))}
                communicator.sendDebugMessage("invalidating old files $invalidFiles", MessageType.Info)
                issueTracker[currentUri] = filesWithProblems.toSet()
            }catch (e : IOException){
                communicator.sendDebugMessage("command could not be executed", MessageType.Info)
                communicator.showMessage(e.message!!, MessageType.Error)
            }
        }

    }

    /**
     * Gets all uris that are no longer contain problems
     * @param currentUri the uri of the curre   nt main file
     * @param filesWithProblems uris of files containing problems
     */
    fun calculateToInvalidate(currentUri : String, filesWithProblems : List<String>) : List<String>{
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
        server.documentSettings.remove(params.textDocument.uri)
    }

    /**
     * The document change notification is sent from the client to the server to
     * signal changes to a text document.
     *
     * Registration Options: TextDocumentChangeRegistrationOptions
     */
    override fun didChange(params: DidChangeTextDocumentParams?) {
        communicator.sendDebugMessage("document ${params!!.textDocument.uri} was changed", MessageType.Info)
        val currentUri = params.textDocument.uri
        checkDocument(currentUri)
    }

}