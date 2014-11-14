package helpers
import grails.plugins.rest.client.RestBuilder
import grails.converters.*

class ConfiguratorService {
    def Component
    def saveComponents(String component) {
        // Update configdb
        try {
            def conf = entities.Component.findByName("Config")
            if (conf.isActive) { 
                def allComponents
                if (component==null) {
                    allComponents = entities.Component.list()
                }
                else { 
                    allComponents = entities.Component.findByName(component)
                }

                def restConfig = new RestBuilder()
                def url = conf.baseURL 
                def tst = new Date()
                def respConfig
                allComponents.each {
                    def j = it as JSON
                    println "$url/$it.name"
                    def respV = restConfig.get("$url/$it.name") { 
                        accept "application/json"
                        contentType "application/json"
                    }                      
                    if (respV.json."_rev"==null) {
                        //insert for the first time
                        respConfig = restConfig.put("$url/$it.name"){ 
                            contentType "application/json"
                            json  '{"timestamp":"' + "$tst" + '", "component":' + "$j" + '}'  
                        }
                    }
                    else {
                        //update existing entry
                      respConfig = restConfig.put("$url/$it.name"){ 
                        contentType "application/json"
                        json  '{"_rev":"' + respV.json."_rev" +'", "timestamp":"' + "$tst" + '", "component":' + "$j" + '}'
                      }                    
                   }                    
                }

            }            
        }
        catch(Exception e3) { 
            println e3.toString()
//            flash.message = message(code: 'default.created.message', args: [message(code: 'component.label', default: '$e3.message', db:'configdb'])
        }        

    }
}
