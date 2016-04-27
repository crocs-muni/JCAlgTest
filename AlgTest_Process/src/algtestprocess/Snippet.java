/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algtestprocess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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