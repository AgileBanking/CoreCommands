package services.commons

import grails.plugins.rest.client.RestBuilder
import grails.converters.JSON

abstract class BaseController {
    static allowedMethods = [
        index:              "GET",
        create:             "POST",
        update:             "PUT", 
        delete:             "DELETE",
        undelete:           "DELETE"]
    
    def renderService
    def buildLinksService  
    def sysConfigService
    
    def index()  {
        render (text:"Choose 'create', 'update' or '[un]delete'", status:400, condentType:"application/json" )
    }
    
    private sourceComponent() {"Commons"}
//    private casheControl() {"public, max-age=5" } // 72000 for 20 hours
    def casheControl() {"private, no-cache, no-store" }
      
    def create() {
//println "\n1 create: $params"       
        params.sourceURI = "/$params.controller/save"   //internal request to domains
//println "2 create: $params"        
//println "2 body: $request.JSON"
        params.hide = ["id", "version"]
        postNow()        
    }
    
    def update(String id) {
//println "\n3 update: $params" 
//println "update-body=" + request.JSON
        params.sourceURI = "/$params.controller/update/{id}.json".replace("{id}", "$id")    //internal request to domains
//        println "params.sourceURI: $params.sourceURI"
//println "4 update: $params"      
//println "4 body: $request.JSON"
        params.hide = ["id", "version"]
        postNow()         
    }
    
    def delete(Long id) {  
//        printlnn "from Delete"
        params.sourceURI = "/$params.controller/delete?id=$id"   //internal request to domains
        postNow()         
    }
    
    def undelete(Long id) {
//        printlnn "from Undelete"
        params.sourceURI = "/$params.controller/undelete?id=$id"   //internal request to domains
        postNow()               
    }
    
    private postNow() {
        params."Cashe-Control" = casheControl()
        params.sourceComponent=sourceComponent()
//println "params.sourceComponent=$params.sourceComponent" 
        params.host = renderService.hostApp() //(request) 
        params.URL =  renderService.URL(request)         
        def answer = renderService.prepareAnswer(params, request) 
//        println "Returned from prepareAnswher with " + answer
//        println "status=$params.status"
        // add to the answer the related links to complete the REST constrain HATOES
//        if ((params.withlinks ? params.withlinks.toLowerCase() : true ) != "false"  ) {
//            answer.links += buildLinksService.controllerLinks(params, request)
//            answer.links += extraLinks() 
//        }
        
        response.setHeader("Cache-Control",params."Cashe-Control")
        response.status = params.status
//        println "respons = ${response}"
        // Keep Audit
        
        try {
            def auditor = sysConfigService.getComponent("Auditor")
//            println "auditor: $auditor, isActive = ${auditor.isActive}" 
            if (auditor.isActive) { 
                // store in the auditdb (CouchDB)
                def restAudit = new RestBuilder()
//                def url = auditor.dbServer + "/$params.reqID"
                def url = "http://auditdb:5984/auditdb/$params.reqID"
//                println "auditdb=$url"
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
        params.host = renderService.hostApp(request)
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