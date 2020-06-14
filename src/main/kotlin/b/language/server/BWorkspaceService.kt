package b.language.server

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService

class BWorkspaceService : WorkspaceService {
    /**
     * The watched files notification is sent from the client to the server when
     * the client detects changes to file watched by the language client.
     */
    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        TODO("Not yet implemented")
    }

    /**
     * A notification sent from the client to the server to signal the change of
     * configuration settings.
     */
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        TODO("Not yet implemented")
    }
}