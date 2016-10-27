package com.github.gv2011.tools.osm;

import static com.github.gv2011.util.Verify.verify;
import static com.github.gv2011.util.ex.Exceptions.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import com.github.gv2011.util.Pair;

public class Display {


  public static void main(final String[] args){
    new Display(new ImageData(Bounds.ALL)).display();
  }

  private static final int GAP = 5;
  private final NumberFormat numberFormat;

  private final ImageData points;
//  private final List<Pair<Float,Float>> newPoints = new ArrayList<>();
  private final AtomicLong noPoints = new AtomicLong();


  private Component drawingBoard;
  private final List<double[]> newPoints = new ArrayList<>();

  public Display(final ImageData points) {
    numberFormat = NumberFormat.getIntegerInstance();
    this.points = points;
  }

  public void display () {
    EventQueue.invokeLater(()->{
      final JFrame frame = new JFrame(getClass().getSimpleName());
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.setContentPane(createContentPane());
      frame.setJMenuBar(createMenuBar());
      frame.pack();
      frame.setLocationByPlatform(true);
      frame.setVisible(true);
    });
  }

  private JMenuBar createMenuBar() {
    final JMenuBar menuBar = new JMenuBar();
    final JMenu menu = new JMenu("File");
    final JMenuItem save = new JMenuItem("Save image");
    save.addActionListener(this::save);
    menu.add(save);
    menuBar.add(menu);
    return menuBar;
  }

  private Container createContentPane() {
    final JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout(GAP, GAP));
    drawingBoard = new DrawingBoard();
    contentPane.add(drawingBoard, BorderLayout.CENTER);
    final JSlider brightness = new JSlider(JSlider.VERTICAL,100, 700, 300);
    brightness.addChangeListener(this::adjustBrightness);
    contentPane.add(brightness, BorderLayout.EAST);
    return contentPane;
  }



  private void adjustBrightness(final ChangeEvent e) {
    final float log = ((JSlider)e.getSource()).getValue();
    points.setBrightNess((float) Math.pow(10, log/100f));
    EventQueue.invokeLater(()->{drawingBoard.repaint();});
  }

  private void save(final ActionEvent e) {
    run(()->ImageIO.write(points.getImage(), "png", new File("osm.png")));
  }

  public void node(final double lon, final double lat) {
    synchronized(newPoints) {newPoints.add(new double[]{lon,lat});}
    EventQueue.invokeLater(()->{drawingBoard.repaint();});
  }

  public long noPoints() {
    EventQueue.invokeLater(()->{drawingBoard.repaint();});
    return points.size();
  }

  private class DrawingBoard extends JPanel {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;

    private DrawingBoard () {
      setOpaque(true);
    }

    @Override
    public Dimension getPreferredSize () {
      return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(final Graphics g) {
      super.paintComponent(g);
      ArrayList<double[]> newPointsCopy;
      synchronized(newPoints){
        newPointsCopy = new ArrayList<>(newPoints);
        newPoints.clear();
      }
      points.addNodes(newPointsCopy);
      g.drawImage(points.getImage(),
        0, 0, getWidth(), getHeight(),
        0, 0, points.xResolution, points.yResolution,
        null
      );
      drawNewPointsRed(g, getWidth(), getHeight(), newPointsCopy);
      g.setColor(Color.WHITE);
      g.drawString(numberFormat.format(points.size())+" nodes", 15, getHeight()-15);
    }

    private void drawNewPointsRed(final Graphics g, final int w, final int h, final ArrayList<double[]> newPointsCopy) {
      g.setColor(Color.RED);
      for(final double[] point: newPointsCopy){
        final double[] norm = points.normalize(point);
        final int x = (int) (norm[0]*w);
        final int y = (int) (norm[1]*h);
        if(x>=0 && y>=0 && x<w-4 && y<h-4){
          g.fillRect(x, y, 2, 2);
        }
      }
    }

}

  }