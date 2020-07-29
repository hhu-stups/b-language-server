package b.language.server.dataStorage


/**
 * kotlin representation of prob ndjson problems
 */
/**
 * @param type type of the problem (error/warning/information(
 * @param message problem message
 * @param reason reason for the problem to occur
 * @param file the file where the problem occurred
 * @param start start position of the problem
 * @param end end position of the problem
 * @version version of porbcli
 */
data class Problem(val type : String , val message: String, val reason : String, val file : String, val start : Position,
                   val end : Position, val version : String)

