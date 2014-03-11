#
# Test the sql template
# This requires the test mysql database to be configured properly
#
log=5
port=5555

handler=main
main.class=sunlabs.brazil.server.ChainHandler
main.handlers=map template

# Map all URL requests to our page

map.class=sunlabs.brazil.handler.UrlMapperHandler
map.match=/*
map.replace=/sql.html

# Set up the sql stuff

template.class=sunlabs.brazil.template.TemplateHandler
template.templates= set bsl sql

set.class=sunlabs.brazil.template.SetTemplate
set.query=Query.

bsl.class=sunlabs.brazil.template.BSLTemplate 

sql.class=sunlabs.brazil.sql.SqlTemplate
sql.driver=org.gjt.mm.mysql.Driver
sql.url=jdbc:mysql://mack.eng/test?user=brazil&password=none
