package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.FileUtils.getPath;
import static com.github.gv2011.util.StringUtils.alignRight;
import static com.github.gv2011.util.ex.Exceptions.notYetImplementedException;

import java.nio.file.Path;

import com.github.gv2011.util.FileUtils;

public class NodeIndex {

  public void put(final Node node){
//    final Path path = getPath(node);

  }

  Path getPath(final Path base, final Node node) {
    //lat="52.5265215" lon="13.4477657"
    final double lon = node.longitude()+180d;
    final double lat = node.latitude()+90d;

    final int[] pLon = new int[5];
    final int[] pLat = new int[5];

    pLon[0] = (int)lon /60;
    pLat[0] = (int)lat /60;

    double rLon = lon-(pLon[0]*60);
    double rLat = lat-(pLat[0]*60);

    Path result = base.resolve(pLon[0]+"-"+pLat[0]);
    for(int i=1; i<5; i++){
      pLon[i] = (int)rLon /10;
      pLat[i] = (int)rLat /10;
      rLon = 10d*(rLon-(pLon[i]*10));
      rLat = 10d*(rLat-(pLat[i]*10));
      result = result.resolve(pLon[i]+"-"+pLat[i]);
    }

    result = result.resolve(node.id()+".node.html");
    return result;
  }



}
