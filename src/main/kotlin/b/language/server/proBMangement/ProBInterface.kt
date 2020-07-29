package b.language.server.proBMangement

import b.language.server.dataStorage.Settings
import org.eclipse.lsp4j.Diagnostic

interface ProBInterface {

    /**
     * Checks the given document with the help of ProB
     * @param uri the source to check
     * @return a list of all problems found
     */
    abstract fun checkDocument(uri : String, settings: Settings) : List<Diagnostic>

}