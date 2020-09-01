package b.language.server.dataStorage

import java.io.File

data class Settings(val strictChecks : Boolean = true,  val wdChecks : Boolean = true,
                    val performanceHints : Boolean = true , val probHome : String = "DEFAULT",
                    val debugMode : Boolean = true)
