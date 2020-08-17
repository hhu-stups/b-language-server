package b.language.server.communication

import b.language.server.communication.CommunicatorInterface
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.PublishDiagnosticsParams


/**
 * The Communicator is a side effect class, we don´t want side effects in out tests; especially such that are for
 * debug purpose only
 */
class DummyCommunication : CommunicatorInterface {

    val outputCollector = mutableListOf<String>()

    /**
     * Sends the diagnostics
     *
     * @param diagnostics object containing the Diagnostics
     */
    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams) {}

    /**
     * Sends a debug message resulting in a output channel message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun sendDebugMessage(message: String, severity: MessageType) {
        outputCollector.add(message)
    }

    /**
     * Sends a popup message resulting in a popup message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun showMessage(message: String, severity: MessageType) {}

    /**
     * To enable/disable debug mode
     *
     * @param mode the new state of the debug mode
     */
    override fun setDebugMode(mode: Boolean) {

    }
}