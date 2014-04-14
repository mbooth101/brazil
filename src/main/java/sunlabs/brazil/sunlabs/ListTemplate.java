/*
 * ListTemplate.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 2002-2006 Sun Microsystems, Inc.
 *
 * Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License Version 
 * 1.0 (the "License"). You may not use this file except in compliance with 
 * the License. A copy of the License is included as the file "license.terms",
 * and also available at http://www.sun.com/
 * 
 * The Original Code is from:
 *    Brazil project web application toolkit release 2.3.
 * The Initial Developer of the Original Code is: suhler.
 * Portions created by suhler are Copyright (C) Sun Microsystems, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): suhler.
 *
 * Version:  1.15
 * Created by suhler on 02/11/25
 * Last modified by suhler on 06/11/13 11:42:48
 *
 * Version Histories:
 *
 * 1.15 06/11/13-11:42:48 (suhler)
 *   look for "track" attribute to display all list changes to the console
 *
 * 1.14 06/06/15-12:50:12 (suhler)
 *   Look for the "namespace" request property that is set by the SetTemplate
 *
 * 1.13 04/12/30-12:39:09 (suhler)
 *   javadoc fixes.
 *
 * 1.12 04/11/30-15:19:43 (suhler)
 *   fixed sccs version string
 *
 * 1.11 04/06/28-11:11:53 (suhler)
 *   When setting a "max" length, "front=true" will trim the items
 *   from the front of the list instead of the back
 *
 * 1.10 04/01/27-17:21:36 (suhler)
 *   - bug fixes
 *
 * 1.9 03/09/09-13:55:32 (suhler)
 *   - better bounds checking
 *   - max=n was broken
 *
 * 1.8 03/05/13-14:01:12 (suhler)
 *   cosmetic changes
 *
 * 1.7 03/01/06-12:24:24 (suhler)
 *   New implementation that makes ismember checks fast
 *
 * 1.6 02/12/19-11:40:09 (suhler)
 *   checkpoint
 *
 * 1.5 02/12/02-15:19:59 (suhler)
 *   added the PageTemplate capability to the ListTemplate
 *   PageTemplate is now obsolete
 *
 * 1.4 02/11/27-10:56:27 (suhler)
 *   change package
 *
 * 1.3 02/11/25-15:54:31 (suhler)
 *   add version info to saveable
 *
 * 1.2 02/11/25-15:36:47 (suhler)
 *   implement Saveable interfave
 *
 * 1.2 02/11/25-12:34:52 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 2 1 sunlabs/ListTemplate.java
 *   Name history : 1 0 slim/ListTemplate.java
 *
 * 1.1 02/11/25-12:34:51 (suhler)
 *   date and time created 02/11/25 12:34:51 by suhler
 *
 */

package sunlabs.brazil.sunlabs;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;
import sunlabs.brazil.session.SessionManager;
import sunlabs.brazil.template.RewriteContext;
import sunlabs.brazil.template.Template;
import sunlabs.brazil.util.Sort;
import sunlabs.brazil.util.Format;	// debugging
import sunlabs.brazil.util.regexp.Regexp;
import sunlabs.brazil.session.PropertiesCacheManager;

/**
 * Manage lists of strings in a (hopefully) useful way.
 * A vector is used to store list elements.  Attributes to
 * the &lt;list&gt; tag are used to manipulate the list, whereas
 * "smart properties" are used to retrieve list members.
 * <pre>
 * &lt;list name=x [namespace=xx clear=true|false delete=xx remove=n insert=xx
 *                  append=xx unique=true|false sort=true|false delim=d
 *                  overlap=n chunksize=n chunk=n next=true|false previous=true|false]"&gt;
 * &lt;list name=x ... /&gt;
 * &lt;/list&gt;
 * </pre>
 * Access to list elements is only valid between &lt;list&gt;... &lt;/list&gt;.
 * See below for details.
 */

public class ListTemplate extends Template {

    Vector listStack = new Vector();		// stack of lists

    /**
     * Clear any left-over lists.
     */

    public boolean
    init(RewriteContext hr) {
	listStack.removeAllElements();
	return super.init(hr);
    }


