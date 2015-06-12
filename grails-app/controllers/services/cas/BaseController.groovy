package services.cas

import grails.plugins.rest.client.RestBuilder
import grails.converters.JSON
abstract class BaseController {
    static allowedMethods = [
        index:              "GET",
        create:             "POST",
        update:             "PUT", 
        delete:             "DELETE",
        undelete:           "DELETE",
        login:              "PUT"]
    
    def renderService
    def buildLinksService  
    def sysConfigService
    
    def index()  {
        render (text:"Choose 'create', 'update' or '[un]delete'", status:400, condentType:"application/json" )
    }
    
    private sourceComponent() {"CAS"}
//    private casheControl() {"public, max-age=5" } // 72000 for 20 hours
    def casheControl() {"private, no-cache, no-store" }
      
    def create() {
        params.sourceURI = "/$params.controller/save.json"   //internal request to domains
        params.hide = ["id", "version"]
        postNow()        
    }
    
    def update() {
        params.sourceURI = "/$params.controller/update.json"   //internal request to domains
        params.hide = ["id", "version"]
        postNow()         
    }
    
    def delete(Long id) {  
//        println "from Delete"
        params.sourceURI = "/$params.controller/delete.json?id=$id"   //internal request to domains
        postNow()         
    }
    
    def undelete(Long id) {
//        println "from Undelete"
        params.sourceURI = "/$params.controller/undelete.json?id=$id"   //internal request to domains
        postNow()               
    }
    
    def login(String username, String password) {
        params.sourceURI = "/$params.controller/login.json?username=$username&password=$password"   
        postNow()          
    }
    
    private postNow() {
        params.sourceComponent=sourceComponent()
        params.host = sysConfigService.getComponentHost("CoreCommands") // renderService.hostApp() //  renderService.hostApp() // (request) 
//        println "params.host: " + params.host
        params.URL =  renderService.URL(request)      
println "postNow.params: $params"
        def answer = renderService.prepareAnswer(params, request) 
println "status=$params.status"
        response.status = params.status        
        if ((params.withlinks ? params.withlinks.toLowerCase() : true ) != "false"  ) {
            answer.links += buildLinksService.controllerLinks(params, request)
            answer.links += extraLinks() 
        }
 
        // Keep Audit
        try {
            def auditor = sysConfigService.getComponent("Auditor")
            if (auditor.component.isActive) { 
                // store in the auditdb (CouchDB)
                def restAudit = new RestBuilder()
                def url = auditor.component.baseURL + "/$params.reqID"
                answer.header.auditRec = "$url"
                def respAudit = restAudit.put("$url"){
                    contentType "application/json"
                    json {["header":answer.header, "body":answer.body]} 
                }
                answer.links += ["audit":["href" : "$url"]]
            }            
        }
        catch(Exception e3) {
            answer.header.error="$e3.message" 
        }
        // sort entries keeping the top entries as ordered
        answer.header = answer.header.sort {it.key}
        answer.body = answer.body.sort {it.key}
        if (answer.links) {answer.links = answer.links.sort {it.key}}
        
        render (text:answer as JSON, status:params.status, ETag:params.ETag) //,"Cache-Control":params."Cashe-Control")         
//        respond answer, [status:params.status , contentType: "application/json"]
    }
    
    def relatedLinks() {
        params.host = renderService.hostApp() // request)
        params.links = buildLinksService.controllerLinks(params, request)
        def result = [:]
        result.controller = params.controller 
        params.links += ["self": ["href": "$params.host/$result.controller/relatedLinks"]]
        params.links += extraLinks()
        result.links= params.links
        render result as JSON
    }     
    
    // returns an array with extra links to be attached in the response by the caller
    def extraLinks() {
        def links = [:]
    }    
}