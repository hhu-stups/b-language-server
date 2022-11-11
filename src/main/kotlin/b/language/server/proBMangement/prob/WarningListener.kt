package b.language.server.proBMangement.prob

import b.language.server.communication.CommunicatorInterface
import de.prob.animator.IWarningListener
import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.Diagnostic
import java.io.File

/**
 * Custom collector to collect warnings from prob kernel
 */
class InformationListener(private val fallbackPath : File, private val communicator : CommunicatorInterface) : IWarningListener {
    private val warningList : ArrayList<ErrorItem> = arrayListOf()


    override fun warningsOccurred(warnings: MutableList<ErrorItem>?) {
        warningList.addAll(warnings!!.toList())
    }

    fun getInformation() : List<Diagnostic>{
        return convertErrorItems(warningList, fallbackPath, communicator)
    }
}