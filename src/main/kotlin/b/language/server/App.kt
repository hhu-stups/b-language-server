package b.language.server

import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.*
import java.net.ServerSocket
import java.util.concurrent.Future


fun main(args: Array<String>) {
    if ("--stdio" in args) {
        // Keep log output of ProB and its dependencies away from the JSON-RPC stream.
        val out = System.out
        System.setOut(System.err)
        startServer(System.`in`, out)
        return
    }

    val socket = ServerSocket(0)

    println("<${socket.localPort}> is the port; opening connection and listening")
    val channel = socket.accept()
    //println("accepted connection from ${channel.inetAddress}")
    startServer(channel.getInputStream(), channel.getOutputStream())
}

fun startServer(inputStream: InputStream, outputStream: OutputStream){

    val server = Server()
    val launcher : Launcher<LanguageClient> = LSPLauncher.createServerLauncher(server, inputStream, outputStream)
    val startListing : Future<*> = launcher.startListening()

    server.setRemoteProxy(launcher.remoteProxy)
    startListing.get()
}
