package b.language.server.prob2.proBMangement

import DummyCommunicator
import b.language.server.BDocumentService
import b.language.server.ServerInterface
import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBInterface
import b.language.server.proBMangement.prob.ProBKernelManager
import org.eclipse.lsp4j.Diagnostic
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

class BDocumentServiceTest {
    

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

        override fun checkDocument(file: File, settings: Settings): List<Diagnostic> {
            return if(counter == 0){
                counter++
                val realKernel = ProBKernelManager(communicator)
                realKernel.checkDocument(file, settings)

            }else{
                listOf()
            }
        }

    }

    @Test
    fun checkDocument_test_documentRegistered() {

        val communicator = DummyCommunicator()

        val documentService = BDocumentService(DummyServer(), communicator, ProBKernelManager(communicator))



        val documentToCheck = File("src/test/resources/WD_M1.mch").absolutePath

        val expectedDocument = File("src/test/resources/WD_M1.mch").absolutePath

        documentService.checkDocument(File(documentToCheck))

        val targetSet =  communicator.pushedDiagnostics.entries.first().value.map { value -> value.source }.toSet()


        assertEquals(1, communicator.pushedDiagnostics.entries.size)
        assertEquals(3, communicator.pushedDiagnostics.entries.first().value.size)
        assertEquals(expectedDocument, targetSet.first())

    }


    @Test
    fun checkDocument_test_subDocument_displayed_properly() {

        val communicator = DummyCommunicator()

        val documentService = BDocumentService(DummyServer(), communicator, ProBKernelManager(communicator))

        val documentToCheck = File("src/test/resources/WD_M2.mch").absolutePath

        val expectedDocument = File("src/test/resources/WD_M1.mch").absolutePath


        documentService.checkDocument(File(documentToCheck))

        communicator.pushedDiagnostics.clear()

        documentService.checkDocument(File(documentToCheck))

        val targetSet =  communicator.pushedDiagnostics.entries.first().value.map { value -> value.source }.toSet()

        assertEquals(2, communicator.pushedDiagnostics.entries.size)
        assertEquals(3, communicator.pushedDiagnostics.entries.first().value.size)
        assertEquals(expectedDocument, targetSet.first())

    }



    @Test
    fun checkDocument_test_unregistered_document_after_no_errors() {

        val communicator = DummyCommunicator()

        val documentService = BDocumentService(DummyServer(), communicator, DummyProBKernelManager(communicator))

        documentService.checkDocument(File(File("src/test/resources/WD_M2.mch").absolutePath))

        documentService.checkDocument(File(File("src/test/resources/WD_M2.mch").absolutePath))

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