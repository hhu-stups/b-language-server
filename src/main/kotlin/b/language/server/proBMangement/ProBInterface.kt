package b.language.server.proBMangement

import b.language.server.dataStorage.Settings
import org.eclipse.lsp4j.Diagnostic
import java.io.File

interface ProBInterface {

    /**
     * Checks the given document with the help of ProB
     * @param file the source to check
     * @param settings the settings for this document
     * @return a list of all problems found
     */
    fun checkDocument(file : File, settings: Settings) : List<Diagnostic>

}