    /**
     * Process the list tag.
     * <pre>
     *   name=nnn		list name (required)
     *   namespace=xxxx		namespace for list.  See below
     * The following options are processed in order:
     *   clear=true|false	clear the list if it exists
     *   delete=xxx  [delim=d]  delete item(s) by value  (items with delimiter d)
     *   remove  [location=n]	remove an item by index (defaults to 0)
     *   insert=xxx [delim=d] [location=n]
     *				insert item (or items with delimiter=d)
     *				insert in front of element "n" - defaults to 0
     *   append=xxx [delim=d]	append item(s) to the end of the list
     *   unique=true|false	eliminate all duplicate items
     *   max=n			set the maximum size of the list "n"
     *   sort=true		do a dictionary sort.
     *   delim=d		set the delimiter for retrieving values
     *   track=true|false	track all changes to the console
     *
     * Once a "list" tag is seen, the following properties are
     *  available (for list foo):
     *   foo.last    	- last item on the list
     *   for.first   	- first item on the list
     *   foo.count   	- number of items on the list
     *   foo.all     	- the entire list
     *   list.gone   	- the last single element removed from the list
     *   foo.n	     	- the nth element (0 is the first element)
     *   foo.n:m     	- the range from n-m, inclusive, starting from 0
     *   foo.ismember.xxx - set to "yes" if "xxx" is in the list
     *   foo.before.xxx	- the range from 0 till the first occurrance of xxx
     *   foo.after.xxx	- the range after the 1st occurance of xxx til the end
     * </pre>
     * The first 4 items, above, always appear (for non empty lists) when
     * the properties are enumerated (as with &lt;foreach&gt;
     * <p>
     * In the current implementation, "ismember" checks are very fast.  However
     * once an "ismember" is accessed, insersion and deletion slows down a bit.
     * Using "clear" will speed up insertion and deletion again.
     * <p>
     * If no <code>namespace</code> parameter is provided,      
     * the request property [prefix].namespace is used, where [prefix]
     * is Rewrite.templatePrefix.  Otherwise the SessionID is used.
     * This results in per-session lists by default.  Specifying a namespace
     * allows lists to be shared across sessions.
     * <p>
     * An additional set of attributes and properties may be used to manage "chunking",
     * to access a list in pieces for paging.
     * Additional &lt;list&gt; attributes to support chunking:
     * <pre>
     *   overlap=nn		- how many items to overlap between each chunk (default=0)
     *   chunksize=n		- max items per chunk (default=20)
     *   chunk=n		- Set the current chunk number (starts at 0)
     *   next=true|false	- it true, advance to the next page (if available)
     *   previous=true|false	- it true, advance to the previous page (if available)
     * </pre>
     * None of the above properties change the contents of the list, only how chunks
     * are extracted using the properties below:
     * <pre>
     *   foo.chunk.[n]		- the list of items in chunk "n"
     *   foo.chunk.chunk	- the current list "chunk" (same as foo.chunk,${foo.chunk.current})
     *
     *   foo.chunk.count	- the number of chunks
     *   foo.chunk.chunks	- the list of chunks: "0 1 2 ... n"
     *   foo.chunk.first	- the first chunk number (always 0)
     *   foo.chunk.before	- the list of chunk numbers before current chunk (if any)
     *   foo.chunk.previous	- The previous chunk number (if any)
     *   foo.chunk.current	- The current chunk number
     *   foo.chunk.next		- The next chunk number (if any)
     *   foo.chunk.after	- the list of chunk numbers after current chunk
     *   foo.chunk.last		- the last chunk number
     *   foo.chunk.size		- The max # of items/chunk
     *   foo.chunk.overlap	- The current chunk overlap
     * </pre>
     */

