package entities

class RefreshCacheController {
/*
it is telling both client caches and proxy caches that once the content is stale 
(older than 'max-age' seconds) they must revalidate at the origin server before they 
can serve the content. This should be the default behavior of caching systems,
but the must-revalidate directive makes this requirement unambiguous.

b) The client should revalidate. It might revalidate using the If-Match or 
If-None-Match headers with an ETag, or it might use the If-Modified-Since or 
If-Unmodified-Since headers with a date.
see also: http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9.4
*/
    
    def index() { 
        render (text:"Cache refreshed", status:200, "Cache-Control": "max-age=0, must-revalidate" )
    }
}
