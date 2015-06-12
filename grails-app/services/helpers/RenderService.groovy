package helpers
import grails.plugins.rest.client.RestBuilder
import grails.converters.*
import java.security.MessageDigest 

class RenderService {
    
    def sysConfigService
    
    static transactional = false
    
    def prepareAnswer(params, request) {
//        def baseURL = "http://agilebanking.net:6789/$params.sourceComponent"  ***
        def baseURL = "http://localhost:6789/$params.sourceComponent"
        params.reqID = UUID.randomUUID().toString()
        params.Date = new Date().toString()
        params.method = request.method.toUpperCase()
        params.format="json"
        
        // prepare hyperlinks    
        def href = "$params.URL"
        def links = ["self": ["href": "$params.URL"]]
        if (href.contains("?")) {
            links.self += ["alt_href": "$href" + "&withlinks=false"]
        }
        else {
            links.self += ["alt_href": "$href" + "?withlinks=false"]
        }
        if (params.links != null) { links += params.links    } 
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
            // see the documentation https://github.com/grails-plugins/grails-rest-client-builder/
//            println "request.body = ${request.JSON as JSON}"
            switch(params.method) {
            case "PUT":
//                println "Ready to PUT the: $baseURL$params.sourceURI"
//                println "with body: ${request.JSON as JSON}"
                resp = rest.put("$baseURL$params.sourceURI") { 
                    accept "application/json"
                    contentType "application/json"
                    json request.JSON as JSON
                    }
//                println "result: $rest.JSON"
                break;
            case "POST":
//                println "Ready to POST the: $baseURL$params.sourceURI"
                resp = rest.post("$baseURL$params.sourceURI") { 
                    accept "application/json"
                    contentType "application/json"
                    json request.JSON as JSON
                    }      
                break;
            case "DELETE":
                resp = rest.delete("$baseURL$params.sourceURI") { 
//                println "Ready to DELETE the: $baseURL$params.sourceURI"
                    accept "application/json"
                    contentType "application/json"
                    }  
                break;
            default:
                resp.status = 400 // bad request
            } 
//            println "resp.status = ${resp.status}"
//            println "resp.json = ${resp.json}" 
            params.status = resp.status 
//            def etag = ""

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
            
//            else {
//                etag = makeEtag(resp.json.toString())
//            }
//            println "2 resp.json = ${resp.json}" 
            // If not modified return nothing  
//            def rETag = request.getHeader("If-None-Match") + "" 
//            params.ETag = etag 
//            if ("$rETag" == "$etag") { 
//                params.status = 304
//                return // without auditing
//            } 
        } 
        catch(Exception e2) {
            def xe2 = e2.toString() //.message.split(':')
            params.status = 503
            answer = ["header": params, "body":["possibleCause": "Unavailable Domain Server $params.sourceComponent", "message":[xe2]]]
            return answer 
        }    
        
        if (params.withlinks == "false" ) {
            params.notes = "To show 'links' include in the headers or in request 'withlinks=true'."
            answer = ["header":params, "body":resp.json] 
        }
        else {
            params.notes = "To hide 'links' include in the headers or in request 'withlinks=false' or null."
            answer = ["header":params, "body":resp.json, "links": links  ] 
        }
//        println "answer = ${answer}" 
        return answer 
    }
    
    def URL(request) {
        def x = request.getRequestURL() 
        return x.substring(0,x.indexOf('.dispatch')) - '/grails'	        
    }   
    
    def hostApp() {
        return sysConfigService.getComponentHost("CoreCommands")
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