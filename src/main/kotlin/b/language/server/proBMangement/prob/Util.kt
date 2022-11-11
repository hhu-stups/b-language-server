package b.language.server.proBMangement.prob

import b.language.server.communication.CommunicatorInterface
import de.prob.animator.domainobjects.ErrorItem
import org.eclipse.lsp4j.*
import java.io.File


fun convertErrorItems(errorItems: List<ErrorItem>, currentLoadedFile: File, communicator: CommunicatorInterface) : List<Diagnostic>{
    return  errorItems.map { errorItem ->
        errorItem.locations.map { location ->

            communicator.sendDebugMessage( File(location.filename).toURI().path, MessageType.Warning)
            Diagnostic(Range(
                    Position(location.startLine - 1, location.startColumn),
                    Position(location.endLine - 1, location.endColumn)),
                    errorItem.message,
                    getErrorItemType(errorItem.type),

                File(location.filename).toURI().path)

        }.ifEmpty { //Fallback when errors from prob do not have position infos
            listOf(Diagnostic(Range(
                    Position(0,0),
                    Position(0,0)),
                    errorItem.message,
                    getErrorItemType(errorItem.type),
               File(currentLoadedFile.absolutePath).toURI().path))
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