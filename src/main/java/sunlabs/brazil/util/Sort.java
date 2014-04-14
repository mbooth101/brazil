/*
 * Sort.java
 *
 * Brazil project web application toolkit,
 * export version: 2.3 
 * Copyright (c) 1999-2004 Sun Microsystems, Inc.
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
 * Contributor(s): cstevens, suhler.
 *
 * Version:  2.2
 * Created by suhler on 99/08/05
 * Last modified by suhler on 04/11/30 14:59:57
 *
 * Version Histories:
 *
 * 2.2 04/11/30-14:59:57 (suhler)
 *   fixed sccs version string
 *
 * 2.1 02/10/01-16:36:57 (suhler)
 *   version change
 *
 * 1.6 02/07/24-10:49:38 (suhler)
 *   doc updates
 *
 * 1.5 99/10/14-12:57:25 (cstevens)
 *   Documentation for Sort.Compare.
 *
 * 1.4 99/08/27-13:15:58 (cstevens)
 *   Merged changes between child workspace "/home/cstevens/ws/brazil/naws" and
 *   parent workspace "/export/ws/brazil/naws".
 *
 * 1.2.1.1 99/08/27-12:31:11 (cstevens)
 *   check the common case first
 *
 * 1.3 99/08/12-16:00:29 (suhler)
 *   type
 *
 * 1.2 99/08/10-16:17:28 (cstevens)
 *   sort arrays of things.
 *
 * 1.2 99/08/05-17:03:08 (Codemgr)
 *   SunPro Code Manager data about conflicts, renames, etc...
 *   Name history : 1 0 util/Sort.java
 *
 * 1.1 99/08/05-17:03:07 (suhler)
 *   date and time created 99/08/05 17:03:07 by suhler
 *
 */

package sunlabs.brazil.util;

import java.util.Vector;
import java.lang.reflect.Array;

/**
 * Placeholder for useful sorting utilities.
 * Currently, sorting arrays and Vectors using the qsort algorithm
 * are preovided.
 *
 * @author	Stephen Uhler (stephen.uhler@sun.com)
 * @author	Colin Stevens (colin.stevens@sun.com)
 * @version	2.2
 */

public class Sort
{

    private Sort() {}

    /**
     * Sort a vector of strings using the Qsort algorithm.
     * The compareTo method of the String class is used for comparison.
     */

    public static void
    qsort(Vector strings) {
	qsort(strings,0,strings.size()-1);
    }

    private static void
    qsort(Vector A, int p, int r) {
	while (p < r) {
	    int q = partition(A, p, r);
	    qsort(A,p,q);
	    p = q + 1;
	}
    }

    private static int
    partition (Vector A, int p, int r) {
	int z = (r+p)/2;
	String x = A.elementAt(z).toString();
	int i = p - 1;
	int j = r + 1;

	while (true) {
	    while (x.compareTo(A.elementAt(--j).toString()) < 0);
	    while (x.compareTo(A.elementAt(++i).toString()) > 0);

	    if (i >= j) {
		return j;
	    }

	    Object o = A.elementAt(i);
	    A.setElementAt(A.elementAt(j),i);
	    A.setElementAt(o,j);
	}
    }

    /**
     * This interface is used by the <code>Sort</code> class to compare
     * elements when an array is being sorted.
     *
     * @author	Colin Stevens (colin.stevens@sun.com)
     * @version	2.2, 04/11/30
     */
    public interface Compare {
	/**
	 * Compare two elements in the given array.  The implementor must
	 * know what the actual type of the array is and cast it to that
	 * type in order to do the comparison.
	 *
	 * @param   array
	 *	    Array being sorted.
	 *
	 * @param   index1
	 *	    The index in the given array of the first element to
	 *	    compare.
	 *
	 * @param   index2
	 *	    The index in the given array of the second element to
	 *	    compare.
	 *
	 * @return  The implementation must return a number less than,
	 *	    equal to, or greater than zero to indicate whether
	 *	    the array element at <code>index1</code> should be
	 *	    considered less than, equal to, or greater than the
	 *	    array element at <code>index2</code>.
	 */	    
	int compare(Object array, int index1, int index2);
    }

    /**
     * Sorts an array of the basic types (ints, floats, bytes, etc.) or
     * Strings.  The sort is in increasing order, and is case-sensitive
     * for strings.  Sorting an array of booleans or an array of objects
     * other than Strings is not supported.  
     *
     * @param	array
     *		The array to sort in place.
     *
     * @throws	IllegalArgumentException
     *		if <code>array</code> is not an array of the types listed
     *		above.
     */
    public static void
    qsort(Object array)
	throws IllegalArgumentException
    {
	qsort(array, new StandardCompare(array));
    }

