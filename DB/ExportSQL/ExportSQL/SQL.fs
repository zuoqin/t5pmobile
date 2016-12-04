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
type Employee = {empid:int; payrollgroupid:int; english:string; chinese : string; birthday : DateTime; id : int}
type New_Employee = {empid:int; empcode:string;english:string;chinese:string;hirestatus:string;birthday:DateTime;gender:string;major:string;    
    degree:string;hiredate:DateTime;positioncode:string;orgcode1:string;orgcode2:string;orgcode3:string;orgcode4:string;id:int
    }
type User = {userid:int; empid:int; usercode:string; userpassword : string; datemask:string; timemask:string;language:int; id : int}
type SysMenu = {menucode:string; menulevel:int; menuopt:int; menuorder:int; english:string; chinese:string;submenu:string; urltarget:string; id : int}
type Message = {messageid:int64; sender:int; senddate:DateTime; english:string; chinese:string;body:string; status:byte; id : int}

[<Literal>]
let connectionString = @"name=HRMS"

let payrollgroupsmap = new Dictionary<int, PayrollGroup>()
let organizationsmap = new Dictionary<int, Organization>()
let positionsmap = new Dictionary<string, Position>()
let employeesmap = new Dictionary<int, Employee>()
let newemployeesmap = new Dictionary<int, New_Employee>()
let usersmap = new Dictionary<int, User>()

