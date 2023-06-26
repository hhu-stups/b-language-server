package b.language.server.communication

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.services.LanguageClient


object Communicator : CommunicatorInterface {


    /**
     * Will be set in the server an encapsulates the client
     */
    lateinit var client : LanguageClient

    private var debugMode : Boolean = true


    /**
     * Sends the diagnostics
     *
     * @param diagnostics object containing the Diagnostics
     */
    override fun publishDiagnostics(target: String, diagnostics: List<Diagnostic>) {
        client.publishDiagnostics(PublishDiagnosticsParams(target, diagnostics))

    }

    /**
     * Sends a debug message resulting in a output channel message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun sendDebugMessage(message: String, severity: MessageType) {
        if(debugMode) {
            client.logMessage(MessageParams(severity, message))
        }

    }

    /**
     * Sends a popup message resulting in a popup message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun showMessage(message: String, severity: MessageType) {
        client.showMessage(MessageParams(severity, message))
    }


    /**
     * To enable/disable debug mode
     *
     * @param mode the new state of the debug mode
     */
    override fun setDebugMode(mode : Boolean){
        debugMode = mode
    }

    /**
     * Can be used to store a messages until a "sendDebugMessage" command is sent. The messages will be sent as FIFO
     * @param message the message to send
     * @param severity tne message severity
     */
    override fun bufferDebugMessage(message: String, severity: MessageType) {
        TODO("Not yet implemented")
    }
}