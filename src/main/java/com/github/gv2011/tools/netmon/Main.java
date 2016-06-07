package com.github.gv2011.tools.netmon;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;

public class Main {

  private static float flow = 100; //bytes per second
  
  public static void main(String[] args) throws MalformedURLException, IOException {
    Instant start = Instant.now();
    long total = 0;
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
        Instant rstart = Instant.now();
        URLConnection cn = new URL("http://letero.eu/").openConnection();
        cn.setConnectTimeout(5000);
        cn.setReadTimeout(2000);
        byte[] buffer = new byte[8192];
        int count = 0;
        try(InputStream is = cn.getInputStream()){
          long requestCount = 0;
          while(count!=-1){
            count = is.read(buffer);
            requestCount+=count;
          }
          Instant after = Instant.now();
          Duration duration = Duration.between(rstart, after);
          if(requestCount!=1894)throw new IOException();
          total+=requestCount;
          System.out.print(after+" ");
          System.out.println((float)requestCount/(float)duration.toMillis()+" kB/s");
        }
      }catch(Exception e){System.out.println(Instant.now()+" "+e);}
    }
  }

  private static boolean limitSpeed(Instant start, long total, float flow) {
    Instant now = Instant.now();
    long target = (long)(((float)Duration.between(start, now).toMillis()) / 1000f * flow);
    return total>target;
  }

}
