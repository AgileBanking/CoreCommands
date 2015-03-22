package helpers
import grails.plugins.rest.client.RestBuilder
import grails.converters.*
//import org.codehaus.groovy.grails.commons.GrailsApplication
class SysConfigService {

    def grailsApplication
    
    def getComponent(String component) { 
        def restConfig = new RestBuilder()
        def url1 = grailsApplication.config.locations
        println "url?=$url1" 
        def url = "http://auditdb:5984/configdb/$component" 
        print "SysConfigService.getComponent url: " + url
        def respV = restConfig.get("$url") { 
            accept "application/json"
            contentType "application/json"
        }   
        return respV.json
    }
    
    def getComponentHost(String component) {
        def restConfig = new RestBuilder() 
        def url = "http://auditdb:5984/configdb/$component" 
        def respV = restConfig.get("$url") { 
            accept "application/json"
            contentType "application/json"
        }   
        return respV.json.name
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
