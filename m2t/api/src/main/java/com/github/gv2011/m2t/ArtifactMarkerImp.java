package com.github.gv2011.m2t;

import java.net.URL;
import java.util.function.ToIntFunction;

import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.Equal;
import com.github.gv2011.util.PropertyUtils;
import com.github.gv2011.util.PropertyUtils.SafeProperties;
import com.github.gv2011.util.beans.BeanHashCode;

final class ArtifactMarkerImp implements ArtifactMarker{

  static ArtifactMarker getMarker(final Class<?> clazz, final URL pomPropertiesResource){
    final SafeProperties props = PropertyUtils.readProperties(pomPropertiesResource::openStream);
    final ArtifactRef ref = BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId).to(props.getProperty(GroupId.M2_NAME))
      .setTStr(ArtifactRef::artifactId).to(props.getProperty(ArtifactId.M2_NAME))
      .setTStr(ArtifactRef::version).to(props.getProperty(Version.M2_NAME))
      .build()
    ;
    return new ArtifactMarkerImp(clazz, ref);
  }

  private static final ToIntFunction<ArtifactMarker> HASH_CODE = BeanHashCode.createHashCodeFunction(
    ArtifactMarker.class, ArtifactMarker::module, ArtifactMarker::basePackage, ArtifactMarker::artifactRef
  );

  private final Class<?> clazz;
  private final ArtifactRef ref;

  ArtifactMarkerImp(final Class<?> clazz, final ArtifactRef ref) {
    this.clazz = clazz;
    this.ref = ref;
  }

  @Override
  public Module module() {
    return clazz.getModule();
  }

  @Override
  public Package basePackage() {
    return clazz.getPackage();
  }

  @Override
  public ArtifactRef artifactRef() {
    return ref;
  }

  @Override
  public int hashCode() {
    return HASH_CODE.applyAsInt(this);
  }

  @Override
  public boolean equals(final Object obj) {
    return Equal.calcEqual(
      this, obj, ArtifactMarker.class, ArtifactMarker::module, ArtifactMarker::basePackage, ArtifactMarker::artifactRef
    );
  }

  @Override
  public String toString() {
    return basePackage().getName();
  }

}
