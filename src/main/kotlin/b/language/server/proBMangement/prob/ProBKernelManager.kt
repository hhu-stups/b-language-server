package b.language.server.proBMangement.prob

import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.ProBSettings
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBInterface
import com.google.inject.Guice
import com.google.inject.Stage
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType
import java.io.File
import java.net.URI

/**
 * Creates the prob kernel access and maintenance
 */
class ProBKernelManager(private val communicator : CommunicatorInterface) : ProBInterface {

    private var kernel : ProBKernel
    private var probHome = "DEFAULT"

    init{
        kernel = setup()
    }


    /**
     * @return an instance of prob kernel
     */
    private fun setup() : ProBKernel {
        val injector = Guice.createInjector(Stage.PRODUCTION, ProBKernelModule(communicator))
        val kernel : ProBKernel
        try{
            kernel = injector.getInstance(ProBKernel::class.java)
        }catch (e : de.prob.exception.CliError){
            throw Exception("wrong path to prob $probHome")
        }
        return kernel
    }

    /**
     * Checks if the given prob home matches - if not a knew prob kernel will be initialized
     * @param probNewHome the potential new proB home
     * @return changes was successful/no change at all
     */
    private fun checkProBVersionSetting(probNewHome : String) : Boolean{
        return if(probNewHome != probHome)
        {
            if(File(probNewHome).exists()) {
                System.setProperty("prob.home", "/home/sebastian/prob_prolog")
                kernel = setup()
                probHome = probNewHome
                true
            } else{
                false
            }
        }else{
            true
        }
    }


    /**
     * Checks the given document with the help of ProB
     * @param uri the source to check
     * @param settings the settings for this document
     * @return a list of all problems found
     *
     * @throws CouldNotFindProBHomeException the given path ist not "DEFAULT" and wrong
     */
    override fun checkDocument(uri: String, settings: Settings): List<Diagnostic> {
        val path = URI(uri).path
        communicator.sendDebugMessage("try to use ${settings.probHome} as prob version", MessageType.Info)
        val result = checkProBVersionSetting(settings.probHome)
        if(!result){
            throw CouldNotFindProBHomeException("searched at ${settings.probHome} for prob but found nothing")
        }
        communicator.sendDebugMessage("success...", MessageType.Info)
        communicator.sendDebugMessage("checking document", MessageType.Info)
        return kernel.check(path, ProBSettings(wdChecks = settings.wdChecks, strictChecks = settings.strictChecks, performanceHints = settings.performanceHints))
    }
}