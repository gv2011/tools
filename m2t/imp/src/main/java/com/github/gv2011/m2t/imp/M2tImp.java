package com.github.gv2011.m2t.imp;

import static com.github.gv2011.util.CollectionUtils.toSingle;
import static com.github.gv2011.util.CollectionUtils.tryGet;
import static com.github.gv2011.util.StringUtils.removePrefix;
import static com.github.gv2011.util.Verify.notNull;
import static com.github.gv2011.util.Verify.verifier;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.util.ex.Exceptions.notYetImplemented;
import static com.github.gv2011.util.icol.ICollections.listOf;
import static com.github.gv2011.util.icol.ICollections.toISet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

import com.github.gv2011.m2t.ArtifactRef;
import com.github.gv2011.m2t.M2t;
import com.github.gv2011.m2t.Scope;
import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.StreamUtils;
import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.icol.ISet;
import com.github.gv2011.util.lock.Lock;

final class M2tImp implements M2t{

  private static final String POM_TEMPLATE =
    StreamUtils.readText(()->notNull(M2tImp.class.getResourceAsStream("pom-template.xml")))
  ;

  private final Invoker invoker;

  private final Lock lock = Lock.create();
  private final Map<ArtifactRef,Path> cache = new HashMap<>();

  M2tImp(final Invoker invoker) {
    this.invoker = invoker;
  }

  public static void main(final String[] args){
    System.out.println(M2tImp.class.getResource("pom-template.xml"));
  }

  @Override
  public Path resolve(final ArtifactRef artifact) {
    return
      lock.apply(artifact, a->tryGet(cache, artifact))
      .orElseGet(()->{
        final Path dir = call(()->Files.createTempDirectory(M2tImp.class.getSimpleName()).toRealPath());

        final Path pom = dir.resolve("pom.xml");
        FileUtils.writeText(
          format(POM_TEMPLATE, artifact.groupId(), artifact.artifactId(), artifact.version()),
          pom
        );

        final InvocationRequest listDependency = new DefaultInvocationRequest();
        listDependency.setBaseDirectory(dir.toFile());
        listDependency.setGoals(listOf("dependency:list"));
        listDependency.setBatchMode(true);
        final Properties properties = new Properties();
        properties.setProperty("excludeTransitive", Boolean.toString(true));
        properties.setProperty("outputAbsoluteArtifactFilename", Boolean.toString(true));
        final Path out = dir.resolve("out.txt");
        properties.setProperty("outputFile", out.toString());
        listDependency.setProperties(properties);
        call(()->invoker.execute(listDependency));
        final String prefix = format("{}:{}:jar:{}:", artifact.groupId(), artifact.artifactId(), artifact.version());
        final String line = FileUtils.readText(out).lines()
          .map(String::trim)
          .filter(l->l.startsWith(prefix))
          .map(l->removePrefix(l, prefix))
          .collect(toSingle())
        ;
        String fileStr;
        if(line.startsWith("compile:")) fileStr = removePrefix(line, "compile:");
        else fileStr = removePrefix(line, "runtime:");
        final Path result = Paths.get(fileStr);
        verify(Files.isRegularFile(result));
        FileUtils.deleteFile(out);
        FileUtils.deleteFile(pom);
        call(()->dir.toFile().delete());
        lock.run(()->cache.put(artifact, result));
        return result;
      })
    ;
  }

  @Override
  public ISet<Path> getDependenciesFiles(final ArtifactRef artifact, final Scope scope) {
    final Path dir = call(()->Files.createTempDirectory(M2tImp.class.getSimpleName()).toRealPath());

    final InvocationRequest copyPom = new DefaultInvocationRequest();
    copyPom.setBaseDirectory(dir.toFile());
    copyPom.setGoals(listOf("dependency:copy"));
    copyPom.setBatchMode(true);
    {
      final Properties properties = new Properties();
      properties.setProperty(
        "artifact",
        format("{}:{}:{}:pom", artifact.groupId(), artifact.artifactId(), artifact.version())
      );
      properties.setProperty("outputAbsoluteArtifactFilename", Boolean.toString(true));
      properties.setProperty("outputDirectory", ".");
      copyPom.setProperties(properties);
    }
    call(()->invoker.execute(copyPom));

    final Path pom = call(()->Files.list(dir)).findAny().get();
    call(()->Files.move(pom, dir.resolve("pom.xml")));

    final InvocationRequest buildClasspath = new DefaultInvocationRequest();
    buildClasspath.setBaseDirectory(dir.toFile());
    buildClasspath.setGoals(listOf("dependency:build-classpath"));
    buildClasspath.setBatchMode(true);
    final String classpathFileName = "classpath.txt";
    final Path out = dir.resolve(classpathFileName);
    final char psChar = File.pathSeparatorChar;
    {
      final Properties properties = new Properties();
      properties.setProperty("includeScope", scope.toString());
      properties.setProperty("mdep.fileSeparator","/");
      properties.setProperty("mdep.pathSeparator", Character.toString(psChar));
      properties.setProperty("outputAbsoluteArtifactFilename", Boolean.toString(true));
      properties.setProperty("mdep.outputFile", classpathFileName);
      buildClasspath.setProperties(properties);
    }
    call(()->invoker.execute(buildClasspath));

    final ISet<Path> result = StringUtils.split(FileUtils.readText(out), psChar).stream()
      .map(f->Paths.get(f))
      .map(verifier(p->Files.isRegularFile(p)))
      .collect(toISet())
    ;

    FileUtils.deleteFile(out);
    FileUtils.deleteFile(pom);
    call(()->dir.toFile().delete());

    return result;
  }

  @Override
  public ISet<ArtifactRef> getDependencies(final ArtifactRef artifact, final Scope scope) {
    // TODO Auto-generated method stub
    return notYetImplemented();
  }

  public Path copyToDirectory(final Path directory){
    final Path dir = call(()->directory.toRealPath());
    verify(Files.isDirectory(dir));
    final InvocationRequest copyToTmpDir = new DefaultInvocationRequest();
    copyToTmpDir.setGoals(listOf("dependency:copy"));
    final Properties properties = new Properties();
    properties.setProperty("artifact", "com.github.gv2011.m2t:m2t-api:0.0.5-SNAPSHOT:pom");
    properties.setProperty("outputDirectory", dir.toString());
    properties.setProperty("interactiveMode", Boolean.toString(false));
    copyToTmpDir.setProperties(properties);
    call(()->invoker.execute(copyToTmpDir));
    return dir;
  }

}
