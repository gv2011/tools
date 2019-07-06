package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import com.github.gv2011.util.AutoCloseableNt;

public class SpeedTest implements AutoCloseableNt{

  public static void main(final String[] args) throws Exception {
    try(SpeedTest osmAnalyzer = new SpeedTest()){
      osmAnalyzer.analyze(
  //        getPath("/work/germany-latest.osm.bz2")
        ()->call(()->
        new BZip2CompressorInputStream(
          Paths.get(
            "/work/germany-latest.osm.bz2"
//              "E:/europe-161001.osm.bz2"
//              "/work/berlin-latest.osm"
//              "O:/osm/planet-160926.osm.bz2"
          ).toUri().toURL()
//          new URL("http://download.geofabrik.de/europe-161001.osm.bz2")
          .openStream()
          )
//        http://download.geofabrik.de/europe-161001.osm.bz2
//        new BZip2CompressorInputStream(getPath("/work/berlin-latest.osm.bz2").toUri().toURL().openStream())
//        getPath("/work/berlin-latest.osm").toUri().toURL().openStream()
        )
      );
    }
  }

  private final Map<String,Set<String>> attNames = new TreeMap<>();
  private final Map<String,Set<String>> childNames = new TreeMap<>();
  private final Map<String,AtomicLong> tagNames = new TreeMap<>();
  private final Map<String,AtomicLong> elementCounts = new TreeMap<>();
  private Display display;

  public void analyze(final Supplier<InputStream> in){
    call(()->{
      final Instant start = Instant.now();
      final Instant limit = start.plus(Duration.ofDays(1));
      final AtomicLong counter = new AtomicLong();
      try(SpeedTestStream is = new SpeedTestStream(in.get(), limit)){
        try{
//          doNothing(is);
          parseXml3(is, counter);
        }
        catch(final Exception ex){
          ex.printStackTrace();
        }
        final Instant finish = Instant.now();
        System.out.println(format("{} bytes read.",is.count()));
        System.out.println(format("{} tags read.", counter.get()));

        final float percent =  is.count() / 1100983095F * 100F;
        System.out.println(format("{} percent.",percent));
        final float time = seconds(Duration.between(start, finish));
        System.out.println(format("Duration: {} seconds.", time));
        System.out.println(format("Estimation: {} seconds.", time/percent*100f));
        for(final Entry<String, Set<String>> e:attNames.entrySet()){
          System.out.println(format("Attributes for {}: {}.", e.getKey(), e.getValue()));
        }
        for(final Entry<String, Set<String>> e:childNames.entrySet()){
          System.out.println(format("Children for {}: {}.", e.getKey(), e.getValue()));
        }
        System.out.println("Elements:");
        for(final Entry<String, AtomicLong> e: elementCounts.entrySet()){
          System.out.println(format("  {}: {}", e.getKey(), e.getValue()));
        }
//        System.out.println("Tags:");
//        for(final Entry<String, AtomicLong> e: tagNames.entrySet()){
//          System.out.println(format("  {}: {}", e.getKey(), e.getValue()));
//        }
        System.out.println(format("Points: {}.", display.noPoints()));
      }

    });
  }

  private void parseXml3(final InputStream is, final AtomicLong counter) throws Exception {
    //    1100983095 bytes read.
    //    15520482 tags read.
    //    100.0 percent.
    //    Duration: 50.902 seconds.
    //    Estimation: 50.902004 seconds.
    //    Attributes for bounds: [maxlat, maxlon, minlat, minlon].
    //    Attributes for member: [ref, role, type].
    //    Attributes for nd: [ref].
    //    Attributes for node: [changeset, id, lat, lon, timestamp, uid, user, version].
    //    Attributes for osm: [generator, timestamp, version].
    //    Attributes for relation: [changeset, id, timestamp, uid, user, version].
    //    Attributes for tag: [k, v].
    //    Attributes for way: [changeset, id, timestamp, uid, user, version].
    //    Children for bounds: [].
    //    Children for member: [].
    //    Children for nd: [].
    //    Children for node: [tag].
    //    Children for osm: [bounds, node, relation, way].
    //    Children for relation: [member, tag].
    //    Children for tag: [].
    //    Children for way: [nd, tag].
    final XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
    while(streamReader.hasNext()){
      if(streamReader.next()==XMLStreamConstants.START_ELEMENT){
        handleElement(streamReader, counter, "");
      }
    }
  }

