package com.github.gv2011.m2t;

import java.nio.file.Path;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.icol.ISet;

public interface M2t extends AutoCloseableNt{

  Path resolve(ArtifactRef artifact);

  ISet<ArtifactRef> getDependencies(ArtifactRef artifact, Scope scope);

  ISet<Path> getDependenciesFiles(ArtifactRef artifact, Scope scope);

}
