/* Copyright © 2001 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

// /*
// Workfile:@(#)JavaLoyaltyInterface.java	1.5
// Version:1.5
// Date:03/26/01
// 
// Archive:  /Products/Europa/samples/com/sun/javacard/samples/SampleLibrary/JavaLoyaltyInterface.java 
// Modified:03/26/01 13:37:48
// Original author: Vadim Temkin
// */

package	com.sun.javacard.samples.SampleLibrary;

import javacard.framework.*;

/**
* Shareable Loyalty Interface
*
* @author Vadim Temkin
*/
public interface JavaLoyaltyInterface extends Shareable
{
	/**
	* Used to ask JavaLoyalty Card applet to grant points. <p>
	* Only primitive values, global arrays and Shareable Interface Objects should
	* be passed as parameters and results across a context switch. The byte array
	* buffer is APDU buffer in classes implementing this interface.
	* See <em>Java Card Runtime Environment (JCRE) 2.1 Specification</em> for details.
	* <p> The format of data in the buffer is subset of Transaction Log record format:
	* 2 bytes of 0, 1 byte of transaction type, 2 bytes amount of transaction,
	* 4 bytes of CAD ID, 3 bytes of date, and 2 bytes of time.
	* @param buffer Apdu buffer containing transaction data.
	*/
	void grantPoints (byte[] buffer);
}

