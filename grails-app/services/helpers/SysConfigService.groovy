package helpers
import grails.plugins.rest.client.RestBuilder
import grails.converters.*
//import org.codehaus.groovy.grails.commons.GrailsApplication
class SysConfigService {

//    def grailsApplication
    
    def getComponent(String component) { // returns the properties, in JSON, of the component
//        println "getComponent for $component"
        def restConfig = new RestBuilder()
        def url = "http://auditdb:5984/configdb/$component" 
        def respV = restConfig.get("$url") { 
            accept "application/json"
            contentType "application/json"
        }   
        return respV.json
    }
    
    def getComponentHost(String component) {  // Returns the component URL
        def restConfig = new RestBuilder() 
        def url = "http://auditdb:5984/configdb/$component" 
        def respV = restConfig.get("$url") { 
            accept "application/json"
            contentType "application/json"
        }   
        return respV.json.appServer
    }    
    
    def isComponentActive(String component) {
        def restConfig = new RestBuilder()
        def url = "http://auditdb:5984/configdb/$component" 
        def respV = restConfig.get("$url") { 
            accept "application/json"
            contentType "application/json"
        }   
        return respV.json.isActive
    }      
}
