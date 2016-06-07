package com.github.gv2011.tools.mailfilter;

import static com.github.gv2011.util.FileUtils.getPath;

import java.io.IOException;
import java.nio.file.Files;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.github.gv2011.util.FileUtils;

import ezvcard.Ezvcard;
import ezvcard.property.Email;

public class EmailsFromContacts {

  public static void main(final String[] args) throws IOException {
    System.out.println(new EmailsFromContacts().getEmails().stream().collect(Collectors.joining("\n")));
  }

  public SortedSet<String> getEmails() throws IOException {
    return Files.list(getPath("contacts"))
    .filter(p->p.getFileName().toString().endsWith(".vcf"))
    .map(FileUtils::readText)
    .map(t->Ezvcard.parse(t).first())
    .flatMap(vc->vc.getEmails().stream())
    .map(Email::getValue)
    .collect(Collectors.toCollection(()->new TreeSet<>()));
  }

}
