package b.language.server

import b.language.server.proBMangement.CommandCouldNotBeExecutedException
import b.language.server.proBMangement.PathCouldNotBeCreatedException
import b.language.server.proBMangement.ProBCommandLineAccess
import b.language.server.proBMangement.ProBInterface
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.ConcurrentHashMap

class BDocumentService(private val server: Server) : TextDocumentService {

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

        val currentUri = params!!.textDocument.uri
        val clientSettings = server.getDocumentSettings(currentUri)

        clientSettings.thenAccept{ settings ->
            val prob : ProBInterface = ProBCommandLineAccess()

            try{
                val diagnostics: List<Diagnostic> = prob.checkDocument(currentUri, settings)
                server.languageClient.publishDiagnostics(PublishDiagnosticsParams(currentUri, diagnostics))
                val filesWithProblems = diagnostics.map { diagnostic -> diagnostic.source }
                calculateToInvalidate(currentUri, filesWithProblems)
                        .forEach{uri -> server.languageClient.publishDiagnostics(PublishDiagnosticsParams(uri, listOf()))}
                issueTracker[currentUri] = filesWithProblems.toSet()
            }catch (e : PathCouldNotBeCreatedException ){
                server.languageClient.showMessage(MessageParams(MessageType.Error, e.message))
            }catch (e : CommandCouldNotBeExecutedException){
                server.languageClient.showMessage(MessageParams(MessageType.Error, e.message))
            }
        }
    }

    /**
     * Gets all uris that are no longer contain problems
     * @param currentUri the uri of the current main file
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
        server.documentSettings.remove(params!!.textDocument.uri)
    }

    /**
     * The document change notification is sent from the client to the server to
     * signal changes to a text document.
     *
     * Registration Options: TextDocumentChangeRegistrationOptions
     */
    override fun didChange(params: DidChangeTextDocumentParams?) {
      //Nothing
    }

}