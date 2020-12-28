package com.github.gv2011.mailsorter;

import com.github.gv2011.util.icol.ISet;

public interface Entity {
	
	String displayName();
	
	ISet<Domain> domains();
	
	EntityType type();
	
	ISet<Channel> channels();

}
