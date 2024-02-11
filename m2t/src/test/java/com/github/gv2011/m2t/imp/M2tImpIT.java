package com.github.gv2011.m2t.imp;

import static com.github.gv2011.testutil.Assert.assertThat;
import static com.github.gv2011.testutil.Matchers.is;
import static com.github.gv2011.testutil.Matchers.meets;
import static com.github.gv2011.util.icol.ICollections.sortedSetOf;
import static com.github.gv2011.util.icol.ICollections.toISortedSet;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.github.gv2011.m2timp.M2tFactoryImp;
import com.github.gv2011.testutil.AbstractTest;
import com.github.gv2011.util.BeanUtils;
import com.github.gv2011.util.icol.ISortedSet;
import com.github.gv2011.util.m2t.ArtifactRef;
import com.github.gv2011.util.m2t.M2t;
import com.github.gv2011.util.m2t.M2tFactory;
import com.github.gv2011.util.m2t.Scope;
import com.github.gv2011.util.m2t.Type;

public class M2tImpIT extends AbstractTest{

  @Test
  public void testParse() {
    try(M2t m2t = M2tFactory.INSTANCE.get().create()){
      m2t.parse("g:a:1.0");
    }
  }

  @Test
  public void testResolve() {
    final Path file = new M2tFactoryImp().create().resolve(artifactRef());
    assertThat(file, meets(Files::isRegularFile));
    assertThat(file.getFileName().toString(), is("util-apis-0.12.jar"));
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
    final ISortedSet<String> classpath = new M2tFactoryImp().create()
      .getClasspath(artifactRef(), Scope.RUNTIME).stream()
      .map(p->p.getFileName().toString())
      .collect(toISortedSet())
    ;
    assertThat(
      classpath,
      is(sortedSetOf(
        "checker-qual-3.41.0.jar", "error_prone_annotations-2.23.0.jar",
        "failureaccess-1.0.2.jar", "guava-33.0.0-jre.jar", "j2objc-annotations-2.8.jar",
        "jakarta.activation-api-2.1.2.jar", "jsr305-3.0.2.jar",
        "listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar", "slf4j-api-2.0.11.jar",
        "util-apis-0.12.jar")
      )
    );
  }


  @Test
  public void testCopy() {
    final Path dir = testFolder();
    final ArtifactRef artifact = commonsDaemonWin();
    final Path copy = new M2tFactoryImp().create().copy(artifact, dir);
    System.out.println(copy);
    //dontDeleteTestFolder();
  }

  private ArtifactRef artifactRef() {
    return BeanUtils.beanBuilder(ArtifactRef.class)
      .setTStr(ArtifactRef::groupId).to("com.github.gv2011")
      .setTStr(ArtifactRef::artifactId).to("util-apis")
      .setTStr(ArtifactRef::version).to("0.12")
      .build();
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

}
