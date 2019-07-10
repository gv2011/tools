package com.github.gv2011.m2t;

import com.github.gv2011.util.beans.Bean;
import com.github.gv2011.util.beans.DefaultValue;

public interface ArtifactRef extends Bean{

  GroupId groupId();

  ArtifactId artifactId();

  Version version();

  @DefaultValue("")
  Classifier classifier();

  Type type();

}
