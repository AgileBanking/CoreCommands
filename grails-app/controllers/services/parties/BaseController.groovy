package services.parties
import grails.plugins.rest.client.RestBuilder
import grails.converters.JSON
abstract class BaseController {
    static allowedMethods = [
        index:              "GET",
        create:             "POST",
        update:             "PUT", 
        delete:             "DELETE",
        undelete:           "DELETE"]
    
    def index()  {
        render (text:"Choose 'create', 'update' or '[un]delete'", status:400, condentType:"application/json" )
    }
    
    private sourceComponent() {"Parties"}
//    private casheControl() {"public, max-age=5" } // 72000 for 20 hours
    def casheControl() {"private, no-cache, no-store" }
    
    def xRenderService
    def xBuildLinksService     
    
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
        params.sourceURI = "/$params.controller/delete.json?id=$id"   //internal request to domains
        postNow()         
    }
    
    def undelete(Long id) {
        params.sourceURI = "/$params.controller/undelete.json?id=$id"   //internal request to domains
        postNow()               
    }
    
    private postNow() {
        params.sourceComponent=sourceComponent()
        params.host = xRenderService.hostApp(request) 
        params.URL =  xRenderService.URL(request)         
        def answer = xRenderService.prepareAnswer(params, request) 
        if ((params.withlinks ? params.withlinks.toLowerCase() : true ) != "false"  ) {
            answer.links += xBuildLinksService .controllerLinks(params, request)
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
        
        render (text:answer as JSON, status:params.status, ETag:params.ETag) //,"Cache-Control":params."Cashe-Control")         
//        respond answer, [status:params.status , contentType: "application/json"]
    }
    
    def relatedLinks() {
        params.host = xRenderService.hostApp(request)
        params.links = xBuildLinksService .controllerLinks(params, request)
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
