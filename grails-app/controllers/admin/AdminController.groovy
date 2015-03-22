package admin
import grails.converters.*
import groovy.json.*
import groovy.xml.MarkupBuilder
import grails.plugins.rest.client.RestBuilder
import grails.web.Action
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
//import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.util.Holders


class AdminController {
    def grailsApplication
    LinkGenerator grailsLinkGenerator
    def RenderService
    def BuildLinksService
    def ConfiguratorService
    
    def index() {redirect(action: "menu") }

    def menu() { 
    def html = """
        <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
        <html>
        <head>
          <meta content="text/html; charset=ISO-8859-1"
         http-equiv="content-type">
          <title>admin</title>
        </head>
        <body>
        <div style="text-align: left;"><big
         style="color: rgb(12, 59, 150);"><big><big><span
         style="font-family: Calibri; font-weight: bold;">CoreUpdates<br>
        </span><span style="font-family: Calibri;">Admin menu</span></big></big></big><br>
        </div>
        <br>
        <ol>
          <li><a href="XSD" target="_blank">Data
        Model in XML Schema[*]</a></li>
          <li><a href="JSD" target="_blank">Data
        Model in JSON Schema[*]</a></li>\n\
          <li><a href="modelDiagram" target="_blank">Model
        Diagram</a></li>
          <!--
          <li><a href="modelDiagram" target="_blank">Relations
        Diagram</a></li>\n\
          <ol>
            <li><a href="modelDiagram" target="_blank">CoreLayer</li>\n\
            <li><a href="relationsDiagram?ComponentName=Commons" target="_blank">Commons</li>
            <li><a href="relationsDiagram?ComponentName=Parties" target="_blank">Parties</li>
            <li><a href="relationsDiagram?ComponentName=Products" target="_blank">Products</li>
            <li><a href="relationsDiagram?ComponentName=Accounts" target="_blank">Accounts</li>\n\
          </ol>\n\
          -->
          <li><a href="componentsActionsByController" target="_blank">Actions by Controller</a></li>\n\
          <li><a href="repo" target="_blank">Resources (services) repository</a></li>\n\
          <li><a href="refreshComponentsConfig" target="_blank">Refresh Components Configuration</a></li>\n\
        </ol>
        [*] You can address specific component by appending:<br>
        ?ComponentName={component}<br>
        For example, after calling one of the above, append:<br>
        ?ComponentName=Commons
        </body>
        </html>
        """
        render (html)
    }
    
