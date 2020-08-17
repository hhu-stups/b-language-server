package b.language.server.proBMangement

import b.language.server.communication.CommunicatorInterface
import b.language.server.proBMangement.prob2.ProBKernelManager


/**
 * Generate new Instances of ProBKernelManger
 */
class ProBFactory {

    fun getProBAccess(commuicator : CommunicatorInterface) : ProBInterface{
        return ProBKernelManager(commuicator)
    }

}