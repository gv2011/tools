package com.github.gv2011.tools.osm;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class SpeedTestStream extends FilterInputStream{

  private final AtomicLong counter = new AtomicLong();
  private final Instant limit;

  protected SpeedTestStream(final InputStream in, final Instant limit) {
    super(in);
    this.limit = limit;
  }

  public long count(){
    return counter.get();
  }

  @Override
  public int read() throws IOException {
    if(Instant.now().isAfter(limit)) throw new InterruptedIOException();
    final int result = in.read();
    if(result!=-1) counter.incrementAndGet();
    return result;
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    if(Instant.now().isAfter(limit)) throw new InterruptedIOException();
    final int count = in.read(b, off, len);
    if(count!=-1) counter.addAndGet(count);
    return count;
  }

}
