package b.language.server.dataStorage

import java.io.File

data class Settings(val strictChecks : Boolean = true,  val wdChecks : Boolean = true,
                    val performanceHints : Boolean = true , val probHome : File = File("~/prob_prolog/probcli.sh"),
                    val debugMode : Boolean = true)
