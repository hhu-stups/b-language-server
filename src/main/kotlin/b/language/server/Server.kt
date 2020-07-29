package b.language.server

import b.language.server.dataStorage.Settings
import com.google.gson.JsonObject
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap
import kotlin.system.exitProcess


class Server : LanguageServer {

    private val textDocumentService : TextDocumentService
    private val bWorkspaceService : WorkspaceService
    lateinit var languageClient : LanguageClient
    var globalSettings : Settings = Settings()
    val documentSettings : HashMap<String, CompletableFuture<Settings>> = HashMap()
    var configurationAbility : Boolean = true

    init {
        textDocumentService = BDocumentService(this)
        bWorkspaceService = BWorkspaceService(this)
    }


    /**
     * The initialize request is sent as the first request from the client to
     * the server.
     *
     * If the server receives request or notification before the initialize request it should act as follows:
     * - for a request the respond should be errored with code: -32001. The message can be picked by the server.
     * - notifications should be dropped, except for the exit notification. This will allow the exit a server without an initialize request.
     *
     * Until the server has responded to the initialize request with an InitializeResult
     * the client must not sent any additional requests or notifications to the server.
     *
     * During the initialize request the server is allowed to sent the notifications window/showMessage,
     * window/logMessage and telemetry/event as well as the window/showMessageRequest request to the client.
     */
    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        val res = InitializeResult(ServerCapabilities())
        res.capabilities.setCodeActionProvider(false)
//        res.capabilities.definitionProvider = false
        res.capabilities.hoverProvider = false
        res.capabilities.referencesProvider = false
        res.capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
        res.capabilities.documentSymbolProvider = false
        return CompletableFuture.supplyAsync { res }
    }

    override fun initialized(params: InitializedParams?) {
        //languageClient.registerCapability(DidChangeConfigurationCapabilities())
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
    }


    /**
     * Get the settings for the current document - will fallback to global settings eventually; If setting not cached
     * method will try to get setting from the client
     * @param uri the uri of the document requested
     * @return settings of the document requested
     */
    fun getDocumentSettings(uri : String) : CompletableFuture<Settings> {
        if(!configurationAbility){
            val returnValue = CompletableFuture<Settings>()
            returnValue.complete(globalSettings)
            return returnValue
        }
        // if client has configuration abilities
        return if(documentSettings.containsKey(uri))
        {
            documentSettings[uri]!!
        }else{
            val configurationItem = ConfigurationItem()
            configurationItem.scopeUri = uri
            configurationItem.section = "languageServer"
            val requestedConfig = languageClient.configuration(ConfigurationParams(listOf(configurationItem)))
            documentSettings[uri] = CompletableFuture.allOf(requestedConfig).thenApply{ castJsonToSetting(requestedConfig.get().first() as JsonObject) }
            documentSettings[uri]!!
        }
    }
}