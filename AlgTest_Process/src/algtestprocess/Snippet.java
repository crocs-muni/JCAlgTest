/*  
    Copyright (c) 2008-2016 Petr Svenda <petr@svenda.com>

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
*/
package algtestprocess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Generates code-snippets from desc.txt file, if key and value (code) are present
 * @author rk
 */
public class Snippet {

	private final String USER_AGENT = "Mozilla/5.0";
	private final String URL = "http://hilite.me/api";
        private String language = "java";
        private String style = "borland";
        private String code = "";

        public void setLanguage(String language) {
            this.language = language;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLanguage() {
            return language;
        }

        public String getStyle() {
            return style;
        }

        public String getCode() {
            return code;
        }
        
        public void addline(String line){
            code += line; 
            code += "%0A!nl!%0A";
        }
        
        public void clear(){
            code = "";
        }
        
        public String getSnippet(){
            String snippet;
            
            try {
                snippet = sendPost();
            } catch (Exception ex) {
                return "Error during snippet generation.";
            }
            snippet = snippet.substring(snippet.indexOf("<pre "),snippet.length()-6); 
            snippet = snippet.replaceAll("!nl!", "</br>"); 
            snippet = snippet.replaceAll("<pre ", "<p class=description "); 
            snippet = snippet.replaceAll("</pre>", "</p>");             
            return snippet;            
        }
        
	public static void main(String[] args) throws Exception {
		Snippet http = new Snippet();
		http.sendPost();
	}        
        
	// HTTP POST request
	private String sendPost() throws Exception {
		URL obj = new URL(URL);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                
		//add reuqest header
		con.setRequestMethod("POST");
                if (code.isEmpty()) code = " ";
                
                //code = URLEncoder.encode(code);
		String urlParameters = "code="+code+"&lexer="+language+"&style="+style;
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		return response.toString();
	}

}