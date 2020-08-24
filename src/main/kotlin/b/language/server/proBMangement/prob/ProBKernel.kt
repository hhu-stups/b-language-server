
import b.language.server.communication.CommunicationCollector
import b.language.server.communication.Communicator
import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.ProBSettings
import b.language.server.proBMangement.prob.MyWarningListener
import b.language.server.proBMangement.prob.convertErrorItems
import com.google.inject.Inject
import com.google.inject.Injector
import de.prob.animator.ReusableAnimator
import de.prob.animator.command.CheckWellDefinednessCommand
import de.prob.exception.ProBError
import de.prob.scripting.ClassicalBFactory
import de.prob.scripting.FactoryProvider
import de.prob.scripting.ModelTranslationError
import de.prob.statespace.AnimationSelector
import de.prob.statespace.StateSpace
import de.prob.statespace.Trace
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType
import java.io.IOException


class ProBKernel @Inject constructor(private val injector : Injector, val classicalBFactory : ClassicalBFactory,
                                     private val animationSelector: AnimationSelector,
                                     private val animator : ReusableAnimator) {


    val communicator = CommunicationCollector()
    fun check(path : String, settings : ProBSettings) : Pair<List<Diagnostic>, List<Pair<String, MessageType>>>{
        communicator.log.clear()

        Communicator.sendDebugMessage("Unload old machine", MessageType.Info)
        unloadMachine()
        val factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.substringAfterLast(".")))
        val warningListener = MyWarningListener()
        animator.addWarningListener(warningListener)
        val parseErrors = mutableListOf<Diagnostic>()
        Communicator.sendDebugMessage("Load new machine", MessageType.Info)
        loadMachine(
                { stateSpace : StateSpace ->
                    try {
                        val extractedModel = factory.extract(path)
                        val stateSpaceWithSettings = extractedModel.load(prepareAdditionalSettings(settings))
                        extractedModel.loadIntoStateSpace(stateSpaceWithSettings)
                    } catch (e: IOException) {
                      //  throw RuntimeException(e)
                        communicator.sendDebugMessage("IOException ${e.message}", MessageType.Info)
                    } catch (e: ModelTranslationError) {
                        communicator.sendDebugMessage("ModelTranslationError ${e.message}", MessageType.Info)

                        //  throw RuntimeException(e)
                    }catch (e : ProBError){
                        parseErrors.addAll(convertErrorItems(e.errors))
                    }
                    Trace(stateSpace)
                }, settings)
        return Pair(listOf(warningListener.getWarnings(), parseErrors).flatten(), communicator.log)
    }

    private fun loadMachine(newTraceCreator :java.util.function.Function<StateSpace, Trace>, settings: ProBSettings){
        val newStateSpace = animator.createStateSpace()
        try {
            animationSelector.changeCurrentAnimation(newTraceCreator.apply(newStateSpace))
            executeAdditionalOptions(settings)
        } catch (e: RuntimeException) {
            newStateSpace.kill()
            throw e
        }
    }

    private fun executeAdditionalOptions(settings : ProBSettings){
       // animator.createStateSpace()
        if(settings.wdChecks){
            animator.execute(CheckWellDefinednessCommand())
        }

    }

    private fun unloadMachine() {
        val oldTrace = animationSelector.currentTrace
        if (oldTrace != null) {
            assert(oldTrace.stateSpace === animator.currentStateSpace)
            animationSelector.changeCurrentAnimation(null)
            oldTrace.stateSpace.kill()
        }
    }


    private fun prepareAdditionalSettings(proBSettings: ProBSettings): MutableMap<String, String> {
        val settings = mutableMapOf<String, String>()
        if(proBSettings.performanceHints) {
            settings["STRICT_CLASH_CHECKING"] = "TRUE"
            settings["TYPE_CHECK_DEFINITIONS"] = "TRUE"
        }
        if (proBSettings.strictChecks) {
            settings["PERFORMANCE_INFO"] = "TRUE"
        }
        return settings
    }


}