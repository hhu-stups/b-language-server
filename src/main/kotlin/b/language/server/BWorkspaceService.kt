package b.language.server

import b.language.server.dataStorage.Settings
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService
import java.io.File

class BWorkspaceService(private val server : Server) : WorkspaceService {
    /**
     * The watched files notification is sent from the client to the server when
     * the client detects changes to file watched by the language client.
     */
    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        // Not needed
    }

    /**
     * A notification sent from the client to the server to signal the change of
     * configuration settings.
     */
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
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
