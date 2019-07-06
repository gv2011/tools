package com.github.gv2011.m2t;

import com.github.gv2011.util.beans.Bean;

public interface ArtifactRef extends Bean{

  GroupId groupId();

  ArtifactId artifactId();

  Version version();

}
