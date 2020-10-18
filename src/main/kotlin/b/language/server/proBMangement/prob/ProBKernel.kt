package b.language.server.proBMangement.prob


import b.language.server.communication.Communicator
import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.ProBSettings
import com.google.inject.Inject
import com.google.inject.Injector
import de.prob.animator.ReusableAnimator
import de.prob.animator.command.CheckWellDefinednessCommand
import de.prob.animator.domainobjects.ErrorItem
import de.prob.exception.ProBError
import de.prob.scripting.ClassicalBFactory
import de.prob.scripting.FactoryProvider
import de.prob.scripting.ModelTranslationError
import de.prob.statespace.AnimationSelector
import de.prob.statespace.StateSpace
import de.prob.statespace.Trace
import kotlinx.coroutines.flow.flowViaChannel
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType
import java.io.IOException

/**
 * Represents the interface to communicate with prob kernel
 * Is called via injector
 * @see ProBKernelManager
 */
class ProBKernel @Inject constructor(private val injector : Injector,
                                     val classicalBFactory : ClassicalBFactory,
                                     private val animationSelector: AnimationSelector,
                                     private val animator : ReusableAnimator,
                                     private val communicator : CommunicatorInterface) {


    /**
     * Checks the given machine file; prepares all necessary object
     * @param path the file to check
     * @param settings the settings under which the check takes place
     * @return a list with the problems found
     */
    fun check(path : String, settings : ProBSettings) : List<Diagnostic>{
        Communicator.sendDebugMessage("unloading old machine", MessageType.Info)
        unloadMachine()

        val factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.substringAfterLast(".")))

        val informationListener = InformationListener(path)
        animator.addWarningListener(informationListener)

        Communicator.sendDebugMessage("loading new machine", MessageType.Info)
        val errors = mutableListOf<ErrorItem>()

        val strictProblems = loadMachine(
                {
                    stateSpace : StateSpace ->
                    try {
                        factory.extract(path).loadIntoStateSpace(stateSpace)
                    } catch (e: IOException) {
                        communicator.sendDebugMessage("IOException ${e.message}", MessageType.Info)
                    } catch (e: ModelTranslationError) {
                        communicator.sendDebugMessage("ModelTranslationError ${e.message}", MessageType.Info)
                    } catch (e : ProBError){
                        errors.addAll(e.errors)
                    }
                    Trace(stateSpace)
                }, settings, path)

        communicator.sendDebugMessage("returning from kernel problems are ${strictProblems.toString()}", MessageType.Info)

        return listOf(informationListener.getInformation(), strictProblems, convertErrorItems(errors, path)).flatten()
    }

    /**
     * Does the main work
     * @param newTraceCreator a anonymous function dealing with the state space
     * @param settings the settings to be applied
     */
    private fun loadMachine(newTraceCreator :java.util.function.Function<StateSpace, Trace>, settings: ProBSettings,
                            path : String): List<Diagnostic> {
        communicator.sendDebugMessage("creating new state space", MessageType.Info)

        val newStateSpace = animator.createStateSpace()
        communicator.sendDebugMessage("setting preferences", MessageType.Info)
        newStateSpace.changePreferences(prepareAdditionalSettings(settings))
        val errors = mutableListOf<ErrorItem>()
        try {
            communicator.sendDebugMessage("changing animation", MessageType.Info)
            animationSelector.changeCurrentAnimation(newTraceCreator.apply(newStateSpace))
            communicator.sendDebugMessage("done", MessageType.Info)

            communicator.sendDebugMessage("executing optional steps", MessageType.Info)
            executeAdditionalOptions(settings)

            if (settings.strictChecks) {
                errors.addAll(newStateSpace.performExtendedStaticChecks())
            }

            communicator.sendDebugMessage("finished evaluation", MessageType.Info)

        }catch (e: RuntimeException) {
            communicator.sendDebugMessage("something bad happened, statespace was killed", MessageType.Warning)
            newStateSpace.kill()
            throw e
        }

        communicator.sendDebugMessage("processing errors", MessageType.Info)

        return convertErrorItems(errors, path)
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