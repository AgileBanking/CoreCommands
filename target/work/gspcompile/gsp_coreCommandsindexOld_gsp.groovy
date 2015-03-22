import org.codehaus.groovy.grails.plugins.metadata.GrailsPlugin
import org.codehaus.groovy.grails.web.pages.GroovyPage
import org.codehaus.groovy.grails.web.taglib.*
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.springframework.web.util.*
import grails.util.GrailsUtil

class gsp_coreCommandsindexOld_gsp extends GroovyPage {
public String getGroovyPageFileName() { "/WEB-INF/grails-app/views/indexOld.gsp" }
public Object run() {
Writer out = getOut()
Writer expressionOut = getExpressionOut()
registerSitemeshPreprocessMode()
printHtmlPart(0)
createTagBody(1, {->
printHtmlPart(1)
invokeTag('captureMeta','sitemesh',4,['gsp_sm_xmlClosingForEmptyTag':("/"),'name':("layout"),'content':("main")],-1)
printHtmlPart(1)
createTagBody(2, {->
createClosureForHtmlPart(2, 3)
invokeTag('captureTitle','sitemesh',5,[:],3)
})
invokeTag('wrapTitleTag','sitemesh',5,[:],2)
printHtmlPart(3)
})
invokeTag('captureHead','sitemesh',82,[:],1)
printHtmlPart(4)
createTagBody(1, {->
printHtmlPart(5)
invokeTag('message','g',84,['code':("default.link.skip.label"),'default':("Skip to content&hellip;")],-1)
printHtmlPart(6)
expressionOut.print(System.getProperty("user.name"))
printHtmlPart(7)
expressionOut.print(TimeZone.getDefault().getDisplayName())
printHtmlPart(8)
expressionOut.print(new Date().getDateTimeString())
printHtmlPart(9)
invokeTag('meta','g',94,['name':("app.version")],-1)
printHtmlPart(10)
invokeTag('meta','g',95,['name':("app.grails.version")],-1)
printHtmlPart(11)
expressionOut.print(System.getProperty('java.version'))
printHtmlPart(12)
expressionOut.print(grails.util.Environment.reloadingAgentEnabled)
printHtmlPart(13)
expressionOut.print(grailsApplication.controllerClasses.size())
printHtmlPart(14)
expressionOut.print(grailsApplication.domainClasses.size())
printHtmlPart(15)
expressionOut.print(grailsApplication.serviceClasses.size())
printHtmlPart(16)
expressionOut.print(grailsApplication.tagLibClasses.size())
printHtmlPart(17)
expressionOut.print(InetAddress.localHost.hostName)
printHtmlPart(18)
expressionOut.print(InetAddress.localHost.hostAddress)
printHtmlPart(19)
expressionOut.print(InetAddress.localHost.canonicalHostName)
printHtmlPart(20)
expressionOut.print("nslookup accounts.app control.lan".execute().text.trim().split(/(\n)/).last().split(/(:)/)[1].trim())
printHtmlPart(21)
expressionOut.print("nslookup accounts.db control.lan".execute().text.trim().split(/(\n)/).last().split(/(:)/)[1].trim())
printHtmlPart(22)
expressionOut.print("nslookup control.lan control.lan".execute().text.trim().split(/(\n)/).last().split(/(:)/)[1].trim())
printHtmlPart(23)
for( plugin in (applicationContext.getBean('pluginManager').allPlugins) ) {
printHtmlPart(24)
expressionOut.print(plugin.name)
printHtmlPart(25)
expressionOut.print(plugin.version)
printHtmlPart(26)
}
printHtmlPart(27)
for( c in (grailsApplication.controllerClasses.sort { it.fullName }) ) {
printHtmlPart(28)
createTagBody(3, {->
expressionOut.print(c.fullName - 'admin.' - 'Controller' - 'server.' - 'services.' - 'entities.' - 'grails.plugin.databasemigration.')
})
invokeTag('link','g',133,['controller':(c.logicalPropertyName)],3)
printHtmlPart(29)
}
printHtmlPart(30)
})
invokeTag('captureBody','sitemesh',138,[:],1)
printHtmlPart(31)
}
public static final Map JSP_TAGS = new HashMap()
protected void init() {
	this.jspTags = JSP_TAGS
}
public static final String CONTENT_TYPE = 'text/html;charset=UTF-8'
public static final long LAST_MODIFIED = 1422045752674L
public static final String EXPRESSION_CODEC = 'none'
public static final String STATIC_CODEC = 'none'
public static final String OUT_CODEC = 'none'
public static final String TAGLIB_CODEC = 'none'
}
