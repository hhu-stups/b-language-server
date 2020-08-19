package b.language.server.prob2.proBMangement

import b.language.server.communication.DummyCommunication
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.WrongPathException
import b.language.server.proBMangement.prob2.ProBKernelManager
import org.eclipse.lsp4j.Diagnostic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class ProBKernelManagerTest {
/*
    @Test
    fun testCorrectBaseSetup(){
        val dummyCommunication = DummyCommunication()
        val proBKernelManager = ProBKernelManager(dummyCommunication)

        proBKernelManager.setup("DEFAULT")

        assertEquals(listOf("default prob selected", "generating kernel...", "..done"), dummyCommunication.outputCollector)
    }


    @Test
    fun testCorrectBaseSetupWrongPath(){
        val dummyCommunication = DummyCommunication()
        val proBKernelManager = ProBKernelManager(dummyCommunication)

        Assertions.assertThrows(WrongPathException::class.java) { proBKernelManager.setup("NOTDEFAULT") }
    }

    @Test
    fun testCheckDocument(){
        val dummyCommunication = DummyCommunication()
        val proBKernelManager = ProBKernelManager(dummyCommunication)

        val diagnostics = proBKernelManager.checkDocument("src/test/resources/Lift.mch", Settings(100, true, true, true, File("DEFAULT")))
        println(dummyCommunication.outputCollector)
        println(diagnostics)
    }
    */

}