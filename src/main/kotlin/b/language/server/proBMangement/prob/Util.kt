package b.language.server.proBMangement.prob

import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

fun convertErrorItems(errorItems: List<ErrorItem>) : List<Diagnostic>{
    return errorItems.toList().map { errorItem ->
        errorItem.locations.map { location ->
            Diagnostic(Range(
                    Position(location.startLine - 1, location.startColumn),
                    Position(location.endLine - 1, location.endColumn)),
                    errorItem.message,
                    getErrorItemType(errorItem.type),
                    location.filename)
        }
    }.flatten()
}

fun getErrorItemType(errorItem: ErrorItem.Type) : DiagnosticSeverity{
    return when(errorItem){
        ErrorItem.Type.ERROR -> {
            DiagnosticSeverity.Error
        }
        ErrorItem.Type.WARNING -> {
            DiagnosticSeverity.Warning
        }
        ErrorItem.Type.INTERNAL_ERROR -> {
            DiagnosticSeverity.Error
        }
        ErrorItem.Type.MESSAGE -> {
            DiagnosticSeverity.Information
        }
        else -> {
            DiagnosticSeverity.Error
        }
    }

}