package com.github.gv2011.mailsorter;

public interface EmailChannel extends Channel{
	
	EmailAddress emailAddress();
	
	Category category();

}
