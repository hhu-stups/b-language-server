package b.language.server.proBMangement.prob

import b.language.server.dataStorage.Settings

data class Request(val type : MessageTypes, val uri : String = String(), val settings : Settings = Settings())