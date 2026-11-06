package b.language.server

import com.google.gson.JsonParser
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.jsonrpc.MessageConsumer
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler
import org.eclipse.lsp4j.jsonrpc.messages.Message
import org.eclipse.lsp4j.jsonrpc.messages.ResponseMessage
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints
import org.eclipse.lsp4j.jsonrpc.validation.ReflectiveMessageValidator
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

private const val TIMEOUT_SECONDS = 60L

/**
 * Runs the server in a process of its own, as `--stdio` is about the real stdin and stdout of that process.
 */
class AppTest {
    enum class Transport(val args: List<String>) {
        STDIO(listOf("--stdio")) {
            override fun connect(process: Process) = LspConnection(process.inputStream, process.outputStream)
        },
        SOCKET(emptyList()) {
            override fun connect(process: Process): LspConnection {
                val banner = process.inputStream.bufferedReader().readLine()
                val port = assertNotNull(Regex("<(\\d+)> is the port").find(banner), banner).groupValues[1].toInt()
                val socket = Socket("localhost", port)
                return LspConnection(socket.getInputStream(), socket.getOutputStream())
            }
        }, ;

        abstract fun connect(process: Process): LspConnection
    }

    @ParameterizedTest
    @EnumSource(Transport::class)
    fun serverStartsAnswersAndExits(transport: Transport) {
        withServer(transport.args) { process ->
            val connection = transport.connect(process)

            assertIs<InitializeResult>(connection.initialize().result)
            connection.shutdownAndExit()

            assertTrue(process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS))
            assertEquals(0, process.exitValue())
        }
    }

    @Test
    fun stdoutIsValidJsonRpcWithStdioFlag() {
        withServer(Transport.STDIO.args) { process ->
            val connection = Transport.STDIO.connect(process)

            connection.initialize()
            connection.shutdownAndExit()
            connection.readUntilEndOfStream()
        }
    }

    private fun withServer(
        args: List<String>,
        block: (Process) -> Unit,
    ) {
        val java = File(System.getProperty("java.home"), "bin/java").path
        val process =
            ProcessBuilder(listOf(java, "-cp", System.getProperty("java.class.path"), "b.language.server.AppKt") + args)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        try {
            // Reading from the server blocks, so a server that does not answer has to fail the test here.
            CompletableFuture.runAsync { block(process) }.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } finally {
            process.destroyForcibly()
        }
    }
}

/**
 * A client of the server, that rejects everything in the stream that is not a valid JSON-RPC message. The framing is
 * checked by hand, as the StreamMessageProducer of lsp4j skips lines that are not headers.
 */
class LspConnection(input: InputStream, private val output: OutputStream) {
    private val input = BufferedInputStream(input)
    private val requests = mutableMapOf<String, String>()
    private val validator = ReflectiveMessageValidator(MessageConsumer { })
    private val jsonHandler =
        MessageJsonHandler(
            ServiceEndpoints.getSupportedMethods(LanguageServer::class.java) +
                ServiceEndpoints.getSupportedMethods(LanguageClient::class.java),
        ).apply { setMethodProvider { requests[it] } }

    fun initialize() = request(1, "initialize", """{"capabilities":{}}""")

    fun shutdownAndExit() {
        request(2, "shutdown", "null")
        send("""{"jsonrpc":"2.0","method":"exit"}""")
    }

    /** Reads the rest of the stream, which fails if anything in it is not a message. */
    fun readUntilEndOfStream() {
        while (!isAtEndOfStream()) {
            readMessage()
        }
    }

    private fun isAtEndOfStream(): Boolean {
        input.mark(1)
        return (input.read() == -1).also { input.reset() }
    }

    private fun request(
        id: Int,
        method: String,
        params: String,
    ): ResponseMessage {
        requests[id.toString()] = method
        send("""{"jsonrpc":"2.0","id":$id,"method":"$method","params":$params}""")
        return generateSequence { readMessage() }.filterIsInstance<ResponseMessage>().first { it.id == id.toString() }
    }

    private fun send(message: String) {
        val content = message.toByteArray()
        output.write("Content-Length: ${content.size}\r\n\r\n".toByteArray() + content)
        output.flush()
    }

    private fun readMessage(): Message {
        val header = readLine()
        val length = Regex("Content-Length: (\\d+)").matchEntire(header)?.groupValues?.get(1)?.toInt()
        if (length == null || readLine().isNotEmpty()) {
            fail("expected a message, but the stream contains: $header")
        }
        val content = String(input.readNBytes(length))
        // Both throw if the content is not a valid message.
        val message = jsonHandler.parseMessage(content).also(validator::consume)

        // lsp4j accepts these two violations of the JSON-RPC specification.
        val json = JsonParser.parseString(content).asJsonObject
        assertEquals("2.0", json.get("jsonrpc").asString)
        if (message is ResponseMessage) {
            assertTrue(json.has("result") != json.has("error"), "expected either a result or an error: $content")
        }
        return message
    }

    private fun readLine(): String =
        generateSequence { input.read().takeUnless { it == -1 || it == '\n'.code } }
            .joinToString("") { it.toChar().toString() }
            .removeSuffix("\r")
}
