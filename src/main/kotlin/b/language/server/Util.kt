package b.language.server

import b.language.server.dataStorage.Settings
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File

fun castJsonToSetting(json : JsonObject) : Settings {
    return Settings(Gson().fromJson(json.get("maxNumberOfProblems"), Int::class.java),
            Gson().fromJson(json.get("wdChecks"), Boolean::class.java),
            Gson().fromJson(json.get("strictChecks"), Boolean::class.java),
            Gson().fromJson(json.get("performanceHints"), Boolean::class.java),
            File(Gson().fromJson(json.get("probHome"), String::class.java)))
}