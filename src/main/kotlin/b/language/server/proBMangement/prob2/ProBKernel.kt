
import b.language.server.dataStorage.ProBSettings
import b.language.server.proBMangement.prob2.MyWarningListener
import b.language.server.proBMangement.prob2.convertErrorItems
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
import org.eclipse.lsp4j.DiagnosticSeverity
import java.io.IOException
import java.nio.file.Paths


class ProBKernel @Inject constructor(private val injector : Injector, val classicalBFactory : ClassicalBFactory,
                                     private val animationSelector: AnimationSelector, private val animator : ReusableAnimator) {


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
        val newStateSpace = animator.createStateSpace()
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

    fun check(path : String, settings : ProBSettings) : List<Diagnostic>{
        unloadMachine()
        val factory = injector.getInstance(FactoryProvider.factoryClassFromExtension(path.substringAfterLast(".")))
        val warningListener = MyWarningListener()
        animator.addWarningListener(warningListener)
        val parseErrors = mutableListOf<Diagnostic>()
        loadMachine(
                java.util.function.Function { stateSpace : StateSpace ->
                    try {
                        factory.extract(path).load(prepareAdditionalSettings(settings))
                        loadIntoStateSpace(stateSpace)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    } catch (e: ModelTranslationError) {
                        throw RuntimeException(e)
                    }catch (e : ProBError){
                        parseErrors.addAll(convertErrorItems(e.errors))
                    }
                    Trace(stateSpace)
                }, settings)
        return listOf(warningListener.getWarnings(), parseErrors).flatten()
    }

    fun prepareAdditionalSettings(proBSettings: ProBSettings): MutableMap<String, String> {
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