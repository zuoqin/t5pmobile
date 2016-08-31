module Processor

// asynchronious client messages for CRUD operations
type CRUDBlogMessage = 
    | ExportOrganizations of string * int * AsyncReplyChannel<int>
    | ExportPositions of string * int * AsyncReplyChannel<int>
    | ExportEmployees of string * int * AsyncReplyChannel<int>


let crud = MailboxProcessor.Start(fun agent ->             
    let rec loop () : Async<unit> = async {
        let! msg = agent.Receive()
        match msg with 
        | ExportOrganizations ( outputFile, id, reply ) ->                      
            let id = SQL.getOrganizations(outputFile, id)
            reply.Reply id
                    
        | ExportPositions ( outputFile, id, reply ) ->
            let id = SQL.getPositions(outputFile, id)
            reply.Reply id

        | ExportEmployees ( outputFile, id, reply ) ->
            let id = SQL.getEmployees(outputFile, id)
            reply.Reply id


        return! loop () }
    loop () )      