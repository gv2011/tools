package com.github.gv2011.mailsorter;

import static com.github.gv2011.util.ex.Exceptions.notYetImplemented;
import static com.github.gv2011.util.icol.ICollections.emptySet;
import static com.github.gv2011.util.icol.ICollections.toISet;

import java.util.Optional;

import com.github.gv2011.util.icol.ISet;

import jakarta.mail.internet.InternetAddress;

class AdressBook {
	
	private final ISet<Entity> entities = emptySet();
	
	Optional<EmailChannel> getEmailChannel(InternetAddress email){
		final EmailAddress emailAddress = emailAddress(email);
		ISet<EmailChannel> channels = forEmailAddress(emailAddress);
		if(channels.size()>1){
			ISet<EmailChannel> pCh = channels.stream().filter(ch->ch.entity() instanceof Person).collect(toISet());
			if(pCh.size()==1) notYetImplemented();
		}
		if(channels.isEmpty()){
			
		}
		return notYetImplemented();
	}
	
	ISet<EmailChannel> forEmailAddress(EmailAddress emailAddress){
		return entities.stream()
			.flatMap(e->e.channels().stream())
			.filter(EmailChannel.class)
			.filter(ch->ch.emailAddress()
			.equals(emailAddress))
			.collect(toISet())
		;
	}
	
	ISet<EmailChannel> forDomain(Domain domain){
		return entities.stream()
			.flatMap(e->e.channels().stream())
			.filter(EmailChannel.class)
//			.filter(ch->ch.emailAddress().equals(emailAddress))
			.collect(toISet())
		;
	}
	
	private EmailAddress emailAddress(InternetAddress email) {
		// TODO Auto-generated method stub
		return notYetImplemented();
	}


}
