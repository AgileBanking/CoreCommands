package services.products

import grails.plugins.rest.client.RestBuilder
import grails.converters.JSON
abstract class BaseController {
    static allowedMethods = [
        index:              "GET",
        create:             "PUT",
        update:             "POST", 
        delete:             "DELETE",
        undelete:           "DELETE"]
    
    def RenderService
    def BuildLinksService  
    
    def index()  {
        render (text:"Choose 'create', 'update' or '[un]delete'", status:400, condentType:"application/json" )
    }
    
    private sourceComponent() {"Products"}
//    def casheControl() {"public, max-age=7200" } // 2 hours
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
    
    private postNow() {
        params.sourceComponent=sourceComponent()
        params.host = RenderService.hostApp(request) 
        params.URL =  RenderService.URL(request)         
        def answer = RenderService.prepareAnswer(params, request) 
        if ((params.withlinks ? params.withlinks.toLowerCase() : true ) != "false"  ) {
            answer.links += BuildLinksService.controllerLinks(params, request)
            answer.links += extraLinks() 
        }
        response.status = params.status
        // Keep Audit
        try {
            def auditor = SysConfigService.getComponent("Auditor")
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
        params.host = RenderService.hostApp(request)
        params.links = BuildLinksService.controllerLinks(params, request)
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