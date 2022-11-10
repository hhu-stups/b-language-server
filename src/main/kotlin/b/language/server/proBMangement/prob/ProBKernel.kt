package b.language.server.proBMangement.prob


import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.ProBSettings
import com.google.inject.Inject
import com.google.inject.Injector
import de.prob.animator.ReusableAnimator
import de.prob.animator.command.CheckWellDefinednessCommand
import de.prob.animator.domainobjects.ErrorItem
import de.prob.exception.ProBError
import de.prob.scripting.FactoryProvider
import de.prob.scripting.ModelFactory
import de.prob.statespace.AnimationSelector
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType
import java.io.IOException
import java.net.URI

/**
 * Represents the interface to communicate with prob kernel
 * Is called via injector
 * @see ProBKernelManager
 */
class ProBKernel @Inject constructor(private val injector : Injector,
                                     private val animationSelector: AnimationSelector,
                                     private val animator : ReusableAnimator,
                                     private val communicator : CommunicatorInterface) {


    /**
     * Checks the given machine file; prepares all necessary object
     * @param path the file to check
     * @param settings the settings under which the check takes place
     * @return a list with the problems found
     */
    fun check(path : URI, settings : ProBSettings) : List<Diagnostic>{
        communicator.sendDebugMessage("unloading old machine", MessageType.Info)
        unloadMachine()



        val factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.path.substringAfterLast(".")))

        val informationListener = InformationListener(path.path)
        animator.addWarningListener(informationListener)

        communicator.sendDebugMessage("loading new machine", MessageType.Info)

        val problems = loadMachine(settings, path, factory)

        communicator.sendDebugMessage("returning from kernel problems are $problems", MessageType.Info)

        return listOf(informationListener.getInformation(), problems).flatten()
    }

    /**
     * Does the main work
     * @param settings the settings of the document
     * @param path the path to the document
     * @param factory a factory
     */
    private fun loadMachine(settings: ProBSettings, path : URI, factory : ModelFactory<*>): List<Diagnostic> {
        communicator.sendDebugMessage("creating new state space", MessageType.Info)

        val newStateSpace = animator.createStateSpace()
        communicator.sendDebugMessage("setting preferences", MessageType.Info)
        newStateSpace.changePreferences(prepareAdditionalSettings(settings))

        val errors = mutableListOf<ErrorItem>()

        try {
            factory.extract(path.path).loadIntoStateSpace(newStateSpace)
        } catch (e: IOException) {
            communicator.sendDebugMessage("IOException ${e.message}", MessageType.Info)
        } catch (e : ProBError){
            errors.addAll(e.errors)
        }

        if(errors.none { errorItem -> errorItem.type == ErrorItem.Type.ERROR }) {
            communicator.sendDebugMessage("No fatal errors found, continuing with option steps...", MessageType.Info)

            if (settings.strictChecks) {
                errors.addAll(newStateSpace.performExtendedStaticChecks())
            }

            try {
                communicator.sendDebugMessage("executing optional steps", MessageType.Info)
                executeAdditionalOptions(settings)
            } catch (e: RuntimeException) {
                communicator.sendDebugMessage("something bad happened, statespace was killed", MessageType.Warning)
                newStateSpace.kill()
            }

        }

        communicator.sendDebugMessage("processing errors", MessageType.Info)
        newStateSpace.kill()
        return convertErrorItems(errors, path.path)
    }


    /**
     * executes additional commands, which are defined as separate command, all others are executed at
     * @see prepareAdditionalSettings
     * @param settings the settings to be executed as command
     */
    private fun executeAdditionalOptions(settings : ProBSettings){
        if(settings.wdChecks){
            communicator.sendDebugMessage("doing WD checks", MessageType.Info)
           animator.execute(CheckWellDefinednessCommand())
        }
    }

    /**
     * to clean up a machine
     */
    private fun unloadMachine() {

        val oldTrace = animationSelector.currentTrace
        if (oldTrace != null) {
            assert(oldTrace.stateSpace === animator.currentStateSpace)
            animationSelector.changeCurrentAnimation(null)
            oldTrace.stateSpace.kill()
        }

    }


    /**
     * prepares a map with additional settings
     * note that the -lint option is an extra call to the state space and must be performed elsewhere
     * @param proBSettings the settings to put in the map
     * @return the settings map
     */
    private fun prepareAdditionalSettings(proBSettings: ProBSettings): MutableMap<String, String> {
        val settings = mutableMapOf<String, String>()
        if(proBSettings.performanceHints) {
            settings["STRICT_CLASH_CHECKING"] = "TRUE"
            settings["TYPE_CHECK_DEFINITIONS"] = "TRUE"
        }else{
            settings["STRICT_CLASH_CHECKING"] = "TRUE"
            settings["TYPE_CHECK_DEFINITIONS"] = "TRUE"
        }
        if (proBSettings.strictChecks) {
            settings["PERFORMANCE_INFO"] = "TRUE"
        }else{
            settings["PERFORMANCE_INFO"] = "TRUE"
        }
        return settings
    }


}