# Config file for testing cgi scripts
# Note:
#  If the cgi's don't seem to work, make sure they are executable, 
#  and can be run directly from the command line
# Suhler 10/98
#
root=.
handler=main
main.class=sunlabs.brazil.server.ChainHandler
#
# Tell the chain handler what to do
# Run cgi scripts before ~user to prevents users from running cgi's
#
main.handlers = cgi cgi-bin home file
#
# home directory redirector.  Looks for url's of the form:
# ~<user>/... and changes them to /home/user/...
#
home.class=sunlabs.brazil.handler.HomeDirHandler
#
# Standard cgi handler.  Looks for ".cgi" files anywhere in the doc root
#
cgi.class=sunlabs.brazil.handler.CgiHandler
cgi.suffix=.cgi
#
# Look for cgi scripts in /cgi-bin only, but with any suffix
#
cgi-bin.class=sunlabs.brazil.handler.CgiHandler
cgi-bin.prefix=/cgi-bin/
cgi-bin.suffix= 
#
# Standard file handler properties
# Add any additional mime types here
#
file.class=sunlabs.brazil.server.FileHandler
file.default=index.html
mime.foo=text/foo
