package com.camunda.orderfullfillment;

import java.util.Properties;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.mail.BodyPart;
import javax.mail.internet.MimeBodyPart;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Transport;
import javax.mail.util.ByteArrayDataSource;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

public class ImageSend {

	public static void main(String[] args) throws Exception {

		System.out.println("Sending mail...");
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session mailSession = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("rajeshsurgetech@gmail.com", "hqcelookkyynwjoa");
			}
		});
		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress("rajeshsurgetech@gmail.com"));
		message.setSubject("HTML  mail with images");
		message.setRecipient(Message.RecipientType.TO, new InternetAddress("rajeshsebastin.s@gmail.com"));
		message.setText("Dear Mail Crawler," + "\n\n No spam to my email, please!");

		MimeMultipart multipart = new MimeMultipart("related");
		BodyPart messageBodyPart = new MimeBodyPart();
		String htmlText = "<H1>Raghava chary</H1>" + "<img src=\"cid:image\">";
		messageBodyPart.setContent(htmlText, "text/html");
		multipart.addBodyPart(messageBodyPart);
		try {
			messageBodyPart = new MimeBodyPart();
			InputStream imageStream = ImageSend.class.getClass().getResourceAsStream("C:\\Users\\STS123\\Downloads\\image.png");
			DataSource fds = new ByteArrayDataSource(IOUtils.toByteArray(imageStream), "image/gif");
			messageBodyPart.setDataHandler(new DataHandler(fds));
			messageBodyPart.setHeader("Content-ID", "<image>");
			multipart.addBodyPart(messageBodyPart);
			message.setContent(multipart);
			Transport.send(message);
			System.out.println("Done");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