  private String handleElement(
    final XMLStreamReader streamReader, final AtomicLong counter, final String parentName
  ) throws XMLStreamException {
    counter.incrementAndGet();
    final String name = streamReader.getLocalName();
    elementCounts.computeIfAbsent(name, n->new AtomicLong()).incrementAndGet();
    final Set<String> atts = attNames.computeIfAbsent(name, n->new TreeSet<>());
    final Set<String> children = childNames.computeIfAbsent(name, n->new TreeSet<>());
    final int attCount = streamReader.getAttributeCount();
    final Map<String,String> attValues = new HashMap<>();
    for(int i=0; i<attCount ; i++){
      final String attName = streamReader.getAttributeLocalName(i);
      atts.add(attName);
      attValues.put(attName, streamReader.getAttributeValue(i));
      streamReader.getAttributeValue(i);
    }
    if(name.equals("tag")) {
      tagNames.computeIfAbsent(attValues.get("k"), k->new AtomicLong()).incrementAndGet();
    }else if(name.equals("node")){
      setNode(attValues);
    }else if(name.equals("bounds")){
      setBounds(attValues);
    }
    while(streamReader.next()!=XMLStreamConstants.END_ELEMENT){
      if(streamReader.getEventType()==XMLStreamConstants.START_ELEMENT){
        final String childName = handleElement(streamReader, counter, name);
        children.add(childName);
      }
    }
    return name;
  }

  private void setBounds(final Map<String, String> attValues) {
    verify(display==null);
    display = new Display(new ImageData(new Bounds(
      Double.parseDouble(attValues.get("minlon")),
      Double.parseDouble(attValues.get("maxlon")),
      Double.parseDouble(attValues.get("minlat")),
      Double.parseDouble(attValues.get("maxlat"))
    )));
    display.display();
  }

  private void setNode(final Map<String, String> attValues) {
    verify(display!=null);
    display.node(
      Double.parseDouble(attValues.get("lon")),
      Double.parseDouble(attValues.get("lat"))
    );
  }

  private float seconds(final Duration duration) {
    return duration.toMillis()/1000F;
  }

  @Override
  public void close() {}

  @SuppressWarnings("unused")//TODO
  private void doNothing(final InputStream is) throws IOException {
    final byte[] buffer = new byte[256];
    int count = 0;
    while(count!=-1) {
      count = is.read(buffer);
    }
  }

  @SuppressWarnings("unused")//TODO
  private void parseXml(final InputStream is) throws Exception {
    final XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(is);
    eventReader.nextTag();
    while(eventReader.hasNext()){
      eventReader.nextTag();
    }
  }

  @SuppressWarnings("unused")//TODO
  private void parseXml2(final InputStream is, final AtomicLong counter) throws Exception {
    //    1100983095 bytes read.
    //    15520482 tags read.
    //    100.0 percent.
    //    Duration: 63.273 seconds.
    final XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(is);
    final XMLEvent tag = eventReader.nextTag();
    verify(tag.isStartElement());
    final StartElement startElement = (StartElement)tag;
    verify(startElement.getName().getLocalPart().equals("osm"));
    handleElement(startElement, eventReader, counter);
    while(eventReader.hasNext()){
      verify(!eventReader.nextEvent().isStartElement());
    }
  }

  private void handleElement(
      final StartElement startElement, final XMLEventReader eventReader, final AtomicLong counter
    ) throws XMLStreamException {
      counter.incrementAndGet();
      XMLEvent nextTag = eventReader.nextTag();
      while(nextTag.isStartElement()){
        handleElement((StartElement) nextTag, eventReader, counter);
        nextTag = eventReader.nextTag();
      }
      verify(nextTag.isEndElement());
    }


}
