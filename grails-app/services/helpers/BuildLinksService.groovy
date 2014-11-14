package helpers

class BuildLinksService {
    static transactional = false

    def controllerLinks(params, request) {
        def links = [:] 
        def controllerURL = "$params.host/$params.controller"
        if (params.withlinks==null) { params.withlinks = request.getHeader('withlinks')}
        if (params.withlinks=="true" || params.withlinks==null ) { 
            links += ["create": ["method":"PUT", "href": "$controllerURL/create", "notes" : "It creates a new resource with the payload (body in jason). \n\
                You can get the template of the body with the /CoreQueries/{entity}/create or with /CoreQueries/{entity}/schema"]]

            links  += ["delete":["template": true, "fields": ["id":"Long (the unique identifier)"], \
                "method":"DELETE", "href":"$controllerURL/delete?id={id}", "notes":"It deletes (logically) the resource (recStatus='Deleted')" ]]
            links  += ["undelete":["template": true, "fields": ["id":"Long (the unique identifier)"], \
                "method":"DELETE", "href":"$controllerURL/undelete?id={id}", "notes":"It undeletes (logically) the resource (recStatus='Active')" ]]            
            links += ["update": ["method":"POST", "href": "$controllerURL/update", "notes" : "It replaces the resource with the payload (body in jason)"]]
            
            links += ["relatedLinks": ["href": "$controllerURL/relatedLinks", "notes":"Returns a list with the most frequent links."]]
            links += ["repo": ["href": "$params.host/repo", "notes": " The repository of hypermedia links of the $params.host"]]
            links += ["refreshCache": ["href": "$params.host/refreshCache", "notes":"Clears all caches (client and proxies). Use it after a system/client disruption."]]
            return links 
        }        
    } 
    
    def controllerSingleLink(params, request) {
        return ["$params.controller": ["href": "$params.host/$params.controller"]]              
    }

}
