package com.github.gv2011.tools.netmon;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;

public class Main {

  private static float flow = 200f * 1E6f / (31*24*60*60); //bytes per second

  public static void main(final String[] args) throws MalformedURLException, IOException {
    final Instant start = Instant.now();
    long total = 0;
    Instant rstart = Instant.now();
    while(!Thread.interrupted()){
      try{
        boolean limitSpeed = limitSpeed(start, total, flow);
        if(limitSpeed){
          System.out.print('.');
          while(limitSpeed) {
            Thread.sleep(1000);
            System.out.print('.');
            limitSpeed = limitSpeed(start, total, flow);
          }
          System.out.println();
        }
        final Instant last = rstart;
        rstart = Instant.now();
        while(rstart.isBefore(last.plus(Duration.ofMillis(100)))){
          Thread.sleep(Duration.ofMillis(100).minus(Duration.between(last, rstart)).toMillis());
          rstart = Instant.now();
        }
        final URLConnection cn = new URL(args[0]).openConnection();
        cn.setConnectTimeout(5000);
        cn.setReadTimeout(2000);
        final byte[] buffer = new byte[8192];
        int count = 0;
        try(InputStream is = cn.getInputStream()){
          long requestCount = 0;
          while(count!=-1){
            count = is.read(buffer);
            requestCount+=count;
          }
          final Instant after = Instant.now();
          final Duration duration = Duration.between(rstart, after);
          if(requestCount!=1894)throw new IOException();
          total+=requestCount;
          System.out.print(after+" ");
          System.out.println((float)requestCount/(float)duration.toMillis()+" kB/s");
        }
      }catch(final Exception e){System.out.println(Instant.now()+" "+e);}
    }
  }

  private static boolean limitSpeed(final Instant start, final long total, final float flow) {
    final Instant now = Instant.now();
    final long target = (long)((Duration.between(start, now).toMillis()) / 1000f * flow);
    return total>target;
  }

}
