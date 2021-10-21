package com.github.gv2011.textconv.gui;

import static com.github.gv2011.util.ex.Exceptions.call;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.github.gv2011.util.AutoCloseableNt;

public class TextconvGui implements AutoCloseableNt{

  public static TextconvGui create(final Runnable closeListener, final Function<String,String> textFunction){
    return SwingUtils.callSwing(()->{
      final TextconvJFrame textconvJFrame = new TextconvJFrame();
      textconvJFrame.addWindowListener(new WindowAdapter(){
          @Override
          public void windowClosing(final WindowEvent e) {
            closeListener.run();
          }
        });
      textconvJFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      final TextconvGui gui = new TextconvGui(textconvJFrame, textFunction);
      textconvJFrame.setVisible(true);
      return gui;
    });
  }

  private final TextconvJFrame textconvJFrame;
  private final Function<String, String> textFunction;

  private TextconvGui(final TextconvJFrame textconvJFrame, final Function<String, String> textFunction){
    this.textconvJFrame = textconvJFrame;
    this.textFunction = textFunction;
    textconvJFrame.textOut().setEnabled(true);
    textconvJFrame.textOut().setEditable(false);
    textconvJFrame.textIn().getDocument().addDocumentListener(new DocumentListener(){
      @Override
      public void insertUpdate(final DocumentEvent e)  {inChanged();}
      @Override
      public void removeUpdate(final DocumentEvent e)  {inChanged();}
      @Override
      public void changedUpdate(final DocumentEvent e) {inChanged();}
    });
  }

  private void inChanged(){
    call(()->{
      final Document inDoc = textconvJFrame.textIn().getDocument();
      final String txt = inDoc.getText(0, inDoc.getLength());
      final Document doc = textconvJFrame.textOut().getDocument();
      doc.remove(0, doc.getLength());
      doc.insertString(0, textFunction.apply(txt), null);
    });
  }

  @Override
  public void close() {
    textconvJFrame.setVisible(false);
    textconvJFrame.dispose();
  }

}
