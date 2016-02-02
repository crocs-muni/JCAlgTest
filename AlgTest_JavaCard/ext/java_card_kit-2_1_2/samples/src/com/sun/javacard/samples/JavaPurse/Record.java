/* Copyright © 2001 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

// /*
// Workfile:@(#)Record.java	1.5
// Version:1.5
// Date:03/26/01
// 
// Archive:  /Products/Europa/samples/com/sun/javacard/samples/JavaPurse/Record.java 
// Modified:03/26/01 13:37:48
// Original author: Zhiqun Chen
// */

package	com.sun.javacard.samples.JavaPurse;

/**
 * A Record.
 * <p>The main reason for this class is that Java Card doesn't support multidimensional
 * arrays, but supports array of objects
 */

class Record
{

	byte[] record;

	Record(byte[] data) {
      this.record = data;
    }

    Record(short size) {
      record = new byte[size];
    }

}

