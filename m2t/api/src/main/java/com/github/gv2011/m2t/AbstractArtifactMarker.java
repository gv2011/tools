package com.github.gv2011.m2t;

import java.net.URL;
import java.util.function.ToIntFunction;

import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.Constant;
import com.github.gv2011.util.Constants;
import com.github.gv2011.util.Equal;
import com.github.gv2011.util.PropertyUtils;
import com.github.gv2011.util.PropertyUtils.SafeProperties;
import com.github.gv2011.util.beans.BeanHashCode;

public abstract class AbstractArtifactMarker implements ArtifactMarker{

  private static final ToIntFunction<ArtifactMarker> HASH_CODE = BeanHashCode.createHashCodeFunction(
    ArtifactMarker.class, ArtifactMarker::module, ArtifactMarker::basePackage, ArtifactMarker::artifactRef
  );

  private final Constant<ArtifactRef> ref = Constants.cachedConstant(this::getArtifactRef);

  protected AbstractArtifactMarker(){}

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
