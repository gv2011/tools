package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.CollectionUtils.pair;
import static com.github.gv2011.util.FileUtils.*;
import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.ex.Exceptions.run;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import com.github.gv2011.util.AutoCloseableNt;
import com.github.gv2011.util.Pair;

public class OsmAnalyzer implements AutoCloseableNt{

  private static final QName V = QName.valueOf("v");
  private static final QName K = QName.valueOf("k");
  private static final QName ID = QName.valueOf("id");

  public static void main(final String[] args) throws Exception {
    try(OsmAnalyzer osmAnalyzer = new OsmAnalyzer()){
      osmAnalyzer.analyze(
  //        getPath("/work/germany-latest.osm.bz2")
          path("/work/berlin-latest.osm.bz2")
        .toUri().toURL()
      );
    }
  }

  private final Counter<String> eNames = new Counter<>("element");
  private final Database db = new Database("jdbc:h2:file:C:/work/osm/db/h2");
//  private Counter<String> attNames;
//  private Counter<String> tags;
//  private Counter<Set<String>> tagGroups;
  private final Path data = path("osm");

  private int count;
  private final int limit = Integer.MAX_VALUE-1000;



  @Override
  public void close() {
    db.close();
  }

  public void analyze(final URL url){
    run(()->{
      try(InputStream is =
        call(()->new BZip2CompressorInputStream(url.openStream()))
      ){
        final XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(is);
        while(eventReader.peek()!=null && count<limit){
          handleEvent(eventReader);
        }
      }
    });
  }

  private void handleEvent(final XMLEventReader eventReader){
    if(call(()->eventReader.peek()).isStartElement()) {
      handleElement(eventReader, Optional.empty());
    }
    else next(eventReader);
  }

  private void handleElement(final XMLEventReader eventReader, final Optional<Element> parent) {
    final StartElement element = call(()->((StartElement)eventReader.peek()));
    final String name = element.getName().getLocalPart();
    eNames.register(name);
    final Element e;
    if(name.equals("tag")) e = handleTag(parent.get(), element);
    else e = handleOther(parent, element, name);
    next(eventReader);
    while(!call(()->eventReader.peek()).isEndElement() && count<limit){
      if(call(()->eventReader.peek()).isStartElement()){
        handleElement(eventReader, Optional.of(e));
      }else next(eventReader);
    }
    next(eventReader);
  }

  private void next(final XMLEventReader eventReader) {
    run(()->eventReader.nextEvent());
    System.out.println(count++);
  }

  private Element handleOther(final Optional<Element> parent, final StartElement element, final String name) {
    final Optional<Long> id = Optional.ofNullable(element.getAttributeByName(ID))
      .map(Attribute::getValue)
      .map(Long::parseLong)
    ;
    final Element e = new Element(parent, name, id);
    final Iterator<?> it = element.getAttributes();
    while(it.hasNext()){
      final Attribute a = (Attribute) it.next();
      handleAttribute(e,a);
    }
    return e;
  }

  private Tag handleTag(final Element parent, final StartElement tag) {
    final String name = tag.getAttributeByName(K).getValue();
    final String value = tag.getAttributeByName(V).getValue();
    final Tag result = new Tag(parent, name, value);
    db.addTag(result);
    return result;
  }

  private void handleAttribute(final Element e, final Attribute a) {
    if(e.eName().equals("tag"))
    System.out.println(a.getName().getLocalPart());
  }


//      final StartElement element = (StartElement)sr2;
//      final String eName = element.getName().getLocalPart();
//      eNames.register(eName);
//      if(eName.equals("node")) readNode(sr);
//      if(eName.equals("tag")){
//        for(final Attribute a: new Iterable<Attribute>(){
//          @SuppressWarnings("unchecked")
//          @Override
//          public Iterator<Attribute> iterator() {
//            return element.getAttributes();
//        }}){
//          final String attName = a.getName().getLocalPart();
//          attNames.register(attName);
//          if(attName.equals("k")) {
//            final String tag = a.getValue();
////                tags.register(tag);
//          }
////              else if(attName.equals("v")) values.register(a.getValue());
//        }
//      }
//    }elseprivate void handleElement(XMLEventReader eventReader) {
//    // TODO Auto-generated method stub
//    throw notYetImplementedException();
//  }
//    {
////          System.out.println(XmlEventType.eventType(event));
//    }
//  }

  @SuppressWarnings("unused")//TODO
  private void readNode(final XMLEventReader sr) throws XMLStreamException, IOException {
    final StartElement node = ((StartElement)sr.peek());
    final long id = Long.parseLong(node.getAttributeByName(ID).getValue());
    final SortedSet<String> tagGroup = new TreeSet<>();
    final Map<String,String> atts = new TreeMap<>();
    while(!sr.nextEvent().isEndElement()){
      if(sr.peek().isStartElement()) {
        final Pair<String, String> tag = readTag(node, sr);
//        tags.register(tag);
        tagGroup.add(tag.getKey());
        atts.put(tag.getKey(), tag.getValue());
      }
    }
//   System.out.println(tagGroup.size());
   if(
     tagGroup.contains("addr:street") &&
     tagGroup.contains("addr:housenumber") &&
     tagGroup.contains("addr:postcode") &&
     tagGroup.contains("addr:city") &&
     tagGroup.contains("addr:country")
   ){
     try{registerAddress(id, atts);}
     catch(final Exception ex){ex.printStackTrace();}
//     tagGroups.register(tagGroup);
   }
  }

  private void registerAddress(final long id, final Map<String, String> atts) throws IOException {
    final String country = atts.get("addr:country");
    if(country.equals("DE")){
      final String city = atts.get("addr:city");
      final String postcode = atts.get("addr:postcode");
      final String street = atts.get("addr:street");
      final String housenumber = atts.get("addr:housenumber");
      final Path streetFile = data
        .resolve(encode(country))
        .resolve(encode(city))
        .resolve(encode(postcode))
        .resolve(encode(street)+".txt");
      Files.createDirectories(streetFile.getParent());
      Files.write(
        streetFile,
        (housenumber+" "+id+"\n").getBytes(UTF_8),
        StandardOpenOption.APPEND, StandardOpenOption.CREATE
      );
    }
  }

  private String encode(final String str) throws UnsupportedEncodingException {
//    return URLEncoder.encode(str, UTF_8.name());
    return str.replace('/', '+');
  }

  private static Pair<String,String> readTag(final StartElement node, final XMLEventReader sr) throws XMLStreamException {
    final StartElement tag = (StartElement)sr.peek();
    final String key = tag.getAttributeByName(K).getValue();
    final String value = tag.getAttributeByName(V).getValue();
    while(!sr.nextEvent().isEndElement()){
      verify(!sr.peek().isStartElement());
    }
    return pair(key,value);
  }

}
