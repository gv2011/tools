package com.github.gv2011.tools.mailfilter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class ExtractEmailsFromAddressBook {

  public static void main(final String[] args) throws IOException {
    final PrintWriter w = new PrintWriter(new FileWriter("out.txt"));
    for(final String email: readEmails("Newsletter.csv"))
//    for(final String email: readEmails("Persönliches Adressbuch.csv"))
      w.print(email.trim()+"\n");
    w.close();
  }

  public static Set<String> readEmails(final String name) throws IOException {
    final Set<String> result = new TreeSet<>();
    final Reader in = new FileReader("C:\\work\\"+name);
    final CSVFormat format = CSVFormat.DEFAULT.withFirstRecordAsHeader();
    final Iterable<CSVRecord> records = format.parse(in);
    for(final CSVRecord r: records){
      final String email = r.get("Primäre E-Mail-Adresse");
      if(email==null?false:!email.trim().isEmpty()){
        result.add(email.trim());
      }
    }
    return result;
  }

}
