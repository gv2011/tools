package com.github.gv2011.tools.serial;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class SerialURLConnection extends URLConnection{

  protected SerialURLConnection(final URL url) {
    super(url);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void connect() throws IOException {
  }

}
