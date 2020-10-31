package b.language.server.proBMangement.prob

import de.prob.animator.IWarningListener
import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.Diagnostic

/**
 * Custom collector to collect warnings from prob kernel
 */
class InformationListener(private val fallbackPath : String) : IWarningListener {
    private val warningList : ArrayList<ErrorItem> = arrayListOf()


    override fun warningsOccurred(warnings: MutableList<ErrorItem>?) {
        warningList.addAll(warnings!!.toList())
    }

    fun getInformation() : List<Diagnostic>{
        return convertErrorItems(warningList, fallbackPath)
    }
}