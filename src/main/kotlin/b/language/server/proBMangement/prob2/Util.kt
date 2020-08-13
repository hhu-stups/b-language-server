package b.language.server.proBMangement.prob2

import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

fun convertErrorItems(errorItems : List<ErrorItem>) : List<Diagnostic>{
    return errorItems.toList().map { errorItem ->
        errorItem.locations.map { location ->
            Diagnostic(Range(Position(location.startLine, location.startColumn), Position(location.endLine, location.endColumn)),
                    errorItem.message,
                    getErrorItemType(errorItem),
                    location.filename)
        }
    }.flatten()
}

fun getErrorItemType(errorItem : ErrorItem) : DiagnosticSeverity{
    return when(errorItem.type){
        ErrorItem.Type.ERROR -> {
            DiagnosticSeverity.Error
        }
        ErrorItem.Type.WARNING -> {
            DiagnosticSeverity.Warning
        }
        ErrorItem.Type.INTERNAL_ERROR -> {
            DiagnosticSeverity.Error
        }
        else -> {
            DiagnosticSeverity.Error
        }
    }

}