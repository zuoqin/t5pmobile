module SQL

open System;
open System.Net
open System.Net.Mail
open System.IO
open FSharp.Configuration
open FSharp.Data
open System.Collections.Generic



type Organization = {orgid:int; orgcode:string; english:string; chinese : string; id : int}
type Position = {positionid:int; english:string; chinese : string; orgid : int; id : int}

[<Literal>]
let connectionString = @"name=HRMS"

let organizationsmap = new Dictionary<int, Organization>()
let positionsmap = new Dictionary<int, Position>()


let getOrganizations (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"SELECT orgid, orgcode, english, chinese from organization",connectionString>()
        
        let items = cmd.Execute()        
        for item in items do
            let newOrganization = { orgid = item.orgid; 
                    orgcode = item.orgcode; english = item.english; chinese = item.chinese; id = newid}
            let bResult = organizationsmap.Add(item.orgid, newOrganization)
            outFile.WriteLine( sprintf "{ :organization/english \"%s\", :organization/chinese \"%s\", :db/id #db/id[:db.part/user %d]" item.english item.chinese newid)
            newid <- newid - 1
    outFile.Close();
    newid

let getPositions (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"SELECT positionid, orgid, positioncode, english, chinese from position",connectionString>()
        
        let items = cmd.Execute()
        for item in items do
            let newPosition = { positionid = item.positionid; 
                    orgid = item.orgid; english = item.english; chinese = item.chinese; id = newid}
            positionsmap.Add(item.positionid, newPosition)
            let (bResult, theOrganization) = organizationsmap.TryGetValue(item.orgid)
            if bResult = true then
                outFile.WriteLine( sprintf "{ :position/english \"%s\", :position/chinese \"%s\", :position/organization #db/id[:db.part/user %d] :db/id #db/id[:db.part/user %d]" item.english item.chinese theOrganization.id newid)
                newid <- newid - 1
    outFile.Close()
    newid