package b.language.server

import b.language.server.communication.Communicator
import com.google.gson.JsonObject
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.MessageType
import org.eclipse.lsp4j.services.WorkspaceService


class BWorkspaceService(private val server: Server, val communicator: Communicator) : WorkspaceService {
    /**
     * The watched files notification is sent from the client to the server when
     * the client detects changes to file watched by the language client.
     */
    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
         communicator.sendDebugMessage("----------changed watched files", MessageType.Info)

        // Not needed
    }

    /**
     * A notification sent from the client to the server to signal the change of
     * configuration settings.
     */
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        communicator.sendDebugMessage("received change in configuration settings", MessageType.Info)
        if(server.configurationAbility) {
            server.documentSettings.clear()
        }else{
            if (params!!.settings is JsonObject) {
                val settings: JsonObject = params.settings as JsonObject
                val documentSetting = castJsonToSetting(settings)
                server.globalSettings = documentSetting
            }
        }
    }
}
