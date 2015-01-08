package com.oss.oauthapp;
 
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
 
import com.google.common.collect.ImmutableMap;

//Used for pretty printing xml
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

//URL libraries
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.io.*;
import java.util.Scanner;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.xml.sax.InputSource;
 
 
public class GoogleOauthServer {
 
 private Server server = new Server(8089);
 
 private final String clientId = "435470233754-stim0j9rfrilnldf0fhmia40b665icu3.apps.googleusercontent.com";
 private final String clientSecret = "r84Yl_8RuDWMo9_JK95r9Z1q";
  
 public static void main(String[] args) throws Exception {
  new GoogleOauthServer().startJetty();
 }
  
 public void startJetty() throws Exception {
 
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
  
        // map servlets to endpoints
        context.addServlet(new ServletHolder(new SigninServlet()),"/signin");       
        context.addServlet(new ServletHolder(new CallbackServlet()),"/oauth2callback");       
         
        server.start();
        server.join();
 }
 
 class SigninServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
    
   // redirect to google for authorization
   StringBuilder oauthUrl = new StringBuilder().append("https://accounts.google.com/o/oauth2/auth")
   .append("?client_id=").append(clientId) // the client id from the api console registration
   .append("&response_type=code")
   .append("&scope=openid%20email") // scope is the api permissions we are requesting
   .append("&redirect_uri=http://localhost:8089/oauth2callback") // the servlet that google redirects to after authorization
   .append("&state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id")
   .append("&access_type=offline") // here we are asking to access to user's data while they are not signed in
   .append("&approval_prompt=force"); // this requires them to verify which account to use, if they are already signed in
    
   resp.sendRedirect(oauthUrl.toString());
  }
 }
  
 class CallbackServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,IOException {
   // google redirects with
   //http://localhost:8089/oauth2callback?state=this_can_be_anything_to_help_correlate_the_response%3Dlike_session_id&code=4/ygE-kCdJ_pgwb1mKZq3uaTEWLUBd.slJWq1jM9mcUEnp6UAPFm0F2NQjrgwI&authuser=0&prompt=consent&session_state=a3d1eb134189705e9acf2f573325e6f30dd30ee4..d62c
    
   // if the user denied access, we get back an error, ex
   // error=access_denied&state=session%3Dpotatoes
    
   if (req.getParameter("error") != null) {
    resp.getWriter().println(req.getParameter("error"));
    return;
   }
    
   // google returns a code that can be exchanged for a access token
   String code = req.getParameter("code");
    
   // get the access token by post to Google
   String body = post("https://accounts.google.com/o/oauth2/token", ImmutableMap.<String,String>builder()
     .put("code", code)
     .put("client_id", clientId)
     .put("client_secret", clientSecret)
     .put("redirect_uri", "http://localhost:8089/oauth2callback")
     .put("grant_type", "authorization_code").build());
 
   // ex. returns
//   {
//       "access_token": "ya29.AHES6ZQS-BsKiPxdU_iKChTsaGCYZGcuqhm_A5bef8ksNoU",
//       "token_type": "Bearer",
//       "expires_in": 3600,
//       "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjA5ZmE5NmFjZWNkOGQyZWRjZmFiMjk0NDRhOTgyN2UwZmFiODlhYTYifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiZW1haWxfdmVyaWZpZWQiOiJ0cnVlIiwiZW1haWwiOiJhbmRyZXcucmFwcEBnbWFpbC5jb20iLCJhdWQiOiI1MDgxNzA4MjE1MDIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdF9oYXNoIjoieUpVTFp3UjVDX2ZmWmozWkNublJvZyIsInN1YiI6IjExODM4NTYyMDEzNDczMjQzMTYzOSIsImF6cCI6IjUwODE3MDgyMTUwMi5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImlhdCI6MTM4Mjc0MjAzNSwiZXhwIjoxMzgyNzQ1OTM1fQ.Va3kePMh1FlhT1QBdLGgjuaiI3pM9xv9zWGMA9cbbzdr6Tkdy9E-8kHqrFg7cRiQkKt4OKp3M9H60Acw_H15sV6MiOah4vhJcxt0l4-08-A84inI4rsnFn5hp8b-dJKVyxw1Dj1tocgwnYI03czUV3cVqt9wptG34vTEcV3dsU8",
//       "refresh_token": "1/Hc1oTSLuw7NMc3qSQMTNqN6MlmgVafc78IZaGhwYS-o"
//   }
    
   JSONObject jsonObject = null;
    
   // get the access token from json and request info from Google
   try {
    jsonObject = (JSONObject) new JSONParser().parse(body);
   } catch (ParseException e) {
    throw new RuntimeException("Unable to parse json " + body);
   }
    
   // google tokens expire after an hour, but since we requested offline access we can get a new token without user involvement via the refresh token
   String accessToken = (String) jsonObject.get("access_token");
      
   // you may want to store the access token in session
   req.getSession().setAttribute("access_token", accessToken);
    
   // get some info about the user with the access token
   //String json = get(new StringBuilder("https://www.googleapis.com/oauth2/v1/userinfo?access_token=").append(accessToken).toString());
   //Gmail Atom Inbox Feed
   //String json = get(new StringBuilder("https://mail.google.com/mail/feed/atom/oauth2/v1/userinfo?access_token=").append(accessToken).toString());
   //String json = get(new StringBuilder("https://mail.google.com/mail/feed/atom"));
    
   // now we could store the email address in session
    
   // return the json of the user's basic info
   //resp.getWriter().println(json);
   URL url = new URL("http://mail.google.com/mail/feed/atom/");
   //Step 1: Open the connection
   HttpURLConnection connection = (HttpURLConnection) url.openConnection();
   connection.setInstanceFollowRedirects(false);
   connection.addRequestProperty("Authorization", "Bearer " + accessToken);
   connection.connect();

   //Checks for a bug which old APIs have where they will try to redirect you
   //to a different page
   if (connection.getResponseCode() == 302)
   { 
       resp.getWriter().println("We caught the redirect 302 code!");
   BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
   String line = "";
   String xml = "";
   while ((line = reader.readLine()) != null) {
       xml += line;
   }
   //resp.getWriter().print(formatXml(xml));
   resp.getWriter().print(xml);
  }
  }
 }
  
 // makes a GET request to url and returns body as a string
 public String get(String url) throws ClientProtocolException, IOException {
  return execute(new HttpGet(url));
 }
  
 // makes a POST request to url with form parameters and returns body as a string
 public String post(String url, Map<String,String> formParameters) throws ClientProtocolException, IOException {
  HttpPost request = new HttpPost(url);
    
  List <NameValuePair> nvps = new ArrayList <NameValuePair>();
   
  for (String key : formParameters.keySet()) {
   nvps.add(new BasicNameValuePair(key, formParameters.get(key)));
  }
 
  request.setEntity(new UrlEncodedFormEntity(nvps));
   
  return execute(request);
 }
  
 // makes request and checks response code for 200
 private String execute(HttpRequestBase request) throws ClientProtocolException, IOException {
  HttpClient httpClient = new DefaultHttpClient();
  HttpResponse response = httpClient.execute(request);
      
  HttpEntity entity = response.getEntity();
     String body = EntityUtils.toString(entity);
 
  if (response.getStatusLine().getStatusCode() != 200) {
   throw new RuntimeException("Expected 200 but got " + response.getStatusLine().getStatusCode() + ", with body " + body);
  }
 
     return body;
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

}
