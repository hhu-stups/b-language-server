package b.language.server.proBMangement.prob2

import ProBKernel
import b.language.server.communication.Communicator
import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.ProBSettings
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBInterface
import b.language.server.proBMangement.WrongPathException
import com.google.inject.Guice
import com.google.inject.Stage
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType

/**
 * Setup a connection to ProB2 Kernel
 */
class ProBKernelManager(private val communicator : CommunicatorInterface) : ProBInterface{

    private lateinit var kernel : ProBKernel
    private var lastProBHome : String = "DEFAULT"

    /**
     * Checks the given document with the help of ProB
     * @param uri the source to check
     * @param settings the settings for this document
     * @return a list of all problems found
     */
    override fun checkDocument(uri: String, settings: Settings): List<Diagnostic> {
        val proBSettings = ProBSettings(settings.wdChecks, settings.strictChecks, settings.performanceHints)
        val probHome = settings.probHome.absolutePath

        communicator.sendDebugMessage("checking document", MessageType.Info)

        return if(probHome == lastProBHome && this::kernel.isInitialized){
            kernel.check(uri, proBSettings)
        }else{
            lastProBHome = probHome
            setup(probHome).check(uri, proBSettings)
        }

    }

    /**
     * @param probHome the home of prob; DEFAULT for the prob version delivered with the library
     * @return an instance of prob kernel
     */
    fun setup(probHome : String) : ProBKernel{
        if(probHome != "DEFAULT"){
            System.setProperty("prob.home", probHome)
            communicator.sendDebugMessage("$probHome selected", MessageType.Info)
        }else{
            communicator.sendDebugMessage("default prob selected", MessageType.Info)
        }

        val injector = Guice.createInjector(Stage.PRODUCTION, ProBKernelModule())
        val kernel : ProBKernel
        try{
            kernel = injector.getInstance(ProBKernel::class.java)
        }catch (e : de.prob.exception.CliError){
            communicator.sendDebugMessage("wrong path to prob", MessageType.Error)
            throw WrongPathException("wrong path to prob $probHome")
        }
        return kernel
    }


}