package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.ex.Exceptions.format;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import com.github.gv2011.util.StringUtils;

public class OsmReader {

  public static void main(final String[] args) throws Exception {
    int latestDone = 2119129;
    while(true){
      final boolean success = load(latestDone+1);
      if(!success) {
        System.out.println("Waiting");
        Thread.sleep(20000);
      }
      else{
        latestDone++;
      }
    }
  }

  private static boolean load(final int sequenceNo) throws Exception {
    boolean result;
    final String withLeadingZeros = StringUtils.alignRight(Integer.toString(sequenceNo), 9, '0');
    final String part1 = withLeadingZeros.substring(0, 3);
    final String part2 = withLeadingZeros.substring(3, 6);
    final String part3 = withLeadingZeros.substring(6, 9);
    final URL url = new URL(format(
      "http://planet.openstreetmap.org/replication/minute/{}/{}/{}.osc.gz", part1, part2, part3
    ));
    try(InputStream in = url.openStream()){
      final GZIPInputStream gin = new GZIPInputStream(in);
      final XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(gin);
      while(sr.hasNext()){
        sr.next();
        System.out.println(sr.isStartElement() ? sr.getName() : sr.getEventType());
      }
      result = true;
    }catch(final IOException ex){
      result = false;
    }
    return result;
  }


}
