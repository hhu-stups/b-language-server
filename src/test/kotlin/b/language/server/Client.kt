package b.language.server

import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture

class Client : LanguageClient {
    /**
     * The telemetry notification is sent from the server to the client to ask
     * the client to log a telemetry event.
     */
    override fun telemetryEvent(`object`: Any?) {
        TODO("Not yet implemented")
    }

    /**
     * Diagnostics notifications are sent from the server to the client to
     * signal results of validation runs.
     */
    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams?) {
        TODO("Not yet implemented")
    }

    /**
     * The show message notification is sent from a server to a client to ask
     * the client to display a particular message in the user interface.
     */
    override fun showMessage(messageParams: MessageParams?) {
       println("hi")
    }

    /**
     * The show message request is sent from a server to a client to ask the
     * client to display a particular message in the user interface. In addition
     * to the show message notification the request allows to pass actions and
     * to wait for an answer from the client.
     */
    override fun showMessageRequest(requestParams: ShowMessageRequestParams?): CompletableFuture<MessageActionItem> {
        TODO("Not yet implemented")
    }

    /**
     * The log message notification is send from the server to the client to ask
     * the client to log a particular message.
     */
    override fun logMessage(message: MessageParams?) {
        TODO("Not yet implemented")
    }
}