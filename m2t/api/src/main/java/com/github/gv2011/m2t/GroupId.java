package com.github.gv2011.m2t;

import com.github.gv2011.util.tstr.TypedString;

public interface GroupId extends TypedString<GroupId>{

  public static final String M2_NAME = "groupId";

  static GroupId create(final String groupIdString){
    return TypedString.create(GroupId.class, groupIdString);
  }

}
