package com.github.gv2011.m2timp;

import static com.github.gv2011.util.CollectionUtils.toSingle;
import static com.github.gv2011.util.CollectionUtils.tryGet;
import static com.github.gv2011.util.StringUtils.removePrefix;
import static com.github.gv2011.util.StringUtils.split;
import static com.github.gv2011.util.Verify.notNull;
import static com.github.gv2011.util.Verify.verifier;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.Verify.verifyEqual;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;
import static com.github.gv2011.util.ex.Exceptions.wrap;
import static com.github.gv2011.util.icol.ICollections.listOf;
import static com.github.gv2011.util.icol.ICollections.toISortedSet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.StreamUtils;
import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.icol.ISortedSet;
import com.github.gv2011.util.icol.Opt;
import com.github.gv2011.util.lock.Lock;
import com.github.gv2011.util.m2t.ArtifactRef;
import com.github.gv2011.util.m2t.M2t;
import com.github.gv2011.util.m2t.Scope;

final class M2tImp implements M2t{

  private static final Logger LOG = LoggerFactory.getLogger(M2tImp.class);

  private static final String POM_TEMPLATE =
    StreamUtils.readText(()->notNull(M2tImp.class.getResourceAsStream("pom-template.txt")))
  ;

  private final Lock lock = Lock.create();
  private final Map<ArtifactRef,Path> cache = new HashMap<>();

  M2tImp() {}

  public static void main(final String[] args){
    System.out.println(M2tImp.class.getResource("pom-template.xml"));
  }

  @Override
  public Path resolve(final ArtifactRef artifact) {
    return
      lock.apply(artifact, a->tryGet(cache, artifact))
      .orElseGet(()->{
        final Path dir = createTmpDirectoryWithFakePom(artifact);

        final InvocationRequest listDependency = new DefaultInvocationRequest();
        listDependency.setBaseDirectory(dir.toFile());
        listDependency.setGoals(listOf("dependency:list"));
        final Properties properties = new Properties();
        properties.setProperty("excludeTransitive", Boolean.toString(true));
        properties.setProperty("outputAbsoluteArtifactFilename", Boolean.toString(true));
        final Path out = dir.resolve("out.txt");
        properties.setProperty("outputFile", out.toString());
        listDependency.setProperties(properties);
        execute(listDependency);
        final String classifier = artifact.classifier().isEmpty() ? "" : format(":{}", artifact.classifier());
        final String prefix = format(
          "{}:{}:{}{}:{}:",
          artifact.groupId(), artifact.artifactId(), artifact.type(), classifier, artifact.version()
        );
        final String line = FileUtils.readText(out).lines()
          .map(String::trim)
          .filter(l->{
            final boolean found = l.startsWith(prefix);
            LOG.trace("Match: {}. Prefix: {}, line: {}.", found, prefix, l);
            return found;
          })
          .map(l->removePrefix(l, prefix))
          .collect(toSingle())
        ;
        String fileStr;
        if(line.startsWith("compile:")) fileStr = removePrefix(line, "compile:");
        else fileStr = removePrefix(line, "runtime:");
        final Path result = Paths.get(fileStr);
        verify(Files.isRegularFile(result));
        FileUtils.deleteFile(out);
        FileUtils.deleteFile(dir.resolve("pom.xml"));
        call(()->dir.toFile().delete());
        lock.run(()->cache.put(artifact, result));
        return result;
      })
    ;
  }

  @Override
  public ISortedSet<Path> getClasspath(final ArtifactRef artifact, final Scope scope) {
    return getClasspathFromPom(createTmpDirectoryWithFakePom(artifact), scope);
  }

