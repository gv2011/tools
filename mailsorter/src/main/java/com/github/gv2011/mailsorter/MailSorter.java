package com.github.gv2011.mailsorter;

import java.util.Arrays;
import java.util.Properties;

import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPMessage;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;

public class MailSorter {

	public static void main(final String[] args) throws MessagingException {
		final Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		final Session session = Session.getDefaultInstance(props, null);
		try(Store store = session.getStore("imaps")){
			store.connect(args[0], args[1], args[2]);

			final IMAPFolder root = (IMAPFolder) store.getDefaultFolder();

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
