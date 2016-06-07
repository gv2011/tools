package com.github.gv2011.tools.mailfilter;

import static com.github.gv2011.util.FileUtils.getPath;
import static com.github.gv2011.util.FileUtils.readOptionalText;
import static com.github.gv2011.util.FileUtils.removeExtension;
import static com.github.gv2011.util.FileUtils.writeText;
import static com.github.gv2011.util.StringUtils.tryRemoveTail;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

public class UpdateContacts {

  public static void main(final String[] args) throws IOException {
    final Sardine sardine = SardineFactory.begin();
    sardine.setCredentials("ph", "Griemen17siek");
    final String server = "https://vz1996.blaucloud.de";
    final String baseUrl = server+"/remote.php/carddav/addressbooks/ph/kontakte/";
    final List<DavResource> resources = sardine.list(baseUrl);

    final Set<String> names = new HashSet<>();

    for (final DavResource res : resources)
    {
      try{
        final String name = tryRemoveTail(res.getName(), ".vcf");
        names.add(name);
        final Path path = getPath("contacts", name+".vcf");
        final Path etagPath = getPath("contacts", name+".etag");
        final Optional<String> etag = readOptionalText(etagPath);
        final boolean copy = etag.isPresent() ? !etag.get().equals(res.getEtag()) : true;
        if(copy){
          System.out.println("Get "+name);
          try(InputStream is = sardine.get(baseUrl+res.getName())){
            Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
            writeText(etagPath, res.getEtag());
          }
        }else System.out.println(name+" exists.");
      }catch(final Exception e){e.printStackTrace();}
    }

    Files.list(getPath("contacts"))
      .filter((p)-> !names.contains(removeExtension(p.getFileName())))
      .forEach((p)->{
        System.out.println("Deleting "+p);
        run(()->Files.deleteIfExists(p));
        })
    ;
  }

}
