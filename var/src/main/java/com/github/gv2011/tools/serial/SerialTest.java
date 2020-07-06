package com.github.gv2011.tools.serial;

import static com.github.gv2011.util.Verify.verify;

import java.io.IOException;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import com.github.gv2011.util.XStream;

public class SerialTest {


  public static void main(final String[] args) throws SerialPortInvalidPortException, IOException, InterruptedException {
    XStream.ofArray(SerialPort.getCommPorts())
    .map(SerialPort::getSystemPortName)
    .forEach(System.out::println);
    final SerialPort com0 = SerialPort.getCommPort("COM5");
    verify(com0.openPort());
    com0.addDataListener(new Listener());
    final SerialPort com1 = SerialPort.getCommPort("COM4");
    verify(com1.openPort());
    final OutputStream out = com1.getOutputStream();
    verify(out!=null);
    out.write("Hallo\n".getBytes());
    out.flush();
    Thread.sleep(3000L);
  }

  private static class Listener implements SerialPortDataListener {

    @Override
    public int getListeningEvents() {
      return SerialPort.LISTENING_EVENT_DATA_AVAILABLE+SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(final SerialPortEvent event) {
      System.out.println(new String(event.getReceivedData()));
    }

  }

}
