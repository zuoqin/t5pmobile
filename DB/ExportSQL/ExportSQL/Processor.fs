module Processor

// asynchronious client messages for CRUD operations
type CRUDBlogMessage = 
    | ExportOrganizations of string * int * AsyncReplyChannel<int>
    | ExportPositions of string * int * AsyncReplyChannel<int>
    | ExportEmployees of string * int * AsyncReplyChannel<int>
    | ExportNewEmployees of string * int * AsyncReplyChannel<int>
    | ExportPayrollGroups of string * int * AsyncReplyChannel<int>
    | ExportUsers of string * int * AsyncReplyChannel<int>
    | ExportSysMenu of string * int * AsyncReplyChannel<int>
    | ExportMessages of string * int * AsyncReplyChannel<int>


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

        | ExportNewEmployees ( outputFile, id, reply ) ->
            let id = SQL.getNewEmployees(outputFile, id)
            reply.Reply id

        | ExportPayrollGroups ( outputFile, id, reply ) ->
            let id = SQL.getPayrollGroups(outputFile, id)
            reply.Reply id

        | ExportUsers ( outputFile, id, reply ) ->
            let id = SQL.getUsers(outputFile, id)
            reply.Reply id

        | ExportSysMenu ( outputFile, id, reply ) ->
            let id = SQL.getSysMenu(outputFile, id)
            reply.Reply id

        | ExportMessages ( outputFile, id, reply ) ->
            let id = SQL.getMessages(outputFile, id)
            reply.Reply id

        return! loop () }
    loop () )      