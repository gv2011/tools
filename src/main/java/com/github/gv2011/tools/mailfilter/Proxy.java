package com.github.gv2011.tools.mailfilter;

import static com.github.gv2011.util.StringUtils.showSpecial;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import com.github.gv2011.util.bytes.ByteUtils;
import com.github.gv2011.util.bytes.CloseableBytes;

public class Proxy {

  public static void main(final String[] args) throws IOException {
    final ServerSocket ss = new ServerSocket(80);
    while(true){
      final Socket s = ss.accept();
      final InputStream in = s.getInputStream();
      final StringBuilder request = new StringBuilder();
      boolean eof=false;
      while(!eof && !request.toString().endsWith("\r\n\r\n")){
        final int read = in.read();
        if(read==-1) eof = true;
        else request.append((char)read);
      }
//      final CloseableBytes request = ByteUtils.fromStream(s.getInputStream());
      System.out.println(showSpecial(request.toString()));
      if(!request.toString().startsWith("GET /webmail/?_task=mail HTTP/1.1\r\n")){
        s.close();
      }else{
        final Socket s2 = SSLSocketFactory.getDefault().createSocket("mail.special-host.de", 443);
        final OutputStream os = s2.getOutputStream();
        os.write((
          "GET /webmail/?_task=mail HTTP/1.1\r\n" +
          "Host: mail.special-host.de\r\n" +
          "User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0\r\n" +
          "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n" +
          "Accept-Language: de,en-US;q=0.7,en;q=0.3\r\n" +
          "\r\n"
        ).getBytes(UTF_8));
  //      request.write(os);
        os.flush();
        final CloseableBytes response = ByteUtils.fromStream(s2.getInputStream());
        System.out.println(response.utf8ToString());
        s2.close();

        final OutputStream os1 = s.getOutputStream();
        response.write(os1);
        os1.close();
      }
    }
  }

}