  @Override
  public ISortedSet<Path> getClasspath(final Bytes pom, final Scope scope) {
    return getClasspathFromPom(createTmpDirectoryFromPom(pom), scope);
  }


private ISortedSet<Path> getClasspathFromPom(final Path dir, final Scope scope) {
    final InvocationRequest buildClasspath = new DefaultInvocationRequest();
    buildClasspath.setBaseDirectory(dir.toFile());
    buildClasspath.setGoals(listOf("dependency:build-classpath"));
    final String classpathFileName = "classpath.txt";
    final Path out = dir.resolve(classpathFileName);
    final char pathSeparatorChar = File.pathSeparatorChar;
    {
      final Properties properties = new Properties();
      properties.setProperty("includeScope", scope.toString());
      properties.setProperty("mdep.fileSeparator", Character.toString(File.separatorChar));
      properties.setProperty("mdep.pathSeparator", Character.toString(pathSeparatorChar));
      properties.setProperty("outputAbsoluteArtifactFilename", Boolean.toString(true));
      properties.setProperty("mdep.outputFile", classpathFileName);
      buildClasspath.setProperties(properties);
    }
    execute(buildClasspath);

    final ISortedSet<Path> result = StringUtils.split(FileUtils.readText(out), pathSeparatorChar).stream()
      .map(f->Paths.get(f))
      .map(verifier(p->Files.isRegularFile(p)))
      .collect(toISortedSet())
    ;

    FileUtils.deleteFile(out);
    FileUtils.deleteFile(dir.resolve("pom.xml"));
    call(()->dir.toFile().delete());

    return result;
  }

  @Override
  public Path copy(final ArtifactRef artifact, final Path directory) {
    final Path dir = call(()->directory.toRealPath());
    verify(Files.isDirectory(dir));
    final InvocationRequest copyToTmpDir = new DefaultInvocationRequest();
    copyToTmpDir.setGoals(listOf("dependency:copy"));
    final Properties properties = new Properties();
    properties.setProperty(
      "artifact",
      // groupId:artifactId:version[:packaging[:classifier]]
      // see https://maven.apache.org/plugins/maven-dependency-plugin/copy-mojo.html
      format(
        "{}:{}:{}:{}:{}",
        artifact.groupId(), artifact.artifactId(), artifact.version(), artifact.type(), artifact.classifier()
      )
    );
    properties.setProperty("outputDirectory", dir.toString());
    properties.setProperty("interactiveMode", Boolean.toString(false));
    copyToTmpDir.setProperties(properties);
    final AtomicReference<String> fileName = new AtomicReference<>("");
    final String prefix = "[INFO] Copying ";
    execute(copyToTmpDir, line->{
      if(line.startsWith(prefix)){
        verify(fileName.compareAndSet("", split(removePrefix(line, prefix),' ').first()));
      }
    });
    verify(fileName.get(), f->!f.isBlank());
    final Path result = dir.resolve(fileName.get());
    verify(result, Files::isRegularFile);
    return result;
  }

  @Override
  public void close() {}


  private void execute(final InvocationRequest request){
    execute(request, s->{});
  }

  private void execute(final InvocationRequest request, final Consumer<String> outListener){
    request.setBatchMode(true);
    final Invoker invoker = new DefaultInvoker();
    final AtomicBoolean producedErrorLines = new AtomicBoolean();
    //invoker.setMavenHome(new File("/usr/share/maven")); TODO
    invoker.setMavenHome(new File("mavenHome"));
    invoker.setErrorHandler(errorLine->{
      producedErrorLines.set(true);
      LOG.error("Maven Error: {}", errorLine);
    });
    invoker.setOutputHandler(line->{
      LOG.debug("Maven out: {}", line);
      outListener.accept(line);
    });
    final InvocationResult result = call(()->invoker.execute(request));
    Opt.ofNullable(result.getExecutionException()).ifPresentDo(ex->{throw wrap(ex);});
    verifyEqual(result.getExitCode(), 0);
    verifyEqual(producedErrorLines.get(), false);
  }

  /**
   * The created fake pom has a single dependency to <code>artifact</code>.
   */
  private Path createTmpDirectoryWithFakePom(final ArtifactRef artifact){
    final Path dir = call(()->Files.createTempDirectory(M2tImp.class.getSimpleName()).toRealPath());

    final Path pom = dir.resolve("pom.xml");
    final String classifier = artifact.classifier().isEmpty()
      ? ""
      : format("\n      <classifier>{}</classifier>", artifact.classifier())
    ;
    FileUtils.writeText(
      format(
        POM_TEMPLATE,
        artifact.groupId(), artifact.artifactId(), artifact.version(), artifact.type(), classifier
      ),
      pom
    );
    LOG.debug("Created fake pom at {}.", pom);
    return dir;
  }

  private Path createTmpDirectoryFromPom(final Bytes pom) {
    final Path dir = call(()->Files.createTempDirectory(M2tImp.class.getSimpleName()).toRealPath());

    final Path pomCopy = dir.resolve("pom.xml");
    pom.write(pomCopy);
    LOG.debug("Created copy of pom at {}.", pomCopy);
    return dir;
  }

}
