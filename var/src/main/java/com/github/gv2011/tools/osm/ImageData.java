package com.github.gv2011.tools.osm;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.util.List;

public class ImageData {

  public static void main(final String[] args) throws Exception {
    System.out.println(Integer.MAX_VALUE);

  }

  private final BufferedImage image;
  private long count;
  final short xResolution;
  private final Bounds bounds;
  final short yResolution;
  private volatile float brightness = 1000f;
//  private final byte[] saved;

  public ImageData(final Bounds bounds){
    this(bounds, (short) 2000);
  }

  public ImageData(final Bounds bounds, final short xResolution){
    this.bounds = bounds;
    this.xResolution = xResolution;
    yResolution = (short) (xResolution/bounds.aspectRatio());
    image = new BufferedImage(xResolution, yResolution, BufferedImage.TYPE_USHORT_GRAY);
//    saved = new byte[xResolution*yResolution];
  }

  private synchronized boolean addNode(final double[] node) {
    boolean added;
    final double[] norm = normalize(node);
    final short x = (short) (norm[0]*xResolution);
    final short y = (short) (norm[1]*yResolution);

    if(x>=0 && x<xResolution && y>=0 && y<yResolution){
      added = true;
//      if(mergeSaved(x,y)){
        final int[] pixel = new int[1];
        final WritableRaster raster = image.getRaster();
        raster.getPixel(x, y, pixel);
        pixel[0]++;
        raster.setPixel(x, y, pixel);
//      }
    }
    else added = false;
    if(added) count++;
    return added;
  }

  public double[] normalize(final double[] node) {
    final double rlon = node[0]-bounds.minLon();
    final double rlat = bounds.maxLat()-node[1];
    final double xNorm = rlon / (bounds.maxLon()-bounds.minLon());
    final double yNorm = rlat / (bounds.maxLat()-bounds.minLat());
    return new double[]{xNorm, yNorm};
  }

//  private boolean mergeSaved(final short x, final short y) {
//    final int index = y*xResolution+x;
//    final byte s = saved[index];
//    if(s==factor-1){
//      saved[index] = 0;
//      return true;
//    }
//    else{
//      saved[index]++;
//      return false;
//    }
//  }

//  private void darker() {
//    factor = factor*2;
//    System.out.println(factor);
//    final WritableRaster raster = image.getRaster();
//    final int[] pixel = new int[1];
//    for(int x=0; x<xResolution; x++){
//      for(int y=0; y<yResolution; y++){
//        raster.getPixel(x, y, pixel);
//        pixel[0] = pixel[0]/2;
//        raster.setPixel(x, y, pixel);
//      }
//    }
//  }

  public synchronized BufferedImage getImage(){
    final float scale = xResolution * yResolution * brightness/ (count+1);
    final RescaleOp op = new RescaleOp(scale,0,null);
    final BufferedImage result = op.createCompatibleDestImage(image, image.getColorModel());
    op.filter(image, result);
    return result;
  }

  public synchronized long size(){
    return count;
  }

  public synchronized void addNodes(final List<double[]> nodes) {
    for(final double[] n: nodes){
      addNode(n);
    }
  }

  public void setBrightNess(final float brightness) {
    this.brightness = brightness;
  }


}
