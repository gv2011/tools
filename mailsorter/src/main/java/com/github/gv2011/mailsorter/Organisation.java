package com.github.gv2011.mailsorter;

public interface Organisation extends Entity{
	
	@Override
	default EntityType type(){
		return EntityType.ORGANISATION;
	}

}
