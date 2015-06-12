package services.cas
import grails.converters.JSON

class SecurityController  extends BaseController {
//    def renderService
//    def buildLinksService
//    def SysConfigService
    
    static allowedMethods = [
        index: "GET",
        login: "PUT"
    ]     
    
    def index()  {
        render (text:"Security is accessed via REST methods only.", status:400, condentType:"application/json" )
    }
    
    
//    def login(String username, String password) {
//        params.sourceURI = "/$params.controller/login.json?username=$username&password=$password"   
//        postNow()          
//    }    
    def login(String username, String password) {
        // ../CoreCommands/Security/login?username<username>&password=<password>
println "raw params: $params"         
        if (username==null || password==null ){
            response.status = 400 // 400 Bad Request
            def answer = ["error":["status":"400","notes" : "expected credentials"]]
            render answer as JSON
        }
        else {
            def uri = "/security/login?username=$username&password=$password"  //internal requestt to domains
            params.sourceComponent=sourceComponent()
            params.sourceURI="$uri" 
            params.host = renderService.hostApp()// renderService.hostApp(request)
            params.URL =  renderService.URL(request) 
            params.URL += "?username=$username&password=$password \n" 
            params.links = buildLinksService.controllerLinks(params, request)
            params.links += extraLinks()
println "before postNow params: $params" 
            postNow()
            
            }
        }
        
    
        def extraLinks() { 
        def controllerURL = "http://$params.host/$params.controller"
        def links = [:]
        links += ["login":["template":true, "fields": ["username":"String", "password":"String"], "href":  "$controllerURL/login?username={username}&password={password}"]]
        println links
        return links 
    }   

}
