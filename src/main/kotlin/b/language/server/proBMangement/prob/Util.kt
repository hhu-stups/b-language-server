package b.language.server.proBMangement.prob

import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.io.File

fun convertErrorItems(errorItems: List<ErrorItem>, currentLoadedFile : String) : List<Diagnostic>{
    return  errorItems.map { errorItem ->
        errorItem.locations.map { location ->
            println(location.filename)
            Diagnostic(Range(
                    Position(location.startLine - 1, location.startColumn),
                    Position(location.endLine - 1, location.endColumn)),
                    errorItem.message,
                    getErrorItemType(errorItem.type),
                    separatorToSystems(location.filename))

        }.ifEmpty { //Fallback when errors from prob do not have position infos
            listOf(Diagnostic(Range(
                    Position(0,0),
                    Position(0,0)),
                    errorItem.message,
                    getErrorItemType(errorItem.type),
                    separatorToSystems(File(currentLoadedFile).absolutePath)))
        }
    }.flatten()
}

/**
 * ProB spits path out in linux writing which is okay, if we have only one root. However, in windows we can have multiple
 * roots. The path needs then to be normalized for the given OS
 *
 * @param path the path to normalize
 */
fun separatorToSystems(path : String) : String{
    return if (File.separatorChar=='\\') {
        // From Windows to Linux/Mac
        path.replace('/', File.separatorChar);
    } else {
        // From Linux/Mac to Windows
        path.replace('\\', File.separatorChar);
    }

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