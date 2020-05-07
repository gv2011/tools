package com.github.gv2011.gittools;

import static com.github.gv2011.util.Verify.notNull;
import static com.github.gv2011.util.ex.Exceptions.call;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;

import com.github.gv2011.util.FileUtils;
import com.github.gv2011.util.StringUtils;
import com.github.gv2011.util.icol.Opt;

/**
 * Eclipse doesn't support Git includeIf, copies settings from include files to
 * project configuration files.
 *
 */
public class UpdateGitProjectConfigFromInclude {

	private static final Logger LOG = getLogger(UpdateGitProjectConfigFromInclude.class);

	private static final String INCLUDE_IF = "includeIf";

	public static void main(final String[] args) throws IOException {
		final Config userConfig = readConfig(Paths.get(System.getProperty("user.home"),".gitconfig"));
		final boolean runDry = false;
		for(final String subsection: userConfig.getSubsections(INCLUDE_IF)){
			doSubDir(
				Paths.get(StringUtils.removePrefix(subsection, "gitdir:")),
				Paths.get(notNull(userConfig.getString(INCLUDE_IF, subsection, "path"))),
				runDry
			);
		}
	}

	private static void doSubDir(final Path directory, final Path includeFile, final boolean runDry) throws IOException {
		final Config include = readConfig(includeFile);
		LOG.debug("Using identity file {} with content:\n<<<<<<<<\n{}>>>>>>>>", includeFile, include.toText());
		Files.list(directory)
			.filter(f->Files.isDirectory(f))
			.map(d->d.resolve(Paths.get(".git", "config")))
			.filter(Files::exists)
			.forEach(
				f->update(f, include, runDry)
			)
		;
	}

	private static void update(final Path config, final Config include, final boolean runDry){
		final Config c = readConfig(config);
		boolean modified = false;
		for(final String section: include.getSections()){
			for(final String name: include.getNames(section)){
				final String value = notNull(include.getString(section, null, name));
				final Opt<String> old = Opt.ofNullable(c.getString(section, null, name));
				if(!old.equals(Opt.of(value))){
					modified = true;
					c.setString(section, null, name, value);
				}
			}
		}
		if(modified){
			final String text = c.toText();
			if(!runDry)FileUtils.writeText(text, config);
			LOG.info("{} {} to:\n<<<<<<<<\n{}>>>>>>>>", runDry?"Would update":"Updated", config, text);
		}
		else{
			LOG.info("Config file {} needs no update.", config);
		}
	}

	private static Config readConfig(final Path config) {
		final Config c = new Config();
		call(()->c.fromText(FileUtils.readText(config)));
		return c;
	}

}
