package b.language.server.prob2.proBMangement

import b.language.server.communication.DummyCommunication
import b.language.server.proBMangement.WrongPathException
import b.language.server.proBMangement.prob2.ProBKernelManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.function.Executable
import kotlin.test.assertEquals

class ProBKernelManagerTest {

    @Test
    fun testCorrectBaseSetup(){
        val dummyCommunication = DummyCommunication()
        val proBKernelManager = ProBKernelManager(dummyCommunication)

        proBKernelManager.setup("DEFAULT")

        assertEquals(listOf("default prob selected"), dummyCommunication.outputCollector)
    }


    @Test
    fun testCorrectBaseSetupWrongPath(){
        val dummyCommunication = DummyCommunication()
        val proBKernelManager = ProBKernelManager(dummyCommunication)

        Assertions.assertThrows(WrongPathException::class.java) { proBKernelManager.setup("NOTDEFAULT") }
    }

    @Test
    fun testCheckDocument(){

    }
}