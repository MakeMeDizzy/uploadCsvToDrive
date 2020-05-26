import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_FILE,
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email");


    private static Credential getCredentials() throws IOException {
        return GoogleCredential.fromStream(new FileInputStream("quickstart-1589782586719-ce15e5308073.json"))
                .createScoped(SCOPES);

    }

    public static void main(String... args) throws IOException, GeneralSecurityException, MessagingException, SQLException, ClassNotFoundException {

        java.io.File file1 = new java.io.File("text.sql");
        try (FileReader fr = new FileReader(file1);
             BufferedReader reader = new BufferedReader(fr)){
            CSV exporter = new CSV();
            String line = reader.readLine();
            while(line != null){
                int bi = line.indexOf("dBegin");
                int ei = line.indexOf("dEnd");
                if(bi != -1 && ei != -1){
                    String newNode = line.replaceAll("\\bdBegin\\b", args[0]);
                    String newNode2 = newNode.replaceAll("\\bdEnd\\b", args[1]);
                    uploadToDrive(exporter.export("review", newNode2), args);
                } else {
                    uploadToDrive(exporter.export("review", line));
                }
                line = reader.readLine();
            }
        }


//        FileList result = service.files().list()
//                .setPageSize(1000)
//                .setFields("nextPageToken, files(id,webContentLink)")
//                .execute();
//        List<com.google.api.services.drive.model.File> files =result.getFiles();
//        System.out.println(files);


    }



    public static void uploadToDrive(String fileName, String... args) throws MessagingException, GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();


        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList("1WVm5SGnN7myz6gM30sj8d09TML1ZVgG5"));
        java.io.File filePath = new java.io.File(fileName);
        FileContent mediaContent = new FileContent("text/csv", filePath);
        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        System.out.println("File ID: " + file.getId());

        SendMail(file.getWebContentLink(),filePath, args);
    }


    public static void SendMail(String webContentLink,java.io.File file, String... args) throws javax.mail.MessagingException {


        try (InputStream inputStream = DriveQuickstart.class.getClassLoader().getResourceAsStream("sent.properties")) {

            Properties properties = new Properties();
            properties.load(inputStream);
            Session session = Session.getDefaultInstance(properties, null);

            String host = properties.getProperty("mail.smtp.host");
            String password = properties.getProperty("password.t");
            String from = properties.getProperty("from.t");
            String[] toAddress = properties.getProperty("toAddress.t").split(",");


            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.setRecipients(Message.RecipientType.TO, Arrays.toString(toAddress));
            mimeMessage.setSubject(Arrays.toString(args));

            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText("Here is the file");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            bodyPart = new MimeBodyPart();

            //DataSource dataSource = new FileDataSource(String.valueOf(file));
            //bodyPart.setDataHandler(new DataHandler(dataSource));
            bodyPart.setText(webContentLink);
            bodyPart.setFileName(String.valueOf(file));
            multipart.addBodyPart(bodyPart);
            mimeMessage.setContent(multipart);

            try (Transport tr = session.getTransport("smtps")) {
                tr.connect(host, from, password);
                tr.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                System.out.println("Mail Sent Succesfully");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}