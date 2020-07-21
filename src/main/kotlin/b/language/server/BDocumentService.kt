package b.language.server

import b.language.server.dataStorage.Problem
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import java.io.File
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class BDocumentService(private val server: Server) : TextDocumentService {

    private val documents = ConcurrentHashMap<String, String>()
    private val issueTracker : ConcurrentHashMap<String, ArrayList<String>> = ConcurrentHashMap()

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


        val uri = URI(params!!.textDocument.uri)

        val path = File(uri.path)

        val errorPath = File(path.parent + "/tmp/_error.json")
        val errorDict = File(path.parent + "/tmp")

        val clientSettings = server.getDocumentSettings(params.textDocument.uri)

        clientSettings.thenAccept{ setting ->
            val probInterface = ProBInterface(setting.probHome, path, errorDict, errorPath, server = server)
            probInterface.createFolder()
            probInterface.performActionOnDocument()

            val problemHandler = ProblemHandler()

            val problemList: List<Problem> = problemHandler.readProblems(errorPath.absolutePath)
            val diagnostics: List<Diagnostic> = problemHandler.transformProblems(problemList)

            server.languageClient.publishDiagnostics(PublishDiagnosticsParams(params.textDocument.uri, diagnostics))
        }

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