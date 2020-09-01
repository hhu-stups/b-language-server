package b.language.server.proBMangement.prob

import b.language.server.communication.Communicator
import b.language.server.communication.CommunicatorInterface
import com.google.inject.AbstractModule
import de.prob.MainModule

class ProBKernelModule(val communicator : CommunicatorInterface) : AbstractModule() {
    override fun configure() {
        install(MainModule())
        bind(CommunicatorInterface::class.java).toInstance(communicator)
    }
}