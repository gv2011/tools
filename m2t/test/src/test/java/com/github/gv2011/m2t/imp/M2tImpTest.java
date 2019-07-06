package com.github.gv2011.m2t.imp;

import org.junit.Test;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.FileUtils;

public class M2tImpTest {

  @Test
  public void testResolve() {
    final ArtifactRef artifact = BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId).to("com.github.gv2011.m2t")
      .setTStr(ArtifactRef::artifactId).to("m2t-api")
      .setTStr(ArtifactRef::version).to("0.0.5-SNAPSHOT")
      .build()
    ;
    System.out.println(FileUtils.readText(new M2tFactoryImp().create().resolve(artifact)));
  }

  @Test
  public void testEnv() {
    System.getenv().entrySet().forEach(System.out::println);
  }

}
