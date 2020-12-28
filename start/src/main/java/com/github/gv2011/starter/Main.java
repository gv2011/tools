package com.github.gv2011.starter;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.bytes.Bytes;
import com.github.gv2011.util.icol.ISet;
import com.github.gv2011.util.m2t.M2t;
import com.github.gv2011.util.m2t.M2tFactory;
import com.github.gv2011.util.m2t.Scope;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		String module = "com.github.gv2011.exjar";
		String mainClass = "com.github.gv2011.exjar.Main";
    final Path projectDir = Paths.get("/data/src/gv2011/executable-jar-example");
    Bytes pom = FileUtils.read(projectDir.resolve("pom.xml"));
    
    final M2t m2t = M2tFactory.INSTANCE.get().create();
    ISet<Path> classpath = m2t.getClasspath(pom, Scope.RUNTIME);
    classpath = classpath.addElement(Paths.get("target", "classes"));
    classpath.forEach(System.out::println);
    
    String modulePath = classpath.stream().map(Path::toString).sorted().collect(joining(File.pathSeparator));
		
		ProcessBuilder pb = new ProcessBuilder();
		pb.directory(projectDir.toFile());
		pb.command(
	    "java", 
	    //"--show-module-resolution", 
	    //"-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000", 
	    "-p", modulePath, 
	    "-m", module+"/"+mainClass
    );
		pb.inheritIO();
		final Process process = pb.start();
		try{
		  process.pid();
			process.waitFor();
		}
		finally{
			if(process.isAlive()){
				process.destroyForcibly().waitFor();
			}
			final int exitValue = process.waitFor();
			if(exitValue!=0) System.exit(exitValue);
		}
	}

}
