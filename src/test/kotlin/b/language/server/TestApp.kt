package b.language.server

import b.language.server.proBMangement.prob.ProBKernelManager
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.Future



    fun main() {
        startServer(System.`in`, System.out)
    }


    fun startServer(inputStream: InputStream, outputStream: OutputStream){


        val client = Client()
        val launcher : Launcher<LanguageServer> = LSPLauncher.createClientLauncher(client, inputStream, outputStream)
        val startListing : Future<*> = launcher.startListening()


        startListing.get()
    }


