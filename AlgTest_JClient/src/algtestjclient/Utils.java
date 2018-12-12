/*  
    Copyright (c) 2008-2014 Petr Svenda <petr@svenda.com>

     LICENSE TERMS

     The free distribution and use of this software in both source and binary
     form is allowed (with or without changes) provided that:

       1. distributions of this source code include the above copyright
          notice, this list of conditions and the following disclaimer;

       2. distributions in binary form include the above copyright
          notice, this list of conditions and the following disclaimer
          in the documentation and/or other associated materials;

       3. the copyright holder's name is not used to endorse products
          built using this software without specific written permission.

     ALTERNATIVELY, provided that this notice is retained in full, this product
     may be distributed under the terms of the GNU General Public License (GPL),
     in which case the provisions of the GPL apply INSTEAD OF those given above.

     DISCLAIMER

     This software is provided 'as is' with no explicit or implied warranties
     in respect of its properties, including, but not limited to, correctness
     and/or fitness for purpose.

    Please, report any bugs to author <petr@svenda.com>
/**/
package algtestjclient;

/**
 *
 * @author github.com/petrs
 */
public class Utils {

    // Parse algorithm name and version of JC which introduced it
    //algParts[0] == algorithm name
    //algParts[1] == introducing version
    //algParts[2] == should be this item included in output? 1/0
    public static String GetAlgorithmName(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        return algParts[0];
    }

    public static String GetAlgorithmIntroductionVersion(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String algorithmVersion = (algParts.length > 1) ? algParts[1] : "";
        return algorithmVersion;
    }

    public static boolean ShouldBeIncludedInOutput(String algorithmInfoString) {
        String[] algParts = algorithmInfoString.split("#");
        String includeInfo = (algParts.length > 2) ? algParts[2] : "1";
        return Integer.decode(includeInfo) != 0;
    }
}
