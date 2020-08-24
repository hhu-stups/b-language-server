package b.language.server.proBMangement.prob

import de.prob.animator.IWarningListener
import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity


class MyWarningListener : IWarningListener {
    private val warningList : ArrayList<ErrorItem> = arrayListOf()
    override fun warningsOccurred(warnings: MutableList<ErrorItem>?) {
        warningList.addAll(warnings!!.toList())
    }

    fun getWarnings() : List<Diagnostic>{
        return convertErrorItems(warningList)
    }
}