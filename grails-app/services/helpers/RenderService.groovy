package helpers
import grails.plugins.rest.client.RestBuilder
import grails.converters.*
import java.security.MessageDigest 

class RenderService {
    def SysConfigService
    static transactional = false
    
    def prepareAnswer(params, request) {
        def baseURL = SysConfigService.getComponent(params.sourceComponent).component.baseURL
//        def baseURL = entities.Component.findByName(params.sourceComponent).baseURL
        params.reqID = UUID.randomUUID().toString()
        params.Date = new Date().toString()
        params.method = request.method.toUpperCase()
        
        // prepare hyperlinks    
        def href = "$params.URL"
        def links = ["self": ["href": "$params.URL"]]
        if (href.contains("?")) {
            links.self += ["alt_href": "$href" + "&withlinks=false"]
        }
        else {
            links.self += ["alt_href": "$href" + "?withlinks=false"]
        }
        if (params.links != null) { links += params.links }
        // clean params
        params.remove("links")
        params.remove("URL")
        // define variables
        def answer = [:]
        def xref = ""
        def resp, s
        def rest = new RestBuilder()
//        params.error = "none"
        try {        
            resp = rest.get("$baseURL$params.sourceURI") { 
                accept "application/json"
                contentType "application/json"
                } 
            params.status = resp.status 
            def etag = ""
            if (resp.json != null) {
                if (resp.status < 300 && resp.json.class != null) { 
                    resp.json.remove("class") 
                }
                if (params.hide) {
                    params.hide.each {
                        resp.json.remove("$it") 
                    }
                    params.remove("hide")
                }    
            }
            else {
                etag = makeEtag(resp.json.toString())
            }
            // If not modified return nothing  
            def rETag = request.getHeader("If-None-Match") + "" 
            params.ETag = etag 
            if ("$rETag" == "$etag") { 
                params.status = 304
                return // without auditing
            } 
            
        } 
        catch(Exception e2) {
            def xe2 = e2.toString() //.message.split(':')
            params.status = 503
            answer = ["header": params, "body":["possibleCause": "Unavailable Domain Server $params.sourceComponent", "message":[xe2]]]
            return answer 
        }       
        if (params.withlinks == "false"  ) {
            params.notes = "To show 'links' include in the headers or in request 'withlinks=true'."
            answer = ["header":params, "body":resp.json] 
        }
        else {
            params.notes = "To hide 'links' include in the headers or in request 'withlinks=false'."
            answer = ["header":params, "body":resp.json, "links": links ] 
        }
        
        return answer      
    }
    
    def URL(request) {
        def x = request.getRequestURL() 
        return x.substring(0,x.indexOf('.dispatch')) - '/grails'	        
    }   
    
    def hostApp(request = null) {
        def appName = SysConfigService.getComponent("CoreCommands").component.baseURL
        //def appName = entities.Component.findByName("CoreQueries").baseURL   
        return appName
    }      
    
    def makeEtag(String s) {
//        return s.encodeAsMD5()
        return s.encodeAsSHA1() // encodeAsSHA256()
        
//        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(s)
        
//        MessageDigest digest = MessageDigest.getInstance("MD5")
//        digest.update(s.bytes);
//        new BigInteger(1, digest.digest()).toString(16)//.padLeft(32, '0')    
        
//        MessageDigest digest = MessageDigest.getInstance("SHA-256")
//        byte[] hash = digest.digest(text.getBytes("UTF-8"));        
    }
    
    
}