/*
 *
 *  Dump/Restore client - Copyright (C) 2013 National Board of e-Health (NSI)
 *
 *  All source code and information supplied as part of <Komponent-Navn> is
 *  copyright to National Board of e-Health.
 *
 *  The source code has been released under a dual license - meaning you can
 *  use either licensed version of the library with your code.
 *
 *  It is released under the Common Public License 1.0, a copy of which can
 *  be found at the link below.
 *  http://www.opensource.org/licenses/cpl1.0.php
 *
 *  It is released under the LGPL (GNU Lesser General Public License), either
 *  version 2.1 of the License, or (at your option) any later version. A copy
 *  of which can be found at the link below.
 *  http://www.gnu.org/copyleft/lesser.html
 *
 *  $HeadURL: https://svn.softwareborsen.dk/dump-restore-client/tags/release-1.0.0/dump-restore-client/src/main/java/dk/nsi/dump_restore_client/Presentation.java $
 *  $Id: Presentation.java 10927 2013-03-22 09:56:13Z ChristianGasser $
 * /
 */

package dk.nsi.dump_restore_client;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

/**
 * @author $LastChangedBy: ChristianGasser $ $LastChangedDate: 2013-03-22 10:56:13 +0100 (Fri, 22 Mar 2013) $
 * @version $Revision: 10927 $
 */
public class Presentation {

    private static final String UTF_8 = "UTF-8";

    private static Logger log = Logger.getLogger(Presentation.class);

    private final Controller controller;
    private final StaticConfig staticConfig;
    private final CprTableModel cprTableModel;

    private DumpRestoreDomain sourceDomain;
    private DumpRestoreDomain targetDomain;

    private JLabel numberLabel;
    private JTextArea loggingArea;

    public Presentation(Controller controller, StaticConfig staticConfig) {
        this.controller = controller;
        this.staticConfig = staticConfig;
        cprTableModel = new CprTableModel();
    }

    private interface Callback {
        void directoryDomainSelected(File directory);

        void environmentDomainSelected(String environment);

        void nullDomainSelected();
    }

    private class CprTableModel extends AbstractTableModel {
        private String[] columnNames = {"Kilde-cpr",
                "Mål-cpr"};
        private Object[][] data = {
                // ten empty, editable rows
                {"", ""},
                {"", ""},
                {"", ""},
                {"", ""},
                {"", ""},
                {"", ""},
                {"", ""},
                {"", ""},
                {"", ""},
                {"", ""},
        };

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        public void setData(Object[][] data) {
            this.data = data;
            fireTableDataChanged();
        }

        public Object[][] getData() {
            return data;
        }

    }

    private class JTextFieldOutputStream extends OutputStream {
        private JTextArea text;
        private ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        public JTextFieldOutputStream(JTextArea text) {
            this.text = text;
        }

        public void write(int b) {
            buffer.write(b);
        }

        public void flush() {
            try {
                text.append(buffer.toString(UTF_8));
                buffer.reset();
            } catch (UnsupportedEncodingException e) {
                log.error(e);
            }
        }
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Dump/Restore Client");
        frame.setPreferredSize(new Dimension(600, 550));

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel);

