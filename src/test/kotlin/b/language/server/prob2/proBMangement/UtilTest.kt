package b.language.server.prob2.proBMangement

import b.language.server.proBMangement.prob.convertErrorItems
import b.language.server.proBMangement.prob.getErrorItemType
import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UtilTest {


    @Test
    fun generateErrorItem()
    {
        val startLine = 100
        val statCol = 100
        val endLine = 101
        val endCol = 101
        val message = "hello"
        val file = "/test"

         val errorItem = ErrorItem(message, ErrorItem.Type.INTERNAL_ERROR,
                 listOf(ErrorItem.Location(file, startLine,statCol,endLine,endCol)))

         val diagnostic = Diagnostic(
                 Range(
                         Position(startLine-1, statCol),
                         Position(endLine-1, endCol)),
                 message, DiagnosticSeverity.Error, file)

        val errorItemAfter = convertErrorItems(listOf(errorItem), "dummy").first()

        assertEquals(diagnostic, errorItemAfter)

    }

    @Test
    fun errorCodeTranslation_Error1()
    {
        val result = getErrorItemType(ErrorItem.Type.INTERNAL_ERROR)

        assertEquals(DiagnosticSeverity.Error, result)
    }

    @Test
    fun errorCodeTranslation_Error2()
    {
        val result = getErrorItemType(ErrorItem.Type.ERROR)

        assertEquals(DiagnosticSeverity.Error, result)
    }

    @Test
    fun errorCodeTranslation_Warning()
    {
        val result = getErrorItemType(ErrorItem.Type.WARNING)

        assertEquals(DiagnosticSeverity.Warning, result)
    }


}