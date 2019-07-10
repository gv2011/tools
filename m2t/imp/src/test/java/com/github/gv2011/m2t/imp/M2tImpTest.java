package com.github.gv2011.m2t.imp;

import static com.github.gv2011.testutil.Assert.assertThat;
import static com.github.gv2011.testutil.Matchers.is;
import static com.github.gv2011.testutil.Matchers.meets;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.Scope;
import com.github.gv2011.m2t.Type;
import com.github.gv2011.testutil.AbstractTest;
import com.github.gv2011.util.BeanUtils;

public class M2tImpTest extends AbstractTest{

  @Test
  public void testResolve() {
    final ArtifactRef artifact = BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId).to("com.github.gv2011.m2t")
      .setTStr(ArtifactRef::artifactId).to("m2t-api")
      .setTStr(ArtifactRef::version).to("0.0.5-SNAPSHOT")
      .build()
    ;
    final Path file = new M2tFactoryImp().create().resolve(artifact);
    assertThat(file, meets(Files::isRegularFile));
    assertThat(file.getFileName().toString(), is("m2t-api-0.0.5-SNAPSHOT.jar"));
  }

  @Test
  public void testResolveWithClassifierAndType() {
    final ArtifactRef artifact = commonsDaemonWin();
    final Path file = new M2tFactoryImp().create().resolve(artifact);
    assertThat(file, meets(Files::isRegularFile));
    assertThat(file.getFileName().toString(), is("commons-daemon-1.2.0-bin-windows.zip"));
  }

  @Test
  public void testGetClasspath() {
    final ArtifactRef artifact = BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId).to("com.github.gv2011.m2t")
      .setTStr(ArtifactRef::artifactId).to("m2t-api")
      .setTStr(ArtifactRef::version).to("0.0.5-SNAPSHOT")
      .build()
    ;
    System.out.println(new M2tFactoryImp().create().getClasspath(artifact, Scope.RUNTIME));
  }


  @Test
  public void testCopy() {
    final Path dir = testFolder();
    final ArtifactRef artifact = commonsDaemonWin();
    final Path copy = new M2tFactoryImp().create().copy(artifact, dir);
    System.out.println(copy);
    //dontDeleteTestFolder();
  }

  private ArtifactRef commonsDaemonWin() {
    final ArtifactRef artifact = BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId   ).to("commons-daemon")
      .setTStr(ArtifactRef::artifactId).to("commons-daemon")
      .setTStr(ArtifactRef::version   ).to("1.2.0")
      .setTStr(ArtifactRef::classifier).to("bin-windows")
      .set    (ArtifactRef::type      ).to(Type.ZIP)
      .build()
    ;
    return artifact;
  }



  @Test
  public void testEnv() {
    System.getenv().entrySet().forEach(System.out::println);
  }

}
