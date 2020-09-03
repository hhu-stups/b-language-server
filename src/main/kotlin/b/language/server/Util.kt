package b.language.server

import b.language.server.dataStorage.Settings
import com.google.gson.Gson
import com.google.gson.JsonObject



/**
 * Takes a json and tries to cast it into a settings objects
 * @param json the json object
 * @return the settings object
 */
fun castJsonToSetting(json : JsonObject) : Settings {
    return Settings(
            Gson().fromJson(json.get("wdChecks"), Boolean::class.java),
            Gson().fromJson(json.get("strictChecks"), Boolean::class.java),
            Gson().fromJson(json.get("performanceHints"), Boolean::class.java),
            Gson().fromJson(json.get("probHome"), String::class.java),
            Gson().fromJson(json.get("debugMode"), Boolean::class.java))

}


