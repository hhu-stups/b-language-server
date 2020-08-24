package b.language.server.proBMangement.prob

import b.language.server.communication.CommunicatorInterface
import com.google.inject.AbstractModule
import de.prob.MainModule

class ProBKernelModule() : AbstractModule() {
    override fun configure() {
        install(MainModule())
    }
}