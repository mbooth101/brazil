# parts of a web site "live" in the jar file, other parts are on 
# the file system

port=7007
log=5
root=/home/suhler/brazil/naws/samples/configs

handler=sunlabs.brazil.server.ChainHandler

# the order is important: "built-in" file come first here
handlers= resource file

resource.class=sunlabs.brazil.handler.ResourceHandler

# This is the root of the site "inside" the jar file
# Since all "built in files" in the standard brazil jar start
# at /html, the url "/html/" will get to the built in index file

resource.root=/

# Use this to start the "built-in" files at "/", but allow files
# in the external filesystem to override the built-in ones
# resource.root=/html
# handlers=file resource

file.class=sunlabs.brazil.server.FileHandler
