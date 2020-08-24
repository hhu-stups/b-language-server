package b.language.server.proBMangement.prob

import b.language.server.communication.CommunicatorInterface
import b.language.server.dataStorage.Settings
import b.language.server.proBMangement.ProBInterface
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.MessageType
import org.zeromq.SocketType

import org.zeromq.ZContext
import org.zeromq.ZMQ


class ProBKernelConnector(val communicator : CommunicatorInterface) : ProBInterface, ServerConnection{


    private val context = ZContext()
    private val socket: ZMQ.Socket = context.createSocket(SocketType.REQ)

    init {
        socket.connect("tcp://localhost:5557")
    }

    /**
     * Checks the given document with the help of ProB
     * @param uri the source to check
     * @param settings the settings for this document
     * @return a list of all problems found
     */
    override fun checkDocument(uri: String, settings: Settings): List<Diagnostic> {
        communicator.sendDebugMessage("start checking, submitting to prob server " , MessageType.Info)
        val reply = send(Request(MessageTypes.CHECK, uri, settings))
        communicator.sendDebugMessage("got reply " , MessageType.Info)
        System.err.println("getting reply $reply")
        deliverDebugMessages(reply.messages)
        System.err.println("deliverd messages")
        return reply.diagnostics
    }


    /**
     * sends a request to the b kernel thread
     * @param payLoad the request to be send
     * @return a reply
     */
    fun send(payLoad : Request) : Reply{
        communicator.sendDebugMessage("sending... " , MessageType.Info)

        socket.send(Gson().toJson(payLoad))
        communicator.sendDebugMessage("rec... " , MessageType.Info)

        val recv = socket.recvStr()
        communicator.sendDebugMessage("rec: $recv " , MessageType.Info)

        System.err.println("deserialize message")
        val answer = try{
            Gson().fromJson(recv, Reply::class.java)
        }catch (e : JsonSyntaxException){
            System.err.println("Failed..")
            throw Exception(e)
        }
        System.err.println("deserialize message done")

        communicator.sendDebugMessage("answer $answer" , MessageType.Info)

        return answer
    }

    /**
     * Delivers Messages we got from the b kernel thread
     * @param messages list with the messages
     */
    fun deliverDebugMessages(messages : List<Pair<String, MessageType>>){
        messages.forEach{ message -> communicator.sendDebugMessage(message.first, message.second) }
    }


    /**
     * Orders the server to shutdown the process it keeps, clean up and shutdown itself
     */
    override fun kill(): Reply {
        socket.send(Gson().toJson(Request(MessageTypes.KILL)))
        return Gson().fromJson(socket.recvStr(), Reply::class.java)
    }
}