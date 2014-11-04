import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.net.HttpURLConnection;
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

    public static String formatXml(String xml){
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

