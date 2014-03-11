# Sample config file for groovy
# usage:
# - java -classpath brazil.jar:groovy-all-1.0-jsr-04.jar \
# 	sunlabs.brazil.server.Main -c config.groovy
# - point your browser at http://localhost:8083/groovy.html 

port=8083
log=5
handler=sunlabs.brazil.server.ChainHandler
handlers=session template

session.class=sunlabs.brazil.handler.CookieSessionHandler
template.class=sunlabs.brazil.template.TemplateHandler
template.templates=set bsl groovy
template.debug=1

set.class=sunlabs.brazil.template.SetTemplate
bsl.class=sunlabs.brazil.template.BSLTemplate
groovy.class=sunlabs.brazil.groovy.GroovyServerTemplate
groovy.script=init.groovy