    def modelDiagram()  {
        // check if internet connection is available
        URL html = new URL('http://yuml.me');
        URLConnection urlConnection = html.openConnection();
        try {
            urlConnection.connect()
        }
        catch(e1) {
            println e1.message
        }
        // if there is internet connection ask yUML.me to draw the diagram, else use the local image
        if (urlConnection.connected) {        
                def url = "http://yuml.me/diagram/nofunky;dir:TD/class/draw2/"  
                url += "[Composer]<>0..*-0..*>[CoreQueries],[Composer]<>0..*-0..*>[CoreCommands],[CoreCommands]<>0..*-0..*>[CoreQueries],[CoreQueries]<>0..*-0..*>[Accounts], [CoreQueries]<>0..*-0..*>[Commons], [CoreQueries]<>0..*-0..*>[Parties], [CoreQueries]<>0..*-0..*>[Products],[CoreQueries]<>0..*-0..*>[API Repository],"
                url += "[CoreCommands]<>0..*-0..*>[Accounts], [CoreCommands]<>0..*-0..*>[Commons], [CoreCommands]<>0..*-0..*>[Parties], [CoreCommands]<>0..*-0..*>[Products],[CoreCommands]<>0..*-0..*>[API Repository],[Policies]<-[CoreQueries],[Policies]<-[CoreCommands],[Policies]<-[Composer],"       
                url += "[Policies]<>0..*-0..*>[IAM-SSO-RBAC],[Policies]<>0..*-0..*>[Scheduler],[Scheduler]<>0..*-0..*>[MailManager], [Scheduler]<>0..*-0..*>[ReportingEngine],[APE (Asynch Processing Engine)]<>0..*-0..*>[Logging],"
                url += "[Clients]<>0..*-0..*>[Composer],[Clients]<>0..*-0..*>[CoreQueries],[Clients]<>0..*-0..*>[CoreCommands],[Clients]<>0..*-0..*>[BizProcessor],[BizProcessor]<>0..*-0..*>[Composer],[BizProcessor]<>0..*-0..*>[CoreQueries],[BizProcessor]<>0..*-0..*>[CoreCommands],"
                url += "[Composer]<>0..*-0..*[APE (Asynch Processing Engine)],[APE (Asynch Processing Engine)]<>0..0..*[Business Intelligence]"
                redirect (url:"$url")
            return
            }
        else {
            // Show the local diagram
            def reldiagram = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
                <html>
                <head>
                  <meta content="text/html; charset=ISO-8859-1"
                 http-equiv="content-type">
                  <title></title>
                </head>
                <body>\n\
                <img src="${resource(dir: 'images', file: 'RelationsDiagram.png')}" 
                     alt="Relations Diagram of Commons entites "/><br>
                </body>
                </html>
            """ 
            render (reldiagram)
        }            
    }   
    def RelationsDiagram() {
        // check if internet connection is available
        URL html = new URL('http://yuml.me');
        URLConnection urlConnection = html.openConnection();
        try {
            urlConnection.connect()
        }
        catch(e1) {
            println e1.message
        }
        // if there is internet connection ask yUML.me to draw the diagram, else use the local image
        if (urlConnection.connected) {
            def domainClasses = grailsApplication.getArtefacts("Domain")
            def classes = ''
            def classrelation = ''
            def relationships = ''
            def proType = ''
            domainClasses.each { domainClass ->
                def relations = "" // [$domainClass.name], "  // Include those are not part of relations
                def classDef = ""
                domainClass.properties.each{prop ->
                    proType = resolveName(prop.type.toString())
                        if (prop.isAssociation()){
                            // if its association only show the owning side
                            if(!prop.isBidirectional() || prop.isOwningSide() || proType == domainClass.name )
                                classrelation = getRelationship(domainClass.name, prop)
                                if (!relationships.contains(classrelation) && !relations.contains(classrelation)) {
                                    relations = "$relations \n$classrelation" 
                                }
                        } else {
                            // Domain class properties 
                            classDef += '\n' + resolveName(prop.getType().getName()) + ' ' + prop.name + ';' 
                        }
                }
                if (!relationships.contains(relations.trim())){
                    relationships += relations
                }
            } 
            // Include those are not part of relations (orphans)
            domainClasses.each { domainClass ->
                if (!relationships.contains(domainClass.name.trim())) {
                    relationships += "[$domainClass.name],"  
                }
            }
            redirect(url: "http://yuml.me/diagram/nofunky;dir:TD/class/draw2/" + classes + relationships)            
        }
        else {
            // Show the local diagram
            def reldiagram = """
                <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
                <html>
                <head>
                  <meta content="text/html; charset=ISO-8859-1"
                 http-equiv="content-type">
                  <title></title>
                </head>
                <body>\n\
                <img src="${resource(dir: 'images', file: 'RelationsDiagram.png')}" 
                     alt="Relations Diagram of Commons entites "/><br>
                </body>
                </html>
            """ 
            render (reldiagram)
        }        
    }

    def componentsActionsByController(String ComponentName, String ControllerName) {
        // example: /server/ComponentsActionsByController?ComponentName=Commons&ControllerName=Currency
        if (ComponentName == "Core" || ComponentName==null) {
            def x = myActions()       
        }
        else { 
            def url 
            def component = entities.Component.findByName(ComponentName) 
            if (component.supportAdmin){
                def baseURL = component.baseURL
                if (ControllerName == null) {
                    url = "$baseURL" + "/admin/actionsByController"
                } 
                else {
                    url = "$baseURL" + "/admin/actionsByController?ControllerName=$ControllerName"
                }
                def rest = new RestBuilder()
                def resp = rest.get("$url") {
                    accept "application/json"
                    contentType "application/json"                       
                }
                render resp.json
            }
            else {
                response.status = 404
                render "<p>'$ComponentName' does not suport Remote Admin.</p><p> Ask the Relations Diagram from the component, directly.</p>"
            }
        }
        
    }

    private myActions()  {
        def componentName = grailsApplication.metadata['app.name']
        def jactions = new JsonBuilder()
        jactions."$componentName"{
            def x = request.getRequestURL()  
            links {
                hostApp x.substring(0,x.indexOf('.dispatch')) - '/grails' - '/admin/' - "$params.action" 
                href x.substring(0,x.indexOf('.dispatch')) - '/grails'  
            } 
            grailsApplication.controllerClasses.each {cc ->   
            String controller = cc.logicalPropertyName
            "$controller" {
                cc.clazz.methods.each { m->
                    String action = m.name 
                    if (action in ["extraLinks"]) {
                            // do nothing - skip 
                        }
                    else {
                        def ann = m.getAnnotation(Action)
                        if (ann) { 
                            Class[] argTypes = ann.commandObjects()
    //                      // exclude index
                            if ("$action"!="index") { 
    //                           "$action" "/${controller}/$action(${argTypes*.name.join(', ')})"
    //                            "$action" "$action(${argTypes*.name.join(', ')})"  - '()'
                                "$action" "/${controller}/$action(${argTypes*.name.join(', ')})" - 'entities.' - '()'
                            }
                        }                        
                    }
                }
             }
          }
       }   
       render jactions.toPrettyString()
    }
   
    def DataModelInJSON() {
        redirect (action:"JSD", params:params)
        return
    }
    
    def JSD (String ComponentName, String ClassName)  {
        def componentName = grailsApplication.metadata['app.name']
        if (ComponentName==null || ComponentName==componentName) {
            def className = params.class
            def appName = grailsApplication.metadata['app.name']
            def jschema = new JsonBuilder()
            def domainSchema = grailsApplication.getArtefacts("Domain")
            jschema."$appName"{
            domainSchema.each { c -> 
                if (className==null || "$c.name"==className){
                    "$c.name" { 
                         c.properties.each { p ->
                            "$p.name" {          
                                    type  p.getReferencedPropertyType().getName().substring(p.getReferencedPropertyType().getName().lastIndexOf('.')+1)
                                    if(!p.isBidirectional() || p.isOwningSide()) {
                                       if (p.isOneToMany()){           //  '1-0..*>':'1-1..*>'
                                           minOccurs "0" 
                                           maxOccurs "unbounded"
                                       } else if (p.isOneToOne()){     //  '1-0..1>':'1-1>'
                                           minOccurs "1" 
                                           maxOccurs "1"
                                       } else if (p.isManyToMany()){   //  '*-*>':'1..*-1..*>'
                                           minOccurs "0" 
                                           maxOccurs "unbounded"                                       
                                       }
                                    }  
                                }
                            }
                        }
                    }
                }
            }
             render jschema 
        }
        else {
            def url 
            def component = entities.Component.findByName(ComponentName) 
            def baseURL = component.baseURL

            if (ClassName == null) {
                url = "$baseURL" + "/admin/JSD"
            } 
            else {
                url = "$baseURL" + "/admin/JSD?ClassName=$ClassName"
            }

            def rest = new RestBuilder()
            def resp = rest.get("$url") {
                accept "application/json"
                contentType "application/json"                        
            } 
            render resp.body.toString()            
        }
    }
    
    def dmxml = {
        redirect(action: "XSD", params: params)
        return
    }
    
    def DataModelInXML = {
        redirect(action: "XSD", params: params)
        return
    }

    def XSD (String ComponentName, String ClassName) {       
        def xsd
        def componentName = grailsApplication.metadata['app.name']
        if (ComponentName==null || ComponentName=="$componentName") {
            def className = ClassName?.capitalize()
            def componentSchema = grailsApplication.getArtefacts("Domain")
            def writer = new StringWriter()
            def schema = new MarkupBuilder(writer)  
            def str = ""
            def ptype = []
            schema."xs:component" (name: "$componentName") {
                componentSchema.each { c ->
                    if (className==null || "$c.name"==className){
                        schema."xs:complexType" (name: "${c.name}") {
                          schema."xs:sequence" { 
                            c.properties.each { p ->
                               switch (p.name) {
                                   case ['validationSkipMap', 'gormPersistentEntity', 'gormPersistentEntity', 'properties', 'gormDynamicFinders', 'all', 'domainClass', 'attached']:
                                       break
                                   case ['class', 'constraints', 'validationErrorsMap', 'dirtyPropertyNames', 'metaClass', 'errors', 'dirty', 'beforeInsert', 'beforeUpdate'] :
                                       break
                                   default:
                                       str = resolveName(p.getReferencedPropertyType().getName())
                                       if (p.isAssociation()){
                                           // if its association only show the do the owning side
                                           if(!p.isBidirectional() || p.isOwningSide()) {
                                               if (p.isOneToMany()){           //  '1-0..*>':'1-1..*>'
                                                   "xs:element"(name:"${p.name}", type:"${str}", minOccurs:"0", maxOccurs:"unbounded") 
                                               } else if (p.isOneToOne()){     //  '1-0..1>':'1-1>'
                                                   "xs:element"(name:"${p.name}", type:"${str}", minOccurs:"1", maxOccurs:"1")
                                               } else if (p.isManyToMany()){   //  '*-*>':'1..*-1..*>'
                                                   "xs:element"(name:"${p.name}", type:"${str}", minOccurs:"0", maxOccurs:"unbounded")
                                               }
                                               break
                                               }
                                               else { // not bidirectional ...
                                               }
                                       } else {
                                           }    

                                       switch ( str ) {
                                           case "String":
                                               "xs:element"(name: "${p.name}", type:"xs:string")
                                               break
                                           case "Long" :
                                               "xs:element"(name:"${p.name}", type:"xs:long")
                                               break
                                           case "Integer" :
                                               "xs:element"(name:"${p.name}", type:"xs:integer")
                                               break                    
                                           case "Date" :
                                               "xs:element"(name:"${p.name}", type:"xs:date")
                                               break     
                                           case "boolean" :
                                               "xs:element"(name:"${p.name}", type:"xs:boolean")
                                               break
                                           case "Set" :
                                               "xs:element"(name:"${p.name}", type:"xs:boolean")
                                               break
                                           default:
                                               "xs:element"(name:"${p.name}", type:"${str}")   
                                       }
                               }
                        }
                       }
                     }
                    }
                }
            }
            xsd = '<?xml version="1.0" encoding="ISO-8859-1" ?>\n<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">\n' + writer.toString() + '\n</xs:schema>'   
            render xsd
        }
        else {
            def url 
            def component = entities.Component.findByName(ComponentName) 
            def baseURL = component.baseURL

            if (ClassName == null) {
                url = "$baseURL" + "/admin/XSD"
            } 
            else {
                url = "$baseURL" + "/admin/XSD?ClassName=$ClassName"
            }

            def rest = new RestBuilder()
            def resp = rest.get("$url") {
                accept "application/json"
                contentType "application/json"                        
            } 
            render resp.body.toString()
        }
    }
        
        
    private resolveName(def name){
        // remove bracket if an array
        if(name.lastIndexOf('[') > -1){
            name = name.replace('[','');
        }
        // remove package name
        if (name.lastIndexOf('.') > -1){
            return name.substring(name.lastIndexOf('.')+1)
        }
        return name
    }        

    def repo() {
        def componentName = grailsApplication.metadata['app.name']
        def jactions = new JsonBuilder()
        def component = [:]
        def controllerLinks = [:]
        params.host = RenderService.hostApp(request)
        def x = request.getRequestURL() 
        x = x.substring(0,x.indexOf('.dispatch')) - '/grails' - "/admin/componentsActionsByController" - "index"
        component =["$componentName" : ["links":["repo" : ["home":"$x" - "/home"],"repo":["href":"$x" ]]]]
        grailsApplication.controllerClasses.each {cc -> 
            String controller = cc.logicalPropertyName
            params.controller = "$controller"
            if (['admin', 'repo', 'component', 'dbdoc', 'refreshCache'].contains(controller)) { 
                controllerLinks += ["$controller" : BuildLinksService.controllerSingleLink(params, request)] 
            }
            else {
                controllerLinks += ["$controller" : BuildLinksService.controllerLinks(params, request) ]
            }
        }
        def result = ["component": component, "controllers": controllerLinks] as JSON
        render result
    }
 
    def refreshComponentsConfig() {
        ConfiguratorService.saveComponents()
        def result = ["status":"200", "description":"Hosts Configuration refreshed" ] as JSON
        render result
    }        
}