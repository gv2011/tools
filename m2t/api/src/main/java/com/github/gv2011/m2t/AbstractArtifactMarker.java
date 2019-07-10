package com.github.gv2011.m2t;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.net.URL;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.Constant;
import com.github.gv2011.util.Constants;
import com.github.gv2011.util.Equal;
import com.github.gv2011.util.PropertyUtils;
import com.github.gv2011.util.PropertyUtils.SafeProperties;
import com.github.gv2011.util.ann.Artifact;
import com.github.gv2011.util.beans.BeanHashCode;
import com.github.gv2011.util.icol.Opt;

public abstract class AbstractArtifactMarker implements ArtifactMarker{

  private static final Logger LOG = LoggerFactory.getLogger(AbstractArtifactMarker.class);

  private static final ToIntFunction<ArtifactMarker> HASH_CODE = BeanHashCode.createHashCodeFunction(
    ArtifactMarker.class, ArtifactMarker::module, ArtifactMarker::basePackage, ArtifactMarker::artifactRef
  );

  private final Constant<ArtifactRef> ref = Constants.cachedConstant(this::getArtifactRef);

  protected AbstractArtifactMarker(){
    verifyConsistentWithModuleAnnotation();
  }

  private void verifyConsistentWithModuleAnnotation() {
    getAnnotation().ifPresent(artifact->{
      verify(
        artifact.groupId(),
        g->g.isEmpty() || g.equals(artifactRef().groupId().toString()),
        g->format(
          "GroupId from pom.properties ({}) does not match annotated groupId ({}).",
          g, artifactRef().groupId()
        )
      );
      verify(
        artifact.artifactId(),
        id->id.isEmpty() || id.equals(artifactRef().artifactId().toString()),
        id->format(
          "ArtifactId from pom.properties ({}) does not match annotated artifactId ({}).",
          id, artifactRef().artifactId()
        )
      );
    });
  }

  //TODO: investigate
  private Opt<Artifact> getAnnotation() {
    try {
      return Opt.ofNullable(module().getAnnotation(Artifact.class));
    } catch (final NoClassDefFoundError e) {
      LOG.info("Could not read {} annotation of {}.", Artifact.class.getName(), module());
      return Opt.empty();
    }
  }

  @Override
  public final Module module() {
    return getClass().getModule();
  }

  @Override
  public final Package basePackage() {
    return getClass().getPackage();
  }

  @Override
  public final ArtifactRef artifactRef() {
    return ref.get();
  }

  private ArtifactRef getArtifactRef(){
    final SafeProperties props = PropertyUtils.readProperties(getPomProperties()::openStream);
    return BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId).to(props.getProperty(GroupId.M2_NAME))
      .setTStr(ArtifactRef::artifactId).to(props.getProperty(ArtifactId.M2_NAME))
      .setTStr(ArtifactRef::version).to(props.getProperty(Version.M2_NAME))
      .build()
    ;
  }

  protected abstract URL getPomProperties();

  @Override
  public final int hashCode() {
    return HASH_CODE.applyAsInt(this);
  }

  @Override
  public final boolean equals(final Object obj) {
    return Equal.calcEqual(
      this, obj, ArtifactMarker.class, ArtifactMarker::module, ArtifactMarker::basePackage, ArtifactMarker::artifactRef
    );
  }

  @Override
  public final String toString() {
    return basePackage().getName();
  }

}
