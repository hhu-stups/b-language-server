package b.language.server.proBMangement.prob

import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType


data class Reply(val diagnostics : List<Diagnostic>, val messages : List<Pair<String, MessageType>>)