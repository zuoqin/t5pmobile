open System;
open System.Net
open System.Net.Mail
open System.IO
open FSharp.Configuration
open FSharp.Data
open System.Collections.Generic
open SQL



type Settings = AppSettings<"App.config">
let OutputFile : string = Settings.OutputFile
let mutable id = -1000001

  


[<EntryPoint>]
let main argv = 
    
    let outFile = new StreamWriter(OutputFile, false)
    outFile.WriteLine( sprintf "[")
    outFile.Close()
    let mutable id = -100001
    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportOrganizations(OutputFile, id, reply) )

    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportPositions(OutputFile, id, reply) )
    
    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportPayrollGroups(OutputFile, id, reply) )

    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportEmployees(OutputFile, id, reply) )

    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportNewEmployees(OutputFile, id, reply) )

    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportUsers(OutputFile, id, reply) )

    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportSysMenu(OutputFile, id, reply) )

    id <- Processor.crud.PostAndReply( fun reply -> Processor.ExportMessages(OutputFile, id, reply) )

    Console.WriteLine(organizationsmap.Count)
    let outFile = new StreamWriter(OutputFile, true)
    outFile.WriteLine( sprintf "]")
    outFile.Close()
    0 // return an integer exit code
