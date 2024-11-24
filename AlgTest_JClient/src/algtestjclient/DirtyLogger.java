/*  
    Copyright (c) 2008-2024 Petr Svenda <petr@svenda.com>

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

import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author xsvenda
 */
public class DirtyLogger {
    FileOutputStream    m_logFile;
    boolean             m_bOutputSystemOut = true;
    public DirtyLogger(FileOutputStream logFile, boolean bOutputSystemOut) {
        m_logFile = logFile;
        m_bOutputSystemOut = bOutputSystemOut;
    }
    public void println() {
        String logLine = "\n";
        print(logLine);
    }
    public void println(String logLine)  {
        logLine += "\n";
        print(logLine);
    }
    public void print(String logLine) {
        if (m_bOutputSystemOut) {
            System.out.print(logLine);
        }
        if (m_logFile != null) {
            try {
                m_logFile.write(logLine.getBytes());
            } catch (IOException ex) {
            }
        }
    }    
}
