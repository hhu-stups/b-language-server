package b.language.server.proBMangement.prob2

import ProBKernel
import b.language.server.dataStorage.ProBSettings
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBInterface
import com.google.inject.Guice
import com.google.inject.Stage
import org.eclipse.lsp4j.Diagnostic

/**
 * Setup a connection to ProB2 Kernel
 */
class ProBKernelManager : ProBInterface{

    private lateinit var kernel : ProBKernel

     /**
     * Checks the given document with the help of ProB
     * @param uri the source to check
     * @return a list of all problems found
     */
    override fun checkDocument(uri: String, settings: Settings): List<Diagnostic> {
        val proBSettings = ProBSettings(settings.wdChecks, settings.strictChecks, settings.performanceHints)
        val probHome = settings.probHome.absolutePath

        if(probHome != "DEFAULT" && !this::kernel.isInitialized)
        {
            if(probHome != "DEFAULT"){
                System.setProperty("prob.home", probHome)
            }
            val injector = Guice.createInjector(Stage.PRODUCTION, ProBKernelModule())
            kernel = injector.getInstance(ProBKernel::class.java)
        }

        return kernel.check(uri, proBSettings)
    }


}