    public void
    tag_list(RewriteContext hr) {
       String name=hr.get("name");		// the name of the list
       String namespace= "ListTemplate:" +
               hr.get("namespace", hr.request.props.getProperty(
               hr.templatePrefix + "namespace",hr.sessionId));

       if (name == null) {
	   debug(hr, "missing list name");
	   return;
       }
       boolean shouldTrack = Format.isTrue(hr.request.props.getProperty(hr.prefix +
	       "track"));
       debug(hr);
       hr.killToken();

       MyList list = (MyList) SessionManager.getSession(namespace, name, null);

       // these operations get done in order

       boolean clear = hr.isTrue("clear");	// clear the list
       String delete = hr.get("delete");	// delete the named item
       String remove = hr.get("remove");	// remove the item at an index
       String insert=hr.get("insert");		// insert onto the list
       String append = hr.get("append");	// append to a list
       boolean unique = hr.isTrue("unique");	// make sure list items are unique
       String maxString=hr.get("max");		// limit the max items
       boolean shouldSort=hr.isTrue("sort");	// simple sort

       // modifiers

       String delim = hr.get("delim");		// for inserting/appending/deleting

       if (list != null && clear) {
	   list.clear();
       }

       if (list != null && delete != null) {	// remove item(s) by value
	   list.remove(delete, delim);
	   if (shouldTrack) {
		System.out.println("<list " + name + "> removing: (" + delete + 
		    ") in namespace: (" + namespace + ")");
	    }
       }

       if (list != null && remove != null) {	// remove item(s)  by index
	   int n = 0;
	   String loc = hr.get("location");
	   if (shouldTrack) {
		System.out.println("<list" + name + "> removing index : (" + loc + 
		    ") in namespace: (" + namespace + ")");
	   }
	   if (loc == null) {
	      list.delete(0);
	   } else {
	       try {
	           list.delete(Integer.decode(loc).intValue());
	       } catch (Exception e) {}
	   }
       }

       if (insert != null) {			// insert item(s)
	   if (list == null) {
	       list = new MyList(name);
	       SessionManager.put(namespace, name, list);
	   }
	   int n = 0;
	   try {
	       n = Integer.decode(hr.get("location")).intValue();
	   } catch (Exception e) {}
	   list.insert(insert, delim, n);
	   if (shouldTrack) {
		System.out.println("<list " + name + "> inserting: (" + insert + 
		    ") at " + n + " in namespace: (" + namespace + ")");
	   }
       }

       if (append != null) {			// append item(s)
	   if (list == null) {
	       list = new MyList(name);
	       SessionManager.put(namespace, name, list);
	   }
	   list.append(append, delim);
	   if (shouldTrack) {
		System.out.println("<list " + name + "> appending: (" + append + 
		    ") in namespace: (" + namespace + ")");
	   }
       }

       if (list != null && unique) {		// remove duplicates
	   list.unique();
       }

       if (list != null && maxString != null) {	// set max length
	    try {
	        int n = Integer.decode(maxString).intValue();
	        if (hr.isTrue("front")) {
		    while(list.size() > n) {
			list.delete(0);
		    }
		} else {
		    list.max(n);
		}
	    } catch (Exception e) {}
       }

       if (list != null && shouldSort) {	// sort the list
	   list.sort();
       }

       /* Manage chunking
        *   overlap=nn		- how many items to overlap between each chunk (default=0)
        *   chunksize=n		- max items per chunk (default=20)
        *   chunk=n		- Set the current chunk number (starts at 0)
        *   next=true|false	- it true, advance to the next page (if available)
        *   previous=true|false	- it true, advance to the previous page (if available)
	*/

       if (list != null) {
	   int n;
	   try {
	        list.overlap = Integer.decode(hr.get("overlap")).intValue();
	   } catch (Exception e) {}
	   try {
	        n = Integer.decode(hr.get("chunksize")).intValue();
		list.chunksize=clamp(1, n, n);
	   } catch (Exception e) {}
	   try {
	        n = Integer.decode(hr.get("chunk")).intValue();
		n = (n<0) ? 1 : n;
		list.chunk=clamp(0, n, list.chunks()-1);
	   } catch (Exception e) {}

	   if (hr.isTrue("next")) {
		list.chunk = clamp(0, list.chunk+1, list.chunks()-1);
           }
	   if (hr.isTrue("previous")) {
		list.chunk = clamp(0, list.chunk-1, list.chunk);
           }
	   list.setDelim(delim);
	   debug(hr,list.toString());
       }

       if (!hr.isSingleton()) {
           listStack.insertElementAt(list, 0);
           if (list != null) {			// add to properties
	       hr.request.addSharedProps(list);
           }
       }
    }

    /**
     * remove the most recent list from the current scope.
     */

    public void
    tag_slash_list(RewriteContext hr) {
	hr.killToken();
	if (listStack.size() > 0) {
	    hr.request.removeSharedProps((Dictionary)listStack.firstElement());
	    listStack.removeElementAt(0);
	}
    }

    /**
     *  Clamp an integer value.
     *  @param min	 minimum legal value
     *  @param value	 the value to clamp
     *  @param max	 maximum legal value
     *  @return		 "value" clamped between min and max
     */

