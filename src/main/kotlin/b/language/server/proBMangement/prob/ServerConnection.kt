package b.language.server.proBMangement.prob

interface ServerConnection {

    /**
     * Orders the server to shutdown the process it keeps, clean up and shutdown itself
     */
    fun kill() : Reply
}