package com.github.gv2011.tools.mailfilter;

import static com.github.gv2011.util.StringUtils.writeFile;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateFilter {

  public static void main(final String[] args) throws IOException{

    final Set<String> newsEmails = ExtractEmailsFromAddressBook.readEmails("Newsletter.csv");

    final String news = createRule("News", newsEmails);
    final String requiredTo = "@iglhaut.com";

    final Set<String> importantEmails = ExtractEmailsFromAddressBook.readEmails("Persönliches Adressbuch.csv");
    final String important = createRule("Wichtig", importantEmails);

    final String filterSet =
      "require [\"fileinto\"];\n" +
        news +
        "# rule:[lists]\n" +
        "if not header :contains \"to\" \"" + requiredTo + "\"\n" +
        "{\n" +
        "\tfileinto \"News\";\n" +
        "\tstop;\n" +
        "}\n" +
        important +
        "# rule:[Unbekannt]\n" +
        "if true\n" +
        "{\n" +
        "\tfileinto \"Unbekannt\";\n" +
        "\tstop;\n" +
        "}\n" +
        "\r\n";
    writeFile(filterSet, "filter.txt");
  }

  private static String createRule(final String folder, final Set<String> emails) {
    final String from = "header :contains \"from\" \"";
    final String delimiter = "\", "+from;
    final String prefix = "# rule:[" + folder + "]\n"
        + "if anyof (" + from;
    final String suffix = "\")\n" +
      "{\n" +
      "\tfileinto \"" + folder + "\";\n" +
      "\tstop;\n" +
      "}\n"
    ;

    return emails.stream().collect(Collectors.joining(delimiter, prefix, suffix));
  }
}