    public static int
    clamp(int min, int value, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Implement a list of strings.  This uses a Vector for its
     * internal implementation, and is a Dictionary to allow
     * convenient access to portions of the list.
     * By implementing Saveable, lists can participate in persistence.
     */

    public static class
    MyList extends Dictionary  implements PropertiesCacheManager.Saveable {
	String name;		// name of this list
	HashVector v;		// contents of the list
	String delim;		// delimiter to use for returning ranges
	String gone=null;	// last deleted item

	/* Manage chunk state */

	public int chunk=0;		// current page in chunk
	public int chunksize=20;	// items per page
	public int overlap=0;		// no. items of overlap between chunks

	/**
	 * Create a named list object.
	 */

        public
	MyList(String name) {
	    this.name=name + ".";
	    this.delim=" ";
	    v = new HashVector();	// this could be an ordinary Vector
	}

	public MyList() {
	    this("undefined");
	}

	public int
	size() {
	    return v.size();
	}

	public boolean
	isEmpty() {
	    return v.isEmpty();
	}

	public void sort() {
	    Sort.qsort(v.getVector());
	}

	/**
	 * Insert a list before position n.
	 * @param s	The list to insert
	 * @param delim The list delimiter (null for a single item(
	 * @param n	The position to insert before
	 */

	public void insert(String s, String delim, int n) {
	    if (delim != null) {
		StringTokenizer st = new StringTokenizer(s, delim);
		while (st.hasMoreTokens()) {
		    Object o = st.nextToken();
		    v.insertElementAt(o, n++);
		}
	    } else {
	        v.insertElementAt(s, n);
	    }
	}

	/**
	 * Append a list to the end of the named list
	 */

	public void
	append(String s, String delim) {
	    if (delim != null) {
		StringTokenizer st = new StringTokenizer(s, delim);
		while (st.hasMoreTokens()) {
		    Object o = st.nextToken();
		    v.addElement(o);
		}
	    } else {
		v.addElement(s);
	    }
	}

	/**
	 * Remove items from a list, by name.
	 */

	public void
	remove(String s, String delim) {
	    if (delim != null) {
		StringTokenizer st = new StringTokenizer(s, delim);
		while (st.hasMoreTokens()) {
		    Object o = st.nextToken();
		    if (v.removeElement(o)) {
			gone = (String) o;
		    }
		}
	    } else if (v.removeElement(s)) {
		gone = s;
	    }
	}

	/**
	 * We should never call this; it's required by the interface.
	 */

	public Object
	remove(Object o) {
	    v.removeElement(o);
	    return null;
	}

	/**
	 * Remove an element by index.
	 */

	public void
	delete(int i) {
	    try {
		gone = null;
		gone = (String) v.elementAt(i);
	        v.removeElementAt(i);
	   } catch (ArrayIndexOutOfBoundsException e) {}
	}

	/**
	 * Clear a list.
	 */

	public
	void clear() {
	    gone = null;
	    max(0);
	}

	/**
	 * Set the max list size.
	 */

	public void
	max(int n) {
	    if (v.size() > n) {
	        v.setSize(n);
	    }
	}

	/**
	 * Remove all non unique elements of the list.
	 * XXX: (cache stupid!)
	 */

	public void unique() {
	    Hashtable t = new Hashtable();
	    for(int i = 0; i < v.size();) {
		Object o = v.elementAt(i);
		if (t.containsKey(o)) {
		    v.removeElementAt(i);
		} else {
		    t.put(o, "");
		    i++;
		}
	    }
	    t.clear();
	}


	/**
	 * Set the delimiter for returning ranges.
	 */

	public void
	setDelim(String delim) {
	    if (delim != null) {
		this.delim  = delim;
	    }
	}

	/**
	 * Get last removed single item (yuk).
	 */

	String gone() {
	    String result = gone;
	    gone=null;
	    return result;
	}

	/*
	 * all these must exist 
	 */

	static String[] items =  new String[] {
	   "first", "last", "all", "count", 
	   "chunk.chunk", "chunk.count", "chunk.chunks", 
	   "chunk.first", "chunk.current", "chunk.last", "chunk.size", 
	   "chunk.overlap"
	};

	/**
	 * Return an enumeration of the "special" keys for this list.
	 */

        public Enumeration
	keys() {
	    return new MyEnumerator(name, items);
        }

	/**
	 * Return the actual list items.
	 */

        public Enumeration elements() {
	    return v.elements();
        }

        /**
         * Get one of the following: "name".
         *  - first, last, count, n, n:m
         */

        static Regexp rangeExp = new Regexp("^([0-9]*):([0-9]*)$");
        static Regexp indexExp = new Regexp("^[0-9]+$");
  
        public Object get(Object k) {
	    String key = (String) k;
	    String[] subs = new String[3];	// for regexp matches
	    String result = null;

	    if (!key.startsWith(name)) {
	        return null;
	    }
	    key = key.substring(name.length());

	    if (key.equals("count")) {				// count
	        result= "" + v.size();
	    } else if (key.equals("gone")) {			// gone
	        result= gone();
	    } else if (key.equals("first") && v.size()>0) {	// first
	        result= "" + v.firstElement();
	    } else if (key.equals("last") && v.size()>0) {	// last
	        result= "" + v.lastElement();
	    } else if (key.equals("all") && v.size()>0) {	// all
	        result= range(0, v.size());
	    } else if (indexExp.match(key, subs)) { 		// a single element
	        int n = -1;
	        try {
	            n = Integer.decode(key).intValue();
	        } catch (Exception e) {}
	        result= (n >= 0 && n < v.size()) ? (String) v.elementAt(n) : null;
	    } else if (rangeExp.match(key, subs)) { 		// n:m (range)
	        int n, m;	
  
	        if (subs[1].equals("")) {
		    n = 0;
	        } else {
	            try {
	                n = Integer.decode(subs[1]).intValue();
	            } catch (Exception e) {
		        return null;
		    }
	        }
	        if (subs[2].equals("")) {
		    m = v.size()-1;
	        } else {
	            try {
	                m = Integer.decode(subs[2]).intValue();
	            } catch (Exception e) {
		        return null;
		    }
	        }
	        result= range(n,m);
	    } else if (key.startsWith("ismember.")) {           // ismember
	        key = key.substring("ismember.".length());
	        result = v.contains(key) ? "yes" : null;
	    } else if (key.startsWith("chunk.")) {		// chunk stuff
	        key = key.substring("chunk.".length());
		result = doChunk(key);
	    } else if (key.startsWith("after.")) {	// list after item
	        key = key.substring("after.".length());
		int pos = v.indexOf(key);
		if (pos >= 0 && pos < v.size()) {
		    result = range(pos+1,v.size());
		}
	    } else if (key.startsWith("before.")) {	// list before item
	        key = key.substring("before.".length());
		int pos = v.indexOf(key);
		if (pos > 0) {
		    result = range(0, pos-1);
		}
	    }
	    return result;
      }

      /**
       * Get all "chunk" related items.
       *   foo.chunk.chunk	- the current list "chunk" (same as foo.chunk,${foo.chunk.current})
       *   foo.chunk.[n]	- the list of items in chunk "n"
       *   foo.chunk.count	- the number of chunks
       *   foo.chunk.chunks	- the list of chunks: "0 1 2 ... n"
       *   foo.chunk.first	- the first chunk number (always 0)
       *   foo.chunk.before	- the list of chunk numbers before current chunk
       *   foo.chunk.current	- The current chunk number
       *   foo.chunk.after	- the list of chunk numbers after current chunk
       *   foo.chunk.last		- the last chunk number
       *   foo.chunk.size		- The max # of items/chunk
       *   foo.chunk.overlap	- The current chunk overlap
       *   foo.chunk.next	- The next chunk (if any)
       *   foo.chunk.previous	- The previous chunk (if any)
       */

      String doChunk(String key) {
	  String result = null;
	  int chunks = chunks();

	  if (key.equals("chunk")) {			// chunk
	      int start = chunk * (chunksize-overlap);
	      int end = start + chunksize-1;
	      result =  range(start, end);
	  } else if (key.equals("count")) {		// count
	      result =  "" + chunks;
	  } else if (key.equals("chunks")) {		// chunks
	      result =  sequence(0, chunks()-1);
	  } else if (key.equals("first")) {		// first
	      result =  "0";
	  } else if (key.equals("before")) {		// before
	      result =  sequence(0, chunk-1);
	  } else if (key.equals("current")) {
	      result =  "" + chunk;
	  } else if (key.equals("after")) {		// after
	      result =  sequence(chunk+1, chunks-1);
	  } else if (key.equals("last")) {		// last
	      result =  "" + (chunks - 1);
	  } else if (key.equals("size")) {		// size
	      result =  "" + chunksize;
	  } else if (key.equals("overlap")) {		// overlap
	      result =  "" + overlap;
	  } else if (key.equals("next")) {		// next
	      result = (chunk+1<chunks) ? "" + (chunk+1) : null;
	  } else if (key.equals("previous")) {		// previous
	      result = (chunk > 0) ? "" + (chunk-1) : null;
	  } else {					// chunk "n"
	      try {
	          int n = Integer.decode(key).intValue();
		  if (n < chunks) {
	             int start = n * (chunksize-overlap);
	             int end = start + chunksize-1;
	             result =  range(start, end);
		  }
              } catch (Exception e) {}
	  }
	  return result;
      }
 
      /**
       * Generate a sequence of integers, inclusive.
       */

      String
      sequence(int start, int end) {
	  if (start > end) {
	      return null;
	  }
	  StringBuffer sb= new StringBuffer("" + start);
	  for(int i=start+1; i<=end; i++) {
	      sb.append(delim).append("" + i);
	  }
	  return sb.toString();
      }

      /** 
       * This is never used; It's required by the interface
       */
  
      public Object put(Object key, Object value) {
	  return null;
      }

      /*
       * Return a range of values from the vector inclusive.
       */

      String range(int i, int j) {
	  if (v.isEmpty()) {
	      return null;
	  }
	  if (j < i) {
	     int tmp=j; j=i; i=tmp;
	  }
	  if (i >= v.size()) {
	      return null;
	  }

	  i = (i<0) ? 0 : i;
	  j = (j >= v.size()) ? v.size() - 1 : j;
	  StringBuffer sb = new StringBuffer((String)v.elementAt(i));
	  for(int k=i+1; k<=j; k++) {
	      sb.append(delim).append(v.elementAt(k));
	  }
	  return sb.toString();
      }

      public String toString() {
	  return "LIST: " + name + ":" + v.toString();
      }

      // chunking support

      public int chunks() {
	  if (overlap > chunksize) {
	      overlap = chunksize-1;
	  }
	  return  (v.size() + chunksize-(overlap*2)-1)/(chunksize-overlap);
      }

      // Implement the saveables

      /**
       * Create a Properties representation of this object,
       * then save it.
       */

      static final String VERSION = "1.0";
      public void save(OutputStream out, String header)  throws IOException {
	  Properties p = new Properties();
	  p.put("version", VERSION);
	  p.put("value", get(name+"all"));
	  p.put("name", name);
	  if (delim != null) {
	      p.put("delim", delim);
	  }
	  if (gone != null) {
	      p.put("gone", gone);
	  }

	  // chunk stuff

	  p.put("chunk", "" + chunksize);
	  p.put("chunksize", "" + chunksize);
	  p.put("overlap",  "" + overlap);

	  p.save(out, header);
      }

      /**
       * load a properties representation of the object, then
       * create the object from it.
       */

      public void load(InputStream in)  throws IOException {
	  Properties p = new Properties();
	  p.load(in);

          String version = p.getProperty("version");
	  if (!version.equals(VERSION)) {
	      throw new IOException("Expecting " + VERSION + " got " + version);
	  }
	  this.name= p.getProperty("name");
	  this.delim = p.getProperty("delim");
	  this.gone = p.getProperty("gone");
	  append(p.getProperty("value"), this.delim);

          /* chunk stuff */

	  chunk = 0;
	  chunksize=20;
	  overlap = 0;
	  try {
	      this.chunk = Integer.decode(p.getProperty("chunk")).intValue();
	      this.chunksize = Integer.decode(p.getProperty("chunksize")).intValue();
	      this.overlap = Integer.decode(p.getProperty("overlap")).intValue();
	  } catch (Exception e) {}
      }
   }
}

/**
 * A vector that keeps a hashtable of items so "ismember" checks are
 * fast.  It would be easy to sub-class Vector, but we're not allowed, 
 * 'cause all the members are final. Geez!  Since we only use this 
 * locally, we only implement the methods we use.
 */

class HashVector {
    Vector v;			// vector of items
    Hashtable cache = null;	// hash table for quick ismember stuff
    static final int THRES=20;  // don't cache below here

