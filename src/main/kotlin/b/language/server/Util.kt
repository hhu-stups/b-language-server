package b.language.server

import b.language.server.dataStorage.Problem
import b.language.server.dataStorage.Settings
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.io.File


/**
 * Takes a json and tries to cast it into a settings objects
 * @param json the json object
 * @return the settings object
 */
fun castJsonToSetting(json : JsonObject) : Settings {
    return Settings(Gson().fromJson(json.get("maxNumberOfProblems"), Int::class.java),
            Gson().fromJson(json.get("wdChecks"), Boolean::class.java),
            Gson().fromJson(json.get("strictChecks"), Boolean::class.java),
            Gson().fromJson(json.get("performanceHints"), Boolean::class.java),
            File(Gson().fromJson(json.get("probHome"), String::class.java)))
}
