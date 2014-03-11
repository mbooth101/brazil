# this config file may be used to run php scripts designed to run
# with apache's mod_php by using the relevent GciHandler options
# Make sure the "runwith" line matches your php installation

port=2345
log=5

handler=main
main.class=sunlabs.brazil.server.ChainHandler

# Either handler order will work.  If "file" is first, then make sure
# "php" is not a valid mime type, so the file handler will ignore all
# php files

main.handlers=default file cgi
# main.handlers=default cgi file

# if "index.php" exists, use it in preference to "index.html" as the
# default file in this directory

default.class=sunlabs.brazil.handler.DefaultFileHandler
default.defaults=index.php index.html

# pass each php script to php

cgi.class=sunlabs.brazil.handler.CgiHandler
cgi.suffix=.php
cgi.url=current
cgi.runwith=/usr/local/bin/php5 -f
cgi.noheaders=true
cgi.mime.php=text/html

# anything else is a regular file, image, etc
file.class=sunlabs.brazil.server.FileHandler