    /**
     * Simple counter class, to keep track of duplicates (etc) in hash table.
     */

    class Counter {
	int count;

	Counter()	{ count = 1; }
	void incr()	{ count++; }
	boolean decr()	{ count--; return (count > 0); }
	int count()	{return count;}	// not used
	public String toString() { return "{" + count + "}"; }
    }

    HashVector() {
	cache=null;
	v = new Vector();
    }

    synchronized void addElement(Object o) {
	v.addElement(o);
	cacheInsert(o);
    }

    synchronized boolean contains(Object o) {
	if (cacheIsMember(o)) {
	    return true;
	} else {
	    return v.contains(o);
	}
    }

    synchronized Object elementAt(int i)
	    throws ArrayIndexOutOfBoundsException {
	return v.elementAt(i);
    }

    synchronized void insertElementAt(Object o, int i) {
	v.insertElementAt(o, ListTemplate.clamp(0, i, v.size()));
	cacheInsert(o);
    }

    synchronized boolean isEmpty() {
	return v.isEmpty();
    }

    Vector getVector() {
	return v;
    }

    public Enumeration elements() {
	return v.elements();
    }

    synchronized boolean removeElement(Object o) {
        if (cacheDelete(o)) {
	    return v.removeElement(o);
	} else {
	    return false;
	}
    }

