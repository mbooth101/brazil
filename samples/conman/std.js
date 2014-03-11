/*
 *  Copyright (c) 2006-2007 Sun Microsystems, Inc.
 *  The contents of this file are subject to the Sun Public License Version
 *  1.0 (the "License"). You may not use this file except in compliance with
 *  the License. A copy of the License is included as the file "license.terms",
 *  and also available at http://www.sun.com/
 *
 * Common javascript for all pages - no fancy stuff.
 */

// this is really dumb!

var timer=null; // we need one per help div XXX
function do_help(text) {
   if (timer && text.length > 0) {
       clearTimeout(timer);
       timer=null;
   }
   if (set_help_text("help", text) && text.length > 0) {
     timer = setTimeout("do_help('')", 5000);
   }
}

function set_help_text(where, text) {
  var dst = document.getElementById(where);
  if (dst) {
    var src = document.getElementById(text);
    if (src) {
       text = src.innerHTML;
    }
    // experiment
    var ref=document.getElementById("reference");
    var width = 0;
    if (ref) {
	width = ref.offsetWidth;
    }
    dst.innerHTML = text;
    if (width > 0) {
        dst.style.width=(width-10) + "px";
    }
    return true
  }
  return false;
}
