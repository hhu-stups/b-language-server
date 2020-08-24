package b.language.server.proBMangement.prob

import ProBKernel
import b.language.server.communication.CommunicationCollector
import b.language.server.communication.Communicator
import b.language.server.dataStorage.ProBSettings
import b.language.server.dataStorage.Settings
import com.google.gson.Gson
import com.google.inject.Guice
import com.google.inject.Stage
import org.eclipse.lsp4j.MessageType
import org.zeromq.SocketType

import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.net.URI

/**
 * Creates the prob kernel access and maintaince a stady connection
 */
class ProBKernelManager : Thread() {

    private var active = true
    private lateinit var kernel : ProBKernel
    private var probHome = ""
    private val communicator = CommunicationCollector()



    override fun run() {
        ZContext().use { context ->
            val socket: ZMQ.Socket = context.createSocket(SocketType.REP)
            socket.bind("tcp://*:5557")
            kernel = setup()
            while (active) {
                val message = socket.recvStr()
                val request = Gson().fromJson(message, Request::class.java)
                Communicator.sendDebugMessage("manageer got request... " , MessageType.Info)

                val reply = if (request.type==MessageTypes.CHECK){
                    check(request.uri, request.settings)
                }else{
                    kill()
                }
                System.err.println("sending result back")
                socket.send(Gson().toJson(reply))
            }
        }
    }



    fun check(uri : String, settings : Settings) : Reply{
        val path = URI(uri).path
        System.err.println("start prob")
        System.err.println("checking file $path")
        Communicator.sendDebugMessage("checking document", MessageType.Info)

        val result = kernel.check(path, ProBSettings(settings.wdChecks, settings.strictChecks, settings.performanceHints))
        System.err.println("returning result")
        return Reply(result.first, result.second)
    }

    fun kill() : Reply{
        active = false
        return Reply(listOf(), listOf())
    }


    /**
     * @param probHome the home of prob; DEFAULT for the prob version delivered with the library
     * @return an instance of prob kernel
     */
    fun setup() : ProBKernel {

    //    Communicator.sendDebugMessage("creating injector...", MessageType.Info)

        System.err.println("Creating injector in thread " + this.id)
        val injector = Guice.createInjector(Stage.PRODUCTION, ProBKernelModule())
        System.err.println("Done " + this.id)

      //  Communicator.sendDebugMessage("..done", MessageType.Info)


        val kernel : ProBKernel
        try{
            kernel = injector.getInstance(ProBKernel::class.java)
        }catch (e : de.prob.exception.CliError){
  //          Communicator.sendDebugMessage("wrong path to prob", MessageType.Error)
            throw Exception("wrong path to prob $probHome")
        }
    //    Communicator.sendDebugMessage("returning kernel", MessageType.Info)

        return kernel
    }
}