let getPayrollGroups (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"
            select 0 as payrollgroupid,
            'system' as english, N'系统' as chinese
            union
            SELECT payrollgroupid, english, chinese from payrollgroups",connectionString>()
        
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
                outFile.WriteLine( sprintf "{ :position/positioncode \"%s\" :position/english \"%s\", :position/chinese \"%s\", :position/organization #db/id[:db.part/user %d] :db/id #db/id[:db.part/user %d] }"
                    item.positioncode item.english item.chinese theOrganization.id newid)
                newid <- newid - 1
    outFile.Close()
    newid


let getNewEmployees (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"
        SELECT a.empid,a.empcode,a.english, a.chinese, a.hirestatus,a.birthday,a.gender,
        a.major,a.degree,a.hiredate,a.positioncode,a.orgcode1,a.orgcode2,a.orgcode3,a.orgcode4,
        a.payrollgroupid
        FROM emp_new a
        WHERE a.template=0 AND a.hirestatus<>'09'  ORDER BY a.empcode",connectionString>()
        
        let items = cmd.Execute()
        for item in items do

            let mutable birthdate = DateTime()
            match item.birthday with 
            | Some x -> birthdate <- x
            | None -> birthdate <- DateTime()

            let mutable hiredate = DateTime()
            match item.hiredate with 
            | Some x -> hiredate <- x
            | None -> hiredate <- DateTime()

            let bResult = employeesmap.Remove(item.empid)
            

            let newEmployee = { empid = item.empid; empcode = item.empcode; english = item.english; chinese = item.chinese; hirestatus=item.hirestatus;
                birthday = birthdate; gender=item.gender; major=item.major;
                degree=item.degree; hiredate=DateTime(); positioncode=item.positioncode;
                orgcode1=item.orgcode1; orgcode2=item.orgcode2;
                orgcode3=item.orgcode3; orgcode4=item.orgcode4;id=newid
                }

            newemployeesmap.Add(item.empid, newEmployee)

            let mutable payrollgroupid = 0
            let (bResult, thePayrollGroup) = payrollgroupsmap.TryGetValue(item.payrollgroupid)
            if bResult = true then
                payrollgroupid <- thePayrollGroup.id 

            let birthdateStr = birthdate.ToString("o")
            let hiredateStr = hiredate.ToString("o")

            outFile.WriteLine( sprintf "{ :emp_new/english \"%s\", :emp_new/chinese \"%s\", :emp_new/empcode \"%s\",
                :emp_new/positioncode \"%s\" :emp_new/payrollgroup  #db/id[:db.part/user %d]  :emp_new/birthday #inst \"%s\"  
                :emp_new/degree \"%s\" :emp_new/major \"%s\" :emp_new/hirestatus \"%s\" :emp_new/hiredate #inst \"%s\"
                :emp_new/orgcode1 \"%s\" :emp_new/orgcode2 \"%s\" :emp_new/orgcode3 \"%s\"  :emp_new/orgcode4 \"%s\" 
                :emp_new/gender \"%s\" :db/id #db/id[:db.part/user %d] }"
                item.english item.chinese item.empcode item.positioncode payrollgroupid birthdateStr 
                item.degree item.major item.hirestatus hiredateStr 
                item.orgcode1 item.orgcode2 item.orgcode3 item.orgcode4
                item.gender newid)
            newid <- newid - 1

    outFile.Close()
    newid



let getEmployees (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"SELECT 0 as empid, '' as portrait,
            (select min(payrollgroupid) from payrollgroups) as payrollgroupid,
            'system' as english, N'系统' as chinese, '1900-01-01' as birthday
            union
            select empid, portrait, payrollgroupid, english, chinese, birthday from emphr ",connectionString>()
        
        let items = cmd.Execute()
        for item in items do

            let ispayrollgrouid = 
                match item.payrollgroupid with 
                | Some x -> x
                | None -> 0

            let mutable birthdate = DateTime()
            match item.birthday with 
            | Some x -> birthdate <- x
            | None -> birthdate <- DateTime()


            let bResult = employeesmap.Remove(item.empid)
            let newEmployee = { empid = item.empid; payrollgroupid = ispayrollgrouid; english = item.english; chinese = item.chinese; birthday = birthdate; id = newid}
            employeesmap.Add(item.empid, newEmployee)

            use positionCmd = new SqlCommandProvider<"SELECT positioncode from empposition where empid = @empid",connectionString>()
            let positions = positionCmd.Execute(empid = item.empid)

            let mutable strPositions = "["
            for position in positions do
                let (bResult, thePosition) = positionsmap.TryGetValue(position)
                if bResult = true then
                    strPositions <- sprintf "%s #db/id[:db.part/user %d]" strPositions thePosition.id

            strPositions <- strPositions + "]"


            let (bResult, thePayrollGroup) = payrollgroupsmap.TryGetValue(ispayrollgrouid)
            if bResult = true then


                let birthdateStr = birthdate.ToString("o")

                outFile.WriteLine( sprintf "{ :employee/english \"%s\", :employee/chinese \"%s\", :employee/positions %s,
                    :employee/portrait \"%s\",
                    :employee/payrollgroup #db/id[:db.part/user %d],  :employee/birthday #inst \"%s\",  :db/id #db/id[:db.part/user %d] }"
                    item.english item.chinese strPositions item.portrait thePayrollGroup.id birthdateStr newid)
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

let getRecipientType (recipienttype:int) :string =
    match recipienttype with
    | 0 -> ":recipient.type/to"
    | 1 -> ":recipient.type/cc"
    | _ -> ":recipient.type/bcc" 

let getMessages (outputFile : string, id : int) : int = 
    let outFile = new StreamWriter(outputFile, true)
    let mutable newid = id
    do
        use cmd = new SqlCommandProvider<"
            SELECT --TOP 20 
                m.messageid,
                m.senderempid sender,
                m.senddate,           
                m.subject_english english,
                m.subject_chinese chinese,
                m.body,    
                m.[status]
            FROM
                [messages] m
            WHERE
                m.[status] > 1
            ORDER BY
                m.senddate DESC",connectionString>()
        
        let items = cmd.Execute()
        for item in items do
//            let mutable receiver = 0
//            match item.receiver with 
//            | Some x -> receiver <- x
//            | None -> receiver <- 0

            
            let mutable body = ""
            match item.body with 
            | Some x -> body <- x.Replace("\\", "\\\\")
            | None -> body <- ""

            body <- body.Replace("\"", "\\\"")
            let mutable senddate = DateTime()
            match item.senddate with 
            | Some x -> senddate <- x
            | None -> senddate <- DateTime()
            let newMenu : Message = { messageid = item.messageid; sender = item.sender; senddate = senddate; body=body;
                english=item.english; chinese=item.chinese; status=item.status; id = newid}

            let (bResult, theSender) = employeesmap.TryGetValue(item.sender)
            if bResult = true then
                use recipientCmd = new SqlCommandProvider<"SELECT empid, recipienttype from messagerecipient where messageid = @messageid",connectionString>()
                let recipients = recipientCmd.Execute(messageid = item.messageid)

                let mutable strRecipients = "["
                //let mutable strCCRecipients = "["
                //let mutable strBccRecipients = "["
                for receipient in recipients do
                    let (bResult, theReceiver) = employeesmap.TryGetValue(receipient.empid)
                    if bResult = true then
                        let recipienttype = getRecipientType((int)receipient.recipienttype)
                        outFile.WriteLine( sprintf "{ :recipient/employee #db/id[:db.part/user %d]  :recipient/type %s :db/id #db/id[:db.part/user %d] }"
                            theReceiver.id recipienttype newid)
                        

                        strRecipients <- sprintf "%s #db/id[:db.part/user %d]" strRecipients newid
                        newid <- newid - 1

                strRecipients <- strRecipients + "]"
                //strCCRecipients <- strCCRecipients + "]"
                //strBccRecipients <- strBccRecipients + "]"

                let senddateStr = senddate.ToString("o")

                outFile.WriteLine( sprintf "{ :message/sender #db/id[:db.part/user %d] :message/recipients %s :message/senddate #inst \"%s\" :message/english \"%s\" :message/chinese \"%s\" :message/body \"%s\" :message/status %d :db/id #db/id[:db.part/user %d] }"
                    theSender.id strRecipients senddateStr  item.english item.chinese body item.status newid)
                newid <- newid - 1

    outFile.Close()
    newid