        JPanel cprListPanel = createCPRListPanel();
        mainPanel.add(cprListPanel, BorderLayout.CENTER);

        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);

        Container loggingTextArea = createLoggingTextArea();
        mainPanel.add(loggingTextArea, BorderLayout.SOUTH);

        centerOnScreen(frame);

        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel sourceLabel = new JLabel();
        JLabel targetLabel = new JLabel();

        JPanel sourcePanel = createSourcePanel(sourceLabel);
        rightPanel.add(sourcePanel);

        JPanel targetPanel = createTargetPanel(targetLabel);
        rightPanel.add(targetPanel);

        JPanel executionPanel = createExecutionPanel(sourceLabel, targetLabel);
        rightPanel.add(executionPanel);

        return rightPanel;
    }

    private JPanel createSourcePanel(final JLabel sourceLabel) {
        return createDomainPanel("Kilde", "Kilden", new Callback() {
            public void directoryDomainSelected(File directory) {
                sourceDomain = new DirectoryDumpRestore(directory);
                sourceLabel.setText(directory.getAbsolutePath());
            }

            public void environmentDomainSelected(String environment) {
                sourceDomain = new SoapDumpRestore(staticConfig, environment);
                sourceLabel.setText(environment);
            }

            public void nullDomainSelected() {
                sourceDomain = null;
                sourceLabel.setText("");
            }
        });
    }

    private JPanel createTargetPanel(final JLabel targetLabel) {
        return createDomainPanel("Mål", "Målet", new Callback() {
            public void directoryDomainSelected(File directory) {
                targetDomain = new DirectoryDumpRestore(directory);
                targetLabel.setText(directory.getAbsolutePath());
            }

            public void environmentDomainSelected(String environment) {
                targetDomain = new SoapDumpRestore(staticConfig, environment);
                targetLabel.setText(environment);
            }

            public void nullDomainSelected() {
                targetDomain = null;
                targetLabel.setText("");
            }
        });
    }

    private JPanel createDomainPanel(String title, String domain, final Callback callback) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        final JRadioButton environmentRadioButton = new JRadioButton(domain + " er et testmiljø");
        environmentRadioButton.setEnabled(false);
        environmentRadioButton.setSelected(false);
        radioPanel.add(environmentRadioButton);
        final JRadioButton directoryRadioButton = new JRadioButton(domain + " er en lokal folder");
        radioPanel.add(directoryRadioButton);
        directoryRadioButton.setEnabled(false);
        directoryRadioButton.setSelected(false);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(environmentRadioButton);
        buttonGroup.add(directoryRadioButton);

        JPanel chooserPanel = new JPanel();
        chooserPanel.setLayout(new BoxLayout(chooserPanel, BoxLayout.Y_AXIS));
        Vector<String> environments = new Vector<String>();
        environments.add("");
        environments.addAll(staticConfig.getEnvironments());
        final JComboBox environmentBox = new JComboBox(environments);
        environmentBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String selectedItem = (String) environmentBox.getSelectedItem();
                environmentRadioButton.setSelected(true);
                if (!selectedItem.isEmpty()) {
                    callback.environmentDomainSelected(selectedItem);
                } else {
                    callback.nullDomainSelected();
                }
            }
        });
        environmentBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        chooserPanel.add(environmentBox);
        final JButton directoryButton = new JButton("Vælg folder");
        directoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnValue = fileChooser.showOpenDialog(directoryButton);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File directory = fileChooser.getSelectedFile();
                    environmentBox.setSelectedIndex(0);
                    directoryRadioButton.setSelected(true);
                    callback.directoryDomainSelected(directory);
                }
            }
        });
        directoryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        chooserPanel.add(directoryButton);

        panel.add(chooserPanel);
        panel.add(radioPanel);

        return panel;
    }

    private JPanel createExecutionPanel(JLabel sourceLabel, final JLabel targetLabel) {
        JPanel executionPanel = new JPanel();
        executionPanel.setBorder(BorderFactory.createTitledBorder("Udførelse"));
        executionPanel.setLayout(new BoxLayout(executionPanel, BoxLayout.Y_AXIS));

        JPanel sourcePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        sourcePanel.add(new JLabel("Kilde:"));
        sourcePanel.add(sourceLabel);
        executionPanel.add(sourcePanel);

        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        targetPanel.add(new JLabel("Mål:"));
        targetPanel.add(targetLabel);
        executionPanel.add(targetPanel);

        JPanel numberOfCPRsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        numberOfCPRsPanel.add(new JLabel("Antal cpr-numre:"));
        numberLabel = new JLabel();
        numberOfCPRsPanel.add(numberLabel);
        executionPanel.add(numberOfCPRsPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        final JButton executeButton = new JButton("Udfør!");
        executeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                // In order to terminate editing of cpr table, see table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); below
                executeButton.requestFocus();
                loggingArea.setText("");
                if (sourceDomain == null || targetDomain == null) {
                    log.error("Kilde og/eller mål mangler at blive valgt!");
                } else {
                    // Start a new thread in order not to block the EvenDispatcher thread ....
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                List<CprPair> cprMapping = CprPair.listFromDoubleArray(cprTableModel.getData());
                                numberLabel.setText(String.valueOf(cprMapping.size()));
                                controller.invokeDumpRestore(sourceDomain, targetDomain, cprMapping);
                                log.info("====================");
                            } catch (Throwable t) {
                                ExceptionHandler.handleThrowable(t);
                            }
                        }
                    }).start();
                }
            }
        });
        buttonPanel.add(executeButton);
        executionPanel.add(buttonPanel);

        return executionPanel;
    }

    private JPanel createCPRListPanel() {
        JPanel cprListPanel = new JPanel(new BorderLayout());
        cprListPanel.setBorder(BorderFactory.createTitledBorder("CPR-liste"));

        JTable table = new JTable(cprTableModel);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setShowGrid(true);
        table.setGridColor(Color.gray);
        table.setShowHorizontalLines(true);
        JScrollPane scrollPane = new JScrollPane(table);
        cprListPanel.add(scrollPane, BorderLayout.CENTER);

        final JButton readFileButton = new JButton("Indlæs tekstfil");
        readFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnValue = fileChooser.showOpenDialog(readFileButton);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    loggingArea.setText("");
                    File file = fileChooser.getSelectedFile();
                    List<CprPair> cprPairs = CprPair.listFromFile(file);
                    if (cprPairs != null && !cprPairs.isEmpty()) {
                        cprTableModel.setData(CprPair.toDoubleArray(cprPairs));
                        numberLabel.setText(String.valueOf(cprPairs.size()));
                    }
                    log.info("Færdig med at indlæse tekstfil. Indlæste " + cprPairs.size() + " CPR par.");
                }
            }
        });
        cprListPanel.add(readFileButton, BorderLayout.SOUTH);

        return cprListPanel;
    }

    private Container createLoggingTextArea() {
        loggingArea = new JTextArea(10, 1);
        loggingArea.setEditable(false);
        JScrollPane pane = new JScrollPane(loggingArea);

        OutputStreamWriter out = new OutputStreamWriter(new JTextFieldOutputStream(loggingArea), Charset.forName(UTF_8));

        WriterAppender appender = new WriterAppender();
        appender.setEncoding(UTF_8);
        appender.setWriter(out);
        appender.setImmediateFlush(true);
        appender.setLayout(new PatternLayout("%d{ABSOLUTE} %-5p %x - %m%n"));
        Logger.getRootLogger().addAppender(appender);

        return pane;
    }

    private void centerOnScreen(JFrame frame) {
        frame.setLocationRelativeTo(null);
    }

}
