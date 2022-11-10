package b.language.server.prob2.proBMangement

import b.language.server.BDocumentService
import b.language.server.ServerInterface
import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBInterface
import b.language.server.proBMangement.prob.ProBKernelManager
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URI
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BDocumentServiceTest {
    
    class DummyCommunicator : CommunicatorInterface{

        val pushedDiagnostics :MutableMap<String, List<Diagnostic>> = mutableMapOf()



        /**
         * Sends the diagnostics
         *
         * @param diagnostics object containing the Diagnostics
         */
        override fun publishDiagnostics(target: String, diagnostics: List<Diagnostic>) {
            pushedDiagnostics[target] = diagnostics
        }

        /**
         * Sends a debug message resulting in a output channel message
         *
         * @param message the message to send
         * @param severity the Severity of the message (Error/Info/Warning)
         */
        override fun sendDebugMessage(message: String, severity: MessageType) {
           // println(message)
        }

        /**
         * Sends a popup message resulting in a popup message
         *
         * @param message the message to send
         * @param severity the Severity of the message (Error/Info/Warning)
         */
        override fun showMessage(message: String, severity: MessageType) {
            //void
        }

        /**
         * To enable/disable debug mode
         *
         * @param mode the new state of the debug mode
         */
        override fun setDebugMode(mode: Boolean) {
            //void
        }

        /**
         * Can be used to store a messages until a "sendDebugMessage" command is sent. The messages will be sent as FIFO
         * @param message the message to send
         * @param severity tne message severity
         */
        override fun bufferDebugMessage(message: String, severity: MessageType) {
            //void
        }

    }

    class DummyServer : ServerInterface{
        override fun getDocumentSettings(uri: String): CompletableFuture<Settings> {
            val result = CompletableFuture<Settings>()
            result.complete(Settings())
            return result
        }

        override fun removeDocumentSettings(uri: String) {
            TODO("Not yet implemented")
        }

    }



    /**
     * Fakes the behavior of the user by returning no errors when called the second time
     * The user fixed the errors
     */
    class DummyProBKernelManager(private val communicator : CommunicatorInterface) : ProBInterface{

        private var counter = 0

        override fun checkDocument(uri: URI, settings: Settings): List<Diagnostic> {
            return if(counter == 0){
                counter++
                val realKernel = ProBKernelManager(communicator)
                realKernel.checkDocument(uri, settings)

            }else{
                listOf()
            }
        }

    }

    @Test
    fun checkDocument_test_documentRegistered() {

        val communicator = DummyCommunicator()

        val documentService = BDocumentService(DummyServer(), communicator, ProBKernelManager(communicator))



        documentService.checkDocument(URI("src/test/resources/WD_M1.mch"))

        val targetSet =  communicator.pushedDiagnostics.entries.first().value.map { value -> value.source }.toSet()


        assertEquals(1, communicator.pushedDiagnostics.entries.size)
        assertEquals(3, communicator.pushedDiagnostics.entries.first().value.size)
        assertTrue(targetSet.first().contains("b-language-server/src/test/resources/WD_M1.mch"))

    }


    @Test
    fun checkDocument_test_subDocument_displayed_properly() {

        val communicator = DummyCommunicator()

        val documentService = BDocumentService(DummyServer(), communicator, ProBKernelManager(communicator))

        val documentToCheck = URI("src/test/resources/WD_M2.mch")

        val expectedDocument = File("src/test/resources/WD_M1.mch").absolutePath


        documentService.checkDocument(documentToCheck)

        communicator.pushedDiagnostics.clear()

        documentService.checkDocument(documentToCheck)

        val targetSet =  communicator.pushedDiagnostics.entries.first().value.map { value -> value.source }.toSet()

        assertEquals(2, communicator.pushedDiagnostics.entries.size)
        assertEquals(3, communicator.pushedDiagnostics.entries.first().value.size)
        assertEquals(expectedDocument, targetSet.first())

    }



    @Test
    fun checkDocument_test_unregistered_document_after_no_errors() {

        val communicator = DummyCommunicator()

        val documentService = BDocumentService(DummyServer(), communicator, DummyProBKernelManager(communicator))

        documentService.checkDocument(URI("src/test/resources/WD_M2.mch"))

        documentService.checkDocument(URI("src/test/resources/WD_M2.mch"))

        assertEquals(emptyList(), communicator.pushedDiagnostics.entries.first().value)
    }

    /**
     * Write Tests:
     * Test different options you can turn on and off
     * strict, WD, performance
     * Included files have errors and errors are mapped properly
     * Included files have no errors
     * Included files have
     */
}