    synchronized void removeElementAt(int i) {
	String s = (String) v.elementAt(i);
	cacheDelete(s);
	v.removeElementAt(i);
    }

    synchronized void setSize(int n) {
	cacheTrimTo(n);
	v.setSize(n);
    }

    Object firstElement() {
	return v.elementAt(0);
    }

    Object lastElement() {
	return v.elementAt(v.size()-1);
    }

    int indexOf(Object o) {
	if (cache!=null && cache.get(o)==null) {
	    return -1;
	} else {
	    return v.indexOf(o);
	}
    }

    synchronized int size() {
	return v.size();
    }

    /*
     * Manage the cache of items for quick ismember checks.
     */

    void cacheInsert(Object o) {
	if (cache != null) {
            Counter count = (Counter) cache.get(o);
	    // System.out.println("Cacheing: " + o + " (" + count + ")");
	    if (count == null) {
	        cache.put(o,new Counter());
	    } else {
		count.incr();
	    }
	}
    }

    // false if sure there isn't one to delete

    boolean cacheDelete(Object o) {
	boolean result = true;
	if (cache != null) {
	    Counter count = (Counter) cache.get(o);
	    if (count != null && !count.decr()) {
		  cache.remove(o);
		  // System.out.println("Removing: " + o);
	    } else {
		result = false;
	    }
	}
	return result;
    }

    // chomp end of vector 

    void cacheTrimTo(int n) {
	if (cache != null) {
	   if (n < THRES) {
	       cache=null;
	   } else {
	       while (v.size() > n) {
		   cacheDelete(v.elementAt(n-1));
		   v.removeElementAt(n-1);
	       }
	   }
	}
    }

    // true if we're sure its there

    boolean cacheIsMember(Object o) {
	if (cache == null && v.size() >= THRES) {
	    cacheFill();
	}
	return (cache!=null && cache.get(o)!=null);
    }

    // create the cache

    void cacheFill() {
	cache = new Hashtable();
	for(int i = 0; i < v.size();i++) {
	    cacheInsert(v.elementAt(i));
	}
    }

    public String toString() {
	String result = "";
	for(int i = 0; i < v.size();i++) {
	    result += "(" + (String) v.elementAt(i) + ") ";
	}
	if (cache != null) {
	    result += " [" + cache.toString() + "]";
	} else {
	    result += "[no cache]";
	}
	// System.out.println(result);
	return result;
    }
}

/**
 * Return an enumeration of the elements in a string array.
 */

class MyEnumerator implements Enumeration {

    String prefix;
    String[] items;
    int element;

    MyEnumerator(String prefix, String[] items) {
	this.prefix = prefix;
	this.items = items;
	element=0;
    }

    public boolean hasMoreElements() {
	return element < items.length;
    }

    public Object
    nextElement() {
	String result =  prefix + items[element++];
	return result;
    }
}
