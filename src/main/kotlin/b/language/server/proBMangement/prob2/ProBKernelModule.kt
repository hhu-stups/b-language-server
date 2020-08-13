package b.language.server.proBMangement.prob2

import com.google.inject.AbstractModule
import de.prob.MainModule

class ProBKernelModule : AbstractModule() {
    override fun configure() {
        install(MainModule())
    }
}
