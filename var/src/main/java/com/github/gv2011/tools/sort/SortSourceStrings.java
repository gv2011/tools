package com.github.gv2011.tools.sort;

import static com.github.gv2011.util.icol.ICollections.sortedSetOf;
import static java.util.stream.Collectors.joining;

public class SortSourceStrings {

  public static void main(final String[] args) {
    System.out.println(sortedSetOf(
        "accounting",
        "autotls",
        "chomsky",
        "helloworld",
        "debplatform",
        "asn1",
        "util",
        "parent",
        "parent-gv",
        "parent-priv",
        "project-mgmt",
        "venonta",
        "topten",
        "quarry",
        "webdav-servlet",
        "tools",
        "hprops",
        "gsoncore",
        "jsoncore",
        "acme4j",
        "maven-plugin-example",
        "maven",
        "plexus-classworlds",
        "mailfilter",
        "jamwiki",
        "testmailserver",
        "jenkins",
        "multi-branch-project-plugin"
      )
      .stream()
      .map(s->"\""+s+"\"")
      .collect(joining(",\n"))
    );
  }

}
