package b.language.server

import b.language.server.proBMangement.prob.ProBKernelManager
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Future


fun main() {
    println("opening connection and waiting ...")
    val socket = ServerSocket(5555)
    val channel = socket.accept()
    println("accepted connection from ${channel.inetAddress}")
    startServer(channel.getInputStream(), channel.getOutputStream())
}

fun startServer(inputStream: InputStream, outputStream: OutputStream){

    println("starting kernel")
    val kernel = ProBKernelManager()
    kernel.start()
    println("done")

    val server = Server()
    val launcher : Launcher<LanguageClient> = LSPLauncher.createServerLauncher(server, inputStream, outputStream)
    val startListing : Future<*> = launcher.startListening()

    server.setRemoteProxy(launcher.remoteProxy)
    startListing.get()
}
