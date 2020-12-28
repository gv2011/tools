package com.github.gv2011.mailsorter;

public interface Person extends Entity{
	
	@Override
	default EntityType type(){
		return EntityType.PERSON;
	}


}
