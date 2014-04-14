#!/usr/bin/tclsh
# Copyright (c) 2004-2007 Sun Microsystems, Inc.
# The contents of this file are subject to the Sun Public License Version
# 1.0 (the "License"). You may not use this file except in compliance with
# the License. A copy of the License is included as the file "license.terms",
# and also available at http://www.sun.com/
#
# This file is both "loadastb" and "dumpastb"
# dump... dump the asterisk database to stdout
# load... load the asterisk database from stdin

# configure the server here

set HOST localhost
set PORT 5038
set USER AMI-USER
set PASS AMI-PASS
set debug 0

# end of user configuration

# log what we got from the server

proc log {msg} {
    global debug
    if {$debug} {
       puts stderr "   got ($msg)"
    }
}

# connect to the manager interface

proc connect {server port user pass} {
    set sock [socket $server $port]
    set line [gets $sock]
    log $line
    if {![regexp "Asterisk Call Manager" $line]} {
        puts stderr "Invalid server response: $line"
        exit 1
    }
    puts $sock "action: login"
    puts $sock "username: $user"
    puts $sock "secret: $pass"
    puts $sock ""
    flush $sock

    set line xx
    set status failed
    while {![eof $sock] && \
          ![regexp {Message.*(accepted|failed)} $line all status]} {
       set line [gets $sock]
       log $line
    }
    if {$status == "failed"} {
       puts stderr "Authentication failed, sorry"
       exit 2
    }
    return $sock
}

# issue a manager command, return the list
# values must be properly quoted

proc command {sock cmd} {
    set start 1
    set result ""
    puts $sock "action: command"
    puts $sock "command: $cmd"
    puts $sock ""
    flush $sock
    set line xx
    while {1} {
        set line [gets $sock]
        if {[eof $sock] || [regexp {^--END} $line]} {
            break
        }
	if {$start} {
            if {[regexp ^Privilege: $line]} {set start 0}
            continue
        }
	if {[regexp success $line]} {break}
	log $line
	lappend result $line
    }
    return $result
}

# Main program

switch -glob [file tail $argv0] {
    dump* {
        set sock [connect $HOST $PORT $USER $PASS]
        foreach item [command $sock "database show"] {
            regsub " *: *" $item = item
            puts [string trim $item]
        }
        close $sock
    }
    load* {
       set sock [connect $HOST $PORT $USER $PASS]
       while {![eof stdin]} {
           set line [gets stdin]
           if {![regexp {^/([^=]+)=(.*)} $line all key value]} continue
           set name $key; set family ""
           regexp {(.*)/(.*)} $key all family name
           set result [command $sock \
		"database put \"$family\" \"$name\" \"$value\""]
       }
       close $sock
    }
    * {
        puts stderr "This program needs to be named dump.. or load..."
        exit 1
    }
}
exit 0
