package com.github.gv2011.mailsorter;

import java.util.Arrays;
import java.util.Properties;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;

public class MailSorter {

	public static void main(String[] args) throws MessagingException {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getDefaultInstance(props, null);
		try(Store store = session.getStore("imaps")){
			store.connect("mail.special-host.de", "test@iglhaut.com", "OjRg62ruIlqrG5j24x6c");

			IMAPFolder root = (IMAPFolder) store.getDefaultFolder();
			
			System.out.println(root.getName());
			Arrays.stream(root.list()).forEach(System.out::println);
			try(final IMAPFolder inbox = (IMAPFolder) root.getFolder("INBOX")){
				inbox.open(Folder.READ_WRITE);
				System.out.println(inbox.getMessageCount());
				final IMAPMessage msg = (IMAPMessage) inbox.getMessage(1);
				System.out.println(msg.getSubject());
				Arrays.stream(msg.getFrom()).forEach(a->System.out.println(((InternetAddress)a).getAddress()));
				
//				msg.writeTo(System.out);
//				inbox.moveMessages(new Message[]{msg}, root.getFolder("test1"));
			}
		}
	}

}
