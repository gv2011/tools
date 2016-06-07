package com.github.gv2011.tools.mailfilter;

import static com.github.gv2011.util.PropertyUtils.readProperties;
import static com.github.gv2011.util.SetUtils.asSet;
import static com.github.gv2011.util.SetUtils.intersection;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import com.github.gv2011.util.PropertyUtils.SafeProperties;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;

public class ImapTest {

  private final SortedSet<String> usedHeaders = new TreeSet<>();
  private final SortedSet<String> tos = new TreeSet<>();
  private final SortedSet<String> myMails = new TreeSet<>();
  private final Set<String> knownMails;
  private IMAPFolder inbox;
  private IMAPFolder others;

  public static void main(final String[] args) throws Exception {
    new ImapTest(new EmailsFromContacts().getEmails()).main1();
  }


  public ImapTest(final Set<String> knownMails) {
    this.knownMails = knownMails;
  }


  public void main1() throws Exception {
    myMails.add("eberhard@iglhaut.com");
    final SafeProperties mbox = readProperties("mailbox.properties");

    myMails.addAll(mbox.getMultiple("ownEmailAddresses"));

    final Properties props = new Properties();
    props.setProperty("mail.store.protocol", "imap");
    props.setProperty("mail.imap.host", mbox.getProperty("host"));
    props.setProperty("mail.imap.port", mbox.getProperty("port"));
    props.setProperty("mail.imap.ssl.enable", "true");
    props.setProperty("mail.imap.peek", "true");
    final Authenticator authenticator = new Authenticator(){
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(mbox.getProperty("user"), mbox.getProperty("password"));
      }};
    final Session session = Session.getDefaultInstance(props, authenticator);

    final Store store = session.getStore("imap");
    store.connect();
    inbox = (IMAPFolder) store.getFolder("INBOX");
    inbox.open(Folder.READ_ONLY);
    others = (IMAPFolder) store.getFolder("ToOthers");
//    others.open(Folder.READ_ONLY);
    final Message[] messages = inbox.getMessages();
    System.out.println(messages.length);
    for(final Message msg: messages){
      final IMAPMessage m = (IMAPMessage)msg;
      filter(m);
    }
    final Folder root = store.getDefaultFolder();
    for(final Folder f: root.list()) System.out.println(f.getName());
    store.close();
    System.out.println(usedHeaders);
    System.out.println(tos);
  }

  private void filter(final IMAPMessage m) throws MessagingException {
    final Enumeration<Header> headers = m.getAllHeaders();
    while(headers.hasMoreElements()){
      final Header h = headers.nextElement();
      usedHeaders .add(h.getName());
    }
    for(final String to: m.getHeader("To")) tos.add(to);

    if(intersection(asSet(m.getHeader("To")), myMails).isEmpty()){
      inbox.moveMessages(new Message[]{m}, others);
    }
  }


}
