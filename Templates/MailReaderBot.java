import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.io.*;
import java.util.Scanner;

//Used for pretty printing xml
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

//OAuth Import Requirements
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.jackson.JacksonFactory;
//import com.google.api.services.oauth2.Oauth2;
//import com.google.api.services.oauth2.model.Userinfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.InputSource;

public class MailReaderBot {

    public static void main(String[] args) throws IOException
    {
        Scanner scan = new Scanner(System.in);
        String username = "";
        String password = "";
        System.out.print("Enter your gmail user name: ");
        username = scan.next();
        System.out.print("Enter you gmail password: ");
        password = scan.next();
        //Create the URL object
        URL url = new URL("https://mail.google.com/mail/feed/atom/");
        //Step 1: Open the connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //Step 2: Encode the username and password using Base64 encoding + put
        //it in the url
        connection.setRequestProperty("Authorization","Basic" + Base64.encode((username + ":" + password).getBytes()));
        //Step 3: Establish the connection
        connection.connect();


        //Grab the contents of the stream provided by the connection in xml var
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = "";
        String xml = "";
        while ((line = reader.readLine()) != null) {
            xml += line;
        }

        System.out.println("This is the contents of your inbox:");
        System.out.print(formatXml(xml));

    }

    private static String formatXml(String xml){
        try{
            Transformer serializer= SAXTransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            //serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            //serializer.setOutputProperty("{http://xml.customer.org/xslt}indent-amount", "2");
            Source xmlSource=new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());            
            serializer.transform(xmlSource, res);
            return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());
        }catch(Exception e){
            //TODO log error
            return xml;
        }
    }

//    // HTTP POST request
//    private void sendPost() throws Exception {
// 
//        //String url = "https://selfsolve.apple.com/wcResults.do";
//        String url = "https://api.oauth2server.com/token";
//        String CLIENT_PASSWORD = "notasecret";
//        String USERNAME = username;
//        String PASSWORD = password;
//        String CLIENT_ID = "";
//        URL obj = new URL(url);
//        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
// 
//        //add reuqest header
//        con.setRequestMethod("POST");
//        con.setRequestProperty("grant_type", CLIENT_PASSWORD);
//        con.setRequestProperty("username", USERNAME);
//        con.setRequestProperty("password",PASSWORD);
//        con.setRequestProperty("client_id",CLIENT_ID);
// 
//        //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
// 
//        // Send post request
//        con.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//        wr.writeBytes(urlParameters);
//        wr.flush();
//        wr.close();
// 
//        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + urlParameters);
//        System.out.println("Response Code : " + responseCode);
// 
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
// 
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
// 
//        //print result
//        System.out.println(response.toString());
// 
//    }

}

//    private static googlecred()
//    {
//        
//
//    }
