package de.schlund.pfixcore.util.email;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Category;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;



/**
 * Utility class for sending mails via java mail-api.
 *
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */

public class EmailSender {

    private static Category CAT =
        Category.getInstance(EmailSender.class.getName());
    private static final String CHARSET = "ISO-8859-15";

    /**
     * Send a mail.
     * @param subject A String specifying the mail subject. Not null.
     * @param text The text the mail should contain. Not null.
     * @param to A String arry specifying the  recipients. Not null.
     * @param from A String specifying the from address. Not null.
     * @param smtphost A String specifying the smtp-server to use. Not null.
     * @throws EmailSenderException on errors when trying to send the mail.
     * @throws IllegalArgumentException when trying to pass NPs as paramters.
     */
    public static void sendMail(
        String subject,
        String text,
        String[] to,
        String from,
        String smtphost)
        throws EmailSenderException {
        //        Multipart mp         =new MimeMultipart();
        //      MimeBodyPart part    =new MimeBodyPart();

        if(subject == null)
            throw new IllegalArgumentException("EmailSender: A NP as subject is not allowed!");
      
        if(text == null)
            throw new IllegalArgumentException("EmailSender: A NP as text is not allowed!");
            
        if(to == null)
            throw new IllegalArgumentException("EmailSender: A NP as recipient-list is not allowed!");      
      
        if(from == null)
            throw new IllegalArgumentException("EmailSender: A NP as from address is not allowed!");

        if(smtphost == null)
            throw new IllegalArgumentException("EmailSender: A NP as SMTP host is not allowed!");


        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtphost);
        MimeMessage msg =
            new MimeMessage(Session.getDefaultInstance(properties, null));

        StringBuffer strError = new StringBuffer();

        //remove all new lines from subject
        Perl5Util perl = new Perl5Util();
        try {
            String newsubject = perl.substitute("s#\n##", subject);
            subject = newsubject;
        } catch (MalformedPerl5PatternException e) {
            strError.append("Caught " + e.getClass().getName() + "\n");
            strError.append("Message: " + e.getMessage() + "\n");
            throw new EmailSenderException(strError.toString());
        }

        // handle to addresses
        InternetAddress[] toaddresses = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            try {
                toaddresses[i] = new InternetAddress(to[i]);
            } catch (AddressException e) {
                strError.append("Caught " + e.getClass().getName() + "\n");
                strError.append(
                    "Text: " + to[i] + " is not a valid address!" + "\n");
                strError.append("Message: " + e.getMessage() + "\n");
                throw new EmailSenderException(strError.toString());
            }
        }

        // handle from address
        InternetAddress fromaddress = null;
        try {
            fromaddress = new InternetAddress(from);
        } catch (AddressException e) {
            strError.append("Caught " + e.getClass().getName() + "\n");
            strError.append(
                "Text: " + from + " is not a valid address!" + "\n");
            strError.append("Message: " + e.getMessage() + "\n");
            throw new EmailSenderException(strError.toString());
        }

        // got everything, now send mail
        try {
            msg.setText(text, CHARSET);
            msg.setHeader("Content-Type", "text/plain; charset=" + CHARSET);
            msg.setHeader("Content-Transfer-Encoding", "8bit");
            msg.setRecipients(Message.RecipientType.TO, toaddresses);
            msg.setSubject(subject, CHARSET);
            msg.setFrom(fromaddress);
            msg.setSentDate(new Date());
            
            Transport.send(msg);
        } catch (MessagingException mex) {
            Exception ex = mex;
            strError.append("Caught " + mex.getClass().getName() + "\n");
            strError.append("Message: " + mex.getMessage() + "\n\n");
            do {
                if (ex instanceof SendFailedException) {
                    SendFailedException sfex = (SendFailedException) ex;
                    Address[] invalid = sfex.getInvalidAddresses();
                    if (invalid != null) {
                        strError.append("** Invalid Addresses\n");
                        if (invalid != null) {
                            for (int i = 0; i < invalid.length; i++)
                                strError.append(" " + invalid[i] + "\n");
                        }
                    }
                    Address[] validUnsent = sfex.getValidUnsentAddresses();
                    if (validUnsent != null) {
                        strError.append("    ** ValidUnsent Addresses\n");
                        if (validUnsent != null) {
                            for (int i = 0; i < validUnsent.length; i++)
                                strError.append(" " + validUnsent[i] + "\n");
                        }
                    }
                    Address[] validSent = sfex.getValidSentAddresses();
                    if (validSent != null) {
                        strError.append("    ** ValidSent Addresses\n");
                        if (validSent != null) {
                            for (int i = 0; i < validSent.length; i++)
                                strError.append(" " + validSent[i] + "\n");
                        }
                    }
                }
                strError.append("\n");
                if (ex instanceof MessagingException)
                    ex = ((MessagingException) ex).getNextException();
                else
                    ex = null;
            } while (ex != null);
            throw new EmailSenderException(strError.toString());
        }
    }

}
