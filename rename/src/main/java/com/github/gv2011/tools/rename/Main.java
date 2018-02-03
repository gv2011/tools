package com.github.gv2011.tools.rename;

import com.github.gv2011.tools.rename.gui.MainFrame;
import static com.github.gv2011.util.StringUtils.removePrefix;
import static com.github.gv2011.util.ex.Exceptions.call;

import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

public class Main {

  public static void main(final String[] args) {
    EventQueue.invokeLater(() -> {
      createController();
    });
  }

  private static Main createController() {
    final MainFrame gui = new MainFrame();
    final Main controller = new Main(gui);
    gui.dirName.setTransferHandler(controller.new Th());
    gui.dirName.addKeyListener(new KL(controller::update));
    gui.prefix.addKeyListener(new KL(controller::update));
    gui.newPrefix.addKeyListener(new KL(controller::update));
    gui.rename.setEnabled(false);
    gui.files.setModel(controller.tableModel);
    gui.rename.addActionListener(e -> controller.rename());
    gui.setVisible(true);
    return controller;
  }

  private final MainFrame gui;
  private final DefaultTableModel tableModel =
    new DefaultTableModel(new Object[]{"Current Name", "New Name"},0)
  ;

  public Main(final MainFrame gui) {
    this.gui = gui;
  }

  private void update(){
    final String fileTxt = gui.dirName.getText();
    System.out.println(fileTxt);
    final File file = new File(fileTxt);
    if(file.exists()){
      File dir;
      if(file.isDirectory()){
        dir = file;
      }else{
        dir = file.getParentFile();
        gui.prefix.setText(file.getName());
      }
      gui.dirName.setText(call(()->dir.getAbsoluteFile().getCanonicalPath()));
      final String prefix = gui.prefix.getText();
      final File[] files = dir.listFiles(f->
        f.isFile() && f.getName().startsWith(prefix)
      );
      tableModel.setRowCount(0);
      final String newPrefix = gui.newPrefix.getText();
      boolean renamePossible = files.length>0;
      for(final File f: files){
        final String name = f.getName();
        String newName = newPrefix+removePrefix(name, prefix);
        if(new File(dir,newName).exists()){
          renamePossible = false;
          newName = "exists: "+newName;
        }
        tableModel.addRow(new Object[]{
          name,
          newName
        });
      }
      gui.rename.setEnabled(renamePossible);
    }
    else gui.rename.setEnabled(false);
  }

  private void rename() {
    final File dir = new File(gui.dirName.getText());
    for(int r=0; r<tableModel.getRowCount(); r++){
      final String currentName = (String) tableModel.getValueAt(r, 0);
      final String newName = (String) tableModel.getValueAt(r, 1);
      final File f = new File(dir, currentName);
      f.renameTo(new File(dir, newName));
    }
    update();
  }



  private static class KL implements KeyListener {
    private final Runnable listener;
    public KL(final Runnable listener) {
      this.listener = listener;
    }
    @Override
    public void keyTyped(final KeyEvent e) {
    }
    @Override
    public void keyPressed(final KeyEvent e) {
    }
    @Override
    public void keyReleased(final KeyEvent e) {
      listener.run();
    }
  }

  private class Th extends TransferHandler {
    @Override
    public boolean canImport(final TransferSupport support) {
      return
        support.isDataFlavorSupported(DataFlavor.stringFlavor) ||
        support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
      ;
    }
    private Optional<Object> getFile(final TransferSupport support) {
      if(!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) return Optional.empty();
      else{
        List<?> files;
        try {
          final Transferable transferable = support.getTransferable();
          files = (List<?>)transferable.getTransferData(transferable.getTransferDataFlavors()[0]);
        } catch (final Exception e) {throw new RuntimeException(e);}
        return files.size()!=1 ? Optional.empty() : Optional.of(files.get(0));
      }
    }
    @Override
    public boolean importData(final TransferSupport support) {
      final Optional<Object> optFile = getFile(support);
      if(optFile.isPresent()){
        gui.dirName.setText(optFile.get().toString());
        update();
        return true;
      }
      else if(support.isDataFlavorSupported(DataFlavor.stringFlavor)){
        try {
          gui.dirName.setText(
            support.getTransferable().getTransferData(DataFlavor.stringFlavor).toString()
          );
        } catch (final Exception e)  {throw new RuntimeException(e);}
        update();
        return true;
      }
      else return false;
    }
  }

  @SuppressWarnings("unused")
  private static class Dtl implements DropTargetListener {
    private final Consumer<DropTargetDropEvent> listener;
    public Dtl(final Consumer<DropTargetDropEvent> listener) {
      this.listener = listener;
    }
    @Override
    public void dragEnter(final DropTargetDragEvent dtde) {
    }
    @Override
    public void dragOver(final DropTargetDragEvent dtde) {
    }
    @Override
    public void dropActionChanged(final DropTargetDragEvent dtde) {
    }
    @Override
    public void dragExit(final DropTargetEvent dte) {
    }
    @Override
    public void drop(final DropTargetDropEvent dtde) {
      listener.accept(dtde);
    }
  }


}