    /**
     * Sorts an array.  The specified comparator is used to control the
     * sorting order.  Arrays of any type may be sorted, depending upon
     * what the comparator accepts.
     *
     * @param	array
     *		The array to sort in place.
     *
     * @param	compare
     *		The comparator for sort order.
     *
     * @throws	IllegalArgumentException
     *		if <code>array</code> is not an array.
     */
    public static void
    qsort(Object array, Compare compare)
	throws IllegalArgumentException
    {
	qsort(array, 0, Array.getLength(array) - 1, compare);
    }

    private static void
    qsort(Object A, int p, int r, Compare compare)
    {
	while (p < r) {
	    int q = partition(A, p, r, compare);
	    qsort(A, p, q, compare);
	    p = q + 1;
	}
    }

    private static int
    partition(Object A, int p, int r, Compare compare)
    {
	int z = (r+p)/2;
	int i = p - 1;
	int j = r + 1;

	while (true) {
	    while (compare.compare(A, z, --j) < 0) {
		/* do nothing. */
	    }
	    while (compare.compare(A, z, ++i) > 0) {
		/* do nothing. */
	    }
	    if (i >= j) {
		return j;
	    }

	    Object o = Array.get(A, i);
	    Array.set(A, i, Array.get(A, j));
	    Array.set(A, j, o);

	    if (z == i) {
		z = j;
	    } else if (z == j) {
		z = i;
	    }
	}
    }

    private static final class StandardCompare implements Sort.Compare
    {
	/*
	 * Keep the two expected cases near the front.
	 */
	private static final int STRING = 0;
	private static final int INT = 1;

	private static final int BYTE = 2;
	private static final int CHAR = 3;
	private static final int SHORT = 4;
	private static final int LONG = 5;
	private static final int FLOAT = 6;
	private static final int DOUBLE = 7;

	int type;

	StandardCompare(Object array)
	    throws IllegalArgumentException
	{
	    if (array.getClass().isArray() == false) {
		/*
		 * Let the qsort routine throw the "not an array exception".
		 */
		return;
	    } else if (array instanceof String[]) {
		type = STRING;
	    } else if (array instanceof int[]) {
		type = INT;
	    } else if (array instanceof byte[]) {
		type = BYTE;
	    } else if (array instanceof char[]) {
		type = CHAR;
	    } else if (array instanceof short[]) {
		type = SHORT;
	    } else if (array instanceof long[]) {
		type = LONG;
	    } else if (array instanceof float[]) {
		type = FLOAT;
	    } else if (array instanceof double[]) {
		type = DOUBLE;
	    } else {
		throw new IllegalArgumentException("cannot sort array of"
			+ array.getClass().getComponentType().getName());
	    }
	}

	public int
	compare(Object array, int index1, int index2)
	{
	    switch (type) {
		case STRING: {
		    String[] a = (String[]) array;
		    String str1 = a[index1];
		    String str2 = a[index2];
		    return str1.compareTo(str2);
		}
		case INT: {
		    int[] a = (int[]) array;
		    int i1 = a[index1];
		    int i2 = a[index2];
		    return i1 - i2;
		}
		case BYTE: {
		    byte[] a = (byte[]) array;
		    byte b1 = a[index1];
		    byte b2 = a[index2];
		    return b1 - b2;
		}
		case CHAR: {
		    char[] a = (char[]) array;
		    char c1 = a[index1];
		    char c2 = a[index2];
		    return c1 - c2;
		}
		case SHORT: {
		    short[] a = (short[]) array;
		    short s1 = a[index1];
		    short s2 = a[index2];
		    return s1 - s2;
		}
		case LONG: {
		    long[] a = (long[]) array;
		    long l1 = a[index1];
		    long l2 = a[index2];
		    if (l1 > l2) {
			return 1;
		    } else if (l2 == l2) {
			return 0;
		    } else {
			return -1;
		    }
		}
		case FLOAT: {
		    float[] a = (float[]) array;
		    float f1 =  a[index1];
		    float f2 =  a[index2];
		    if (f1 > f2) {
			return 1;
		    } else if (f1 == f2) {
			return 0;
		    } else {
			return -1;
		    }
		}
		case DOUBLE: {
		    double[] a = (double[]) array;
		    double d1 = a[index1];
		    double d2 = a[index2];
		    if (d1 > d2) {
			return 1;
		    } else if (d1 == d2) {
			return 0;
		    } else {
			return -1;
		    }
		}
	    }
	    return 0;
	}
    }
}
