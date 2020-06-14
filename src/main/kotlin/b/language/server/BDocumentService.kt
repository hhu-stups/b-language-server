package b.language.server

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class BDocumentService(private val server: Server) : TextDocumentService {

    private val documents = ConcurrentHashMap<String, String>()

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
        println("Save")
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    /**
     * The document change notification is sent from the client to the server to
     * signal changes to a text document.
     *
     * Registration Options: TextDocumentChangeRegistrationOptions
     */
    override fun didChange(params: DidChangeTextDocumentParams?) {

        CompletableFuture.runAsync {
             val diagnostics : ArrayList<Diagnostic> =
                     arrayListOf(Diagnostic(Range(Position(1,1), Position(1,1)), "Test",  DiagnosticSeverity.Error, "Test"))
            server.languageClient.publishDiagnostics(PublishDiagnosticsParams(params?.textDocument?.uri, diagnostics))
        }
    }





}