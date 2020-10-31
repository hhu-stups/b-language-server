package b.language.server

import b.language.server.communication.Communicator
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.prob.ProBKernelManager
import com.google.gson.JsonObject
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap
import kotlin.system.exitProcess

class Server : LanguageServer, ServerInterface {

    private val textDocumentService : TextDocumentService = BDocumentService(this, Communicator, ProBKernelManager(Communicator))
    private val bWorkspaceService : WorkspaceService = BWorkspaceService(this, Communicator)
    private lateinit var languageClient : LanguageClient
    var globalSettings : Settings = Settings()
    val documentSettings : HashMap<String, CompletableFuture<Settings>> = HashMap()
    var configurationAbility : Boolean = true




    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        val res = InitializeResult(ServerCapabilities())
        res.capabilities.textDocumentSync = Either.forLeft(TextDocumentSyncKind.Full)
        res.capabilities.workspace = WorkspaceServerCapabilities(WorkspaceFoldersOptions())
        res.capabilities.workspace.workspaceFolders.supported = true
       // res.capabilities.completionProvider = CompletionOptions()
        return CompletableFuture.supplyAsync { res }
    }

    /**
     * The shutdown request is sent from the client to the server. It asks the
     * server to shutdown, but to not exit (otherwise the response might not be
     * delivered correctly to the client). There is a separate exit notification
     * that asks the server to exit.
     */
    override fun shutdown(): CompletableFuture<Any> {
        return CompletableFuture.supplyAsync{true}
    }

    /**
     * Provides access to the textDocument services.
     */
    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    /**
     * A notification to ask the server to exit its process.
     */
    override fun exit() {
        exitProcess(0)
    }



    /**
     * Provides access to the workspace services.
     */
    override fun getWorkspaceService(): WorkspaceService {
        return bWorkspaceService
    }

    fun setRemoteProxy(remoteProxy: LanguageClient) {
        this.languageClient = remoteProxy
        Communicator.client = remoteProxy
    }


    /**
     * Get the settings for the current document - will fallback to global settings eventually; If setting not cached
     * method will try to get setting from the client
     * @param uri the uri of the document requested
     * @return settings of the document requested
     */
    override fun getDocumentSettings(uri : String) : CompletableFuture<Settings> {
        Communicator.sendDebugMessage("requesting configuration for document: $uri", MessageType.Info)
        return if(!configurationAbility){
            val returnValue = CompletableFuture<Settings>()
            returnValue.complete(globalSettings)

            returnValue
        }else{
            val configurationItem = ConfigurationItem()
            configurationItem.scopeUri = uri
            configurationItem.section = "languageServer"

            val requestedConfig = languageClient.configuration(ConfigurationParams(listOf(configurationItem)))
            documentSettings[uri] = CompletableFuture.allOf(requestedConfig).thenApply{ castJsonToSetting(requestedConfig.get().first() as JsonObject) }
            documentSettings[uri]!!
        }
    }

    override fun removeDocumentSettings(uri: String) {
        documentSettings.remove(uri)
    }





}