package com.github.gv2011.tools.rest;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.util.Optional;

import org.slf4j.Logger;

import com.github.gv2011.util.PropertyUtils;
import com.github.gv2011.util.PropertyUtils.SafeProperties;
import com.github.gv2011.util.http.HttpUtils;
import com.github.gv2011.util.json.JsonNode;

public class ReadRestResource {

  private static final Logger LOG = getLogger(ReadRestResource.class);

  public static void main(final String[] args) {
    final SafeProperties config = PropertyUtils.readProperties("config.properties");
    JsonNode node = HttpUtils.read(URI.create(config.getProperty("url")));
    final Optional<String> filter = config.tryGet("filter");
    if(filter.isPresent()) {
      node = node.filter(filter.get());
    }
    LOG.info(node.serialize());
  }

}
