package b.language.server.communication

import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.PublishDiagnosticsParams

/**
 * Describes the basic functions used to communicate with the outside world
 */
interface CommunicatorInterface {

    /**
     * Sends the diagnostics
     *
     * @param diagnostics object containing the Diagnostics
     */
    fun publishDiagnostics(diagnostics : PublishDiagnosticsParams)

    /**
     * Sends a debug message resulting in a output channel message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    fun sendDebugMessage(message: String, severity: MessageType)


    /**
     * Sends a popup message resulting in a popup message
     *
     * @param message the message to send
     * @param severity the Severity of the message (Error/Info/Warning)
     */
    fun showMessage(message : String, severity : MessageType)

    /**
     * To enable/disable debug mode
     *
     * @param mode the new state of the debug mode
     */
    fun setDebugMode(mode : Boolean)


    /**
     * Can be used to store a messages until a "sendDebugMessage" command is sent. The messages will be sent as FIFO
     * @param message the message to send
     * @param severity tne message severity
     */
    fun bufferDebugMessage(message : String, severity: MessageType)
}