# b-language-server
A language server implementation for B, using ProB as backend, based on Java/Kotlin. This language server comes with his
own version of probcli.

## Usage
After starting the server; either from an IDE via main or by building and executing a shadowjar, a language client can 
connect to it via port 55555.

## Capabilities

- Calling ProB Java Kernel with additional options
- Options need to be specified by the client; They are requested by the server
- Options are: 
    *   val strictChecks : Boolean = true  
    *   val wdChecks : Boolean = true
    *   val performanceHints : Boolean = true 
    *   val probHome : String = "DEFAULT"
    *   val debugMode : Boolean = true
    
- Setting probHome to a different target then DEFAULT can cause all sorts of bad stuff. Ensure that the path points to the
correct directory. Note that any probcli other version then the version used by prob java kernel can cause uncaught errors.
If you want to use another version probcli point to the directory, not to probcli.sh e.g.:
```
/home/sebastian/prob_prolog
```
    
## Clients

- VSCode: https://marketplace.visualstudio.com/items?itemName=SeeBasTStick.b-language-extension