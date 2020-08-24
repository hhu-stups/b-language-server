package b.language.server.communication

import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.PublishDiagnosticsParams

class CommunicationCollector : CommunicatorInterface {

    val log : MutableList<Pair<String, MessageType>> = mutableListOf()
    /**
     * Sends the diagnostics
     *
     * @param diagnostics object containing the Diagnostics
     */
    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {
        TODO("Not yet implemented")
    }

    /**
     * Sends a debug message resulting in a output channel message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun sendDebugMessage(message: String, severity: MessageType) {
        log.add(Pair(message, severity))
    }

    /**
     * Sends a popup message resulting in a popup message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun showMessage(message: String, severity: MessageType) {
        TODO("Not yet implemented")
    }

    /**
     * To enable/disable debug mode
     *
     * @param mode the new state of the debug mode
     */
    override fun setDebugMode(mode: Boolean) {
        TODO("Not yet implemented")
    }
}