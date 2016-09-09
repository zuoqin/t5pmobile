module SQL

open System;
open System.Net
open System.Net.Mail
open System.IO
open FSharp.Configuration
open FSharp.Data
open System.Collections.Generic


type PayrollGroup = {payrollgroupid:int; english:string; chinese : string; id : int}
type Organization = {orgid:int; orgcode:string; english:string; chinese : string; id : int}
type Position = {positionid:int; positioncode:string; english:string; chinese : string; orgid : int; id : int}
type Employee = {empid:int; payrollgroupid:int; english:string; chinese : string; id : int}

[<Literal>]
let connectionString = @"name=HRMS"

let payrollgroupsmap = new Dictionary<int, PayrollGroup>()
let organizationsmap = new Dictionary<int, Organization>()
let positionsmap = new Dictionary<string, Position>()
let employeesmap = new Dictionary<int, Employee>()

let getPayrollGroups (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"SELECT payrollgroupid, english, chinese from payrollgroups",connectionString>()
        
        let items = cmd.Execute()        
        for item in items do
            let newPayrollGroup : PayrollGroup = { payrollgroupid = item.payrollgroupid; 
                    english = item.english; chinese = item.chinese; id = newid}
            let bResult = payrollgroupsmap.Add(item.payrollgroupid, newPayrollGroup)
            outFile.WriteLine( sprintf "{ :payrollgroup/english \"%s\", :payrollgroup/chinese \"%s\", :db/id #db/id[:db.part/user %d] }" item.english item.chinese newid)
            newid <- newid - 1
    outFile.Close();
    newid

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
            outFile.WriteLine( sprintf "{ :organization/english \"%s\", :organization/chinese \"%s\", :organization/orgcode \"%s\", :db/id #db/id[:db.part/user %d] }"
                item.english item.chinese item.orgcode newid)
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
            let newPosition = { positionid = item.positionid; positioncode = item.positioncode;
                    orgid = item.orgid; english = item.english; chinese = item.chinese; id = newid}
            let bResult = positionsmap.Remove(item.positioncode)
            positionsmap.Add(item.positioncode, newPosition)
            
            let (bResult, theOrganization) = organizationsmap.TryGetValue(item.orgid)
            if bResult = true then
                outFile.WriteLine( sprintf "{ :position/english \"%s\", :position/chinese \"%s\", :position/organization #db/id[:db.part/user %d] :db/id #db/id[:db.part/user %d] }"
                    item.english item.chinese theOrganization.id newid)
                newid <- newid - 1
    outFile.Close()
    newid


let getEmployees (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"SELECT empid, payrollgroupid, english, chinese from emphr",connectionString>()
        
        let items = cmd.Execute()
        for item in items do
            let newEmployee = { empid = item.empid; payrollgroupid = item.payrollgroupid; english = item.english; chinese = item.chinese; id = newid}
            employeesmap.Add(item.empid, newEmployee)

            use positionCmd = new SqlCommandProvider<"SELECT positioncode from empposition where empid = @empid",connectionString>()
            let positions = positionCmd.Execute(empid = item.empid)

            let mutable strPositions = "["
            for position in positions do
                let (bResult, thePosition) = positionsmap.TryGetValue(position)
                if bResult = true then
                    strPositions <- sprintf "%s #db/id[:db.part/user %d]" strPositions thePosition.id

            strPositions <- strPositions + "]"


            let (bResult, thePayrollGroup) = payrollgroupsmap.TryGetValue(item.payrollgroupid)
            if bResult = true then
                outFile.WriteLine( sprintf "{ :employee/english \"%s\", :employee/chinese \"%s\", :employee/positions %s :employee/payrollgroup  #db/id[:db.part/user %d] :db/id #db/id[:db.part/user %d] }"
                    item.english item.chinese strPositions thePayrollGroup.id newid)
                newid <- newid - 1

    outFile.Close()
    newid