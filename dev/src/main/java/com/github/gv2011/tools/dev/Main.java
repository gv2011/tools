package com.github.gv2011.tools.dev;


import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.base.CaseFormat;

public class Main extends JFrame
                           implements DocumentListener {

    private JTextField entry;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JLabel status;
    private JTextArea textArea;

    final static Color  HILIT_COLOR = Color.LIGHT_GRAY;
    final static Color  ERROR_COLOR = Color.PINK;
    final static String CANCEL_ACTION = "cancel-search";

    final Color entryBg;


    public Main() {
        initComponents();

        entryBg = entry.getBackground();
        entry.getDocument().addDocumentListener(this);

        final InputMap im = entry.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        final ActionMap am = entry.getActionMap();
        im.put(KeyStroke.getKeyStroke("ESCAPE"), CANCEL_ACTION);
        am.put(CANCEL_ACTION, new CancelAction());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */

    private void initComponents() {
        entry = new JTextField();
        textArea = new JTextArea();
        status = new JLabel();
        jLabel1 = new JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Devtools");

        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        jScrollPane1 = new JScrollPane(textArea);

        jLabel1.setText("");

        final GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

    //Create a parallel group for the horizontal axis
    final ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

    //Create a sequential and a parallel groups
    final SequentialGroup h1 = layout.createSequentialGroup();
    final ParallelGroup h2 = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);

    //Add a container gap to the sequential group h1
    h1.addContainerGap();

    //Add a scroll pane and a label to the parallel group h2
    h2.addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE);
    h2.addComponent(status, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE);

    //Create a sequential group h3
    final SequentialGroup h3 = layout.createSequentialGroup();
    h3.addComponent(jLabel1);
    h3.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
    h3.addComponent(entry, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE);

    //Add the group h3 to the group h2
    h2.addGroup(h3);
    //Add the group h2 to the group h1
    h1.addGroup(h2);

    h1.addContainerGap();

    //Add the group h1 to the hGroup
    hGroup.addGroup(GroupLayout.Alignment.TRAILING, h1);
    //Create the horizontal group
    layout.setHorizontalGroup(hGroup);


    //Create a parallel group for the vertical axis
    final ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
    //Create a sequential group v1
    final SequentialGroup v1 = layout.createSequentialGroup();
    //Add a container gap to the sequential group v1
    v1.addContainerGap();
    //Create a parallel group v2
    final ParallelGroup v2 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
    v2.addComponent(jLabel1);
    v2.addComponent(entry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
    //Add the group v2 tp the group v1
    v1.addGroup(v2);
    v1.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
    v1.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE);
    v1.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
    v1.addComponent(status);
    v1.addContainerGap();

    //Add the group v1 to the group vGroup
    vGroup.addGroup(v1);
    //Create the vertical group
    layout.setVerticalGroup(vGroup);
    pack();
    }

    public void update() {
        final String s = entry.getText();
        textArea.setText(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, s));
    }

    void message(final String msg) {
        status.setText(msg);
    }

    // DocumentListener methods

    @Override
    public void insertUpdate(final DocumentEvent ev) {
        update();
    }

    @Override
    public void removeUpdate(final DocumentEvent ev) {
        update();
    }

    @Override
    public void changedUpdate(final DocumentEvent ev) {
    }

    class CancelAction extends AbstractAction {
        @Override
        public void actionPerformed(final ActionEvent ev) {
            entry.setText("");
            entry.setBackground(entryBg);
        }
    }


    public static void main(final String args[]) {
    SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                  new Main().setVisible(true);
            }
        });
    }


}