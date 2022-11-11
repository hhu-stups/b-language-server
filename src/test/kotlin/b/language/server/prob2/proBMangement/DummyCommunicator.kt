import b.language.server.communication.CommunicatorInterface
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType

class DummyCommunicator : CommunicatorInterface {

    val pushedDiagnostics :MutableMap<String, List<Diagnostic>> = mutableMapOf()
    private val pushedDebugMessages = mutableListOf<String>()


    /**
     * Sends the diagnostics
     *
     * @param diagnostics object containing the Diagnostics
     */
    override fun publishDiagnostics(target: String, diagnostics: List<Diagnostic>) {
        pushedDiagnostics[target] = diagnostics
    }

    /**
     * Sends a debug message resulting in a output channel message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun sendDebugMessage(message: String, severity: MessageType) {
        pushedDebugMessages.add(message)
    }

    /**
     * Sends a popup message resulting in a popup message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    override fun showMessage(message: String, severity: MessageType) {
        //void
    }

    /**
     * To enable/disable debug mode
     *
     * @param mode the new state of the debug mode
     */
    override fun setDebugMode(mode: Boolean) {
        //void
    }

    /**
     * Can be used to store a messages until a "sendDebugMessage" command is sent. The messages will be sent as FIFO
     * @param message the message to send
     * @param severity tne message severity
     */
    override fun bufferDebugMessage(message: String, severity: MessageType) {
        //void
    }

}
