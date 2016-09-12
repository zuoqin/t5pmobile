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
type User = {userid:int; empid:int; usercode:string; userpassword : string; datemask:string; timemask:string;language:int; id : int}
type SysMenu = {menucode:string; menulevel:int; menuopt:int; menuorder:int; english:string; chinese:string;submenu:string; urltarget:string; id : int}

[<Literal>]
let connectionString = @"name=HRMS"

let payrollgroupsmap = new Dictionary<int, PayrollGroup>()
let organizationsmap = new Dictionary<int, Organization>()
let positionsmap = new Dictionary<string, Position>()
let employeesmap = new Dictionary<int, Employee>()
let usersmap = new Dictionary<int, User>()

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
            let bResult = employeesmap.Remove(item.empid)
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

let getUsers (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"SELECT userid, empid, usercode, userpassword, datemask, timemask, language from users",connectionString>()
        
        let items = cmd.Execute()
        for item in items do
            let newUser : User = { userid = item.userid; empid = item.empid; usercode = item.usercode; userpassword = item.userpassword;
                datemask=item.datemask; timemask=item.timemask; language=item.language; id = newid}
            usersmap.Add(item.userid, newUser)

            let (bResult, theEmployee) = employeesmap.TryGetValue(item.empid)
            
            if bResult = true then
                outFile.WriteLine( sprintf "{ :user/code \"%s\", :user/password \"%s\" :user/datemask \"%s\" :user/timemask \"%s\" :user/language %d :user/employee  #db/id[:db.part/user %d] :db/id #db/id[:db.part/user %d] }"
                    item.usercode item.userpassword item.datemask item.timemask item.language theEmployee.id newid)
                newid <- newid - 1

    outFile.Close()
    newid



let getSysMenu (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"SELECT [menucode], [menuopt], [english], [chinese],[submenu] ,[menulevel],[menuorder],
            Lower(
            case
            when
            CHARINDEX('.',

            case
            when CHARINDEX ( '/', REVERSE(urltarget)) > 0 then
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)) -1)
            else 
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)))
            end) > 0 then 



            LEFT( case
            when CHARINDEX ( '/', REVERSE(urltarget)) > 0 then
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)) -1)
            else 
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)))
            end,
            CHARINDEX('.',

            case
            when CHARINDEX ( '/', REVERSE(urltarget)) > 0 then
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)) -1)
            else 
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)))
            end) -1)

            else
            LEFT( case
            when CHARINDEX ( '/', REVERSE(urltarget)) > 0 then
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)) -1)
            else 
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)))
            end,
            CHARINDEX('.',

            case
            when CHARINDEX ( '/', REVERSE(urltarget)) > 0 then
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)) -1)
            else 
	            RIGHT(urltarget, CHARINDEX ( '/', REVERSE(urltarget)))
            end) )
            end) as urltarget
        FROM [menus] a WHERE a.menucode<>'FAVORITE'
        AND a.submenu<>'FAVORITE' AND a.menucode<>'ESS' AND a.submenu<>'ESS' and (a.menucode + '_' +convert(nvarchar, a.menuopt) <> 'PAYROLL_9' )
        ORDER BY a.menulevel,a.menucode,CASE WHEN a.menuopt<1000 AND a.menucode='WEBSITE' THEN 0-a.menuorder ELSE a.menuorder END",connectionString>()
        
        let items = cmd.Execute()
        for item in items do
            let mutable url = ""
            match item.urltarget with 
            | Some x -> url <- x
            | None -> url <- ""
            let newMenu : SysMenu = { menucode = item.menucode; menulevel = item.menulevel; menuopt = item.menuopt; menuorder=item.menuorder;
                english=item.english; chinese=item.chinese; submenu=item.submenu; urltarget=url;id = newid}

            outFile.WriteLine( sprintf "{ :sysmenu/menucode \"%s\", :sysmenu/menulevel %d :sysmenu/menuopt %d :sysmenu/menuorder % d :sysmenu/chinese \"%s\" :sysmenu/english \"%s\" :sysmenu/submenu \"%s\" :sysmenu/urltarget \"%s\" :db/id #db/id[:db.part/user %d] }"
                item.menucode item.menulevel item.menuopt item.menuorder item.chinese item.english item.submenu url newid)
            newid <- newid - 1

    outFile.Close()
    newid