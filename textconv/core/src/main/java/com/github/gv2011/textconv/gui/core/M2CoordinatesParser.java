package com.github.gv2011.textconv.gui.core;

import static com.github.gv2011.util.ex.Exceptions.call;
import static com.github.gv2011.util.icol.ICollections.iCollections;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.gv2011.util.icol.IList.Builder;
import com.github.gv2011.util.m2t.ArtifactId;
import com.github.gv2011.util.m2t.GroupId;
import com.github.gv2011.util.m2t.Version;
import com.github.gv2011.util.xml.DomUtils;

public class M2CoordinatesParser {

  public String parse(final String depsList){
    return call(()->parseInternal(depsList));
  }

  private String parseInternal(final String depsList) throws SAXException, IOException, ParserConfigurationException{
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(false);
    dbf.setValidating(false);

    final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    final Element doc = documentBuilder.parse(new InputSource(new StringReader("<doc>"+depsList+"</doc>"))).getDocumentElement();
    final Builder<String> deps = iCollections().listBuilder();
    collectDeps(deps, doc);
    return deps.build().stream().collect(joining("\n"));
  }

  private void collectDeps(final Builder<String> deps, final Node node) {
    if(isDependency(node)) deps.add(format((Element) node));
    final NodeList childNodes = node.getChildNodes();
    for(int i=0; i<childNodes.getLength(); i++){
      collectDeps(deps, childNodes.item(i));
    }
  }

  private String format(final Element element) {
    return
      getChild(element, GroupId.M2_NAME).map(n->n.getTextContent()).orElse("") + ":" +
      getChild(element, ArtifactId.M2_NAME).map(n->n.getTextContent()).orElse("") + ":" +
      getChild(element, Version.M2_NAME).map(n->n.getTextContent()).orElse("")
    ;
  }

  private Optional<Node> getChild(final Element element, final String m2Name) {
    return DomUtils.stream(element.getChildNodes())
      .filter(n->n.getNodeType()==Node.ELEMENT_NODE)
      .filter(n->n.getNodeName().equals(m2Name))
      .findFirst()
    ;
  }

  private boolean isDependency(final Node node) {
    return node.getNodeType()!=Node.ELEMENT_NODE ? false : hasArtifact((Element) node);
  }

  private boolean hasArtifact(final Element element) {
    return DomUtils.stream(element.getChildNodes())
      .filter(n->n.getNodeType()==Node.ELEMENT_NODE)
      .anyMatch(n->n.getNodeName().equals(ArtifactId.M2_NAME))
    ;
  }

}
