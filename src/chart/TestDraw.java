/**
 * OOP2 assignments 3 and 4
 * Created by V.Batytskyy on 15.04.14
 */

package chart;

import parser.CSVProcessor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Test client for DiagramDrawer class
 */
public class TestDraw {
    private final JFrame frame;
    private final DiagramDrawer diagramDrawer;

    // header labels
    private final ArrayList<String> header = new ArrayList<>();

    // axes labels
    private final ArrayList<String> axes = new ArrayList<>();

    // chart/table data
    private final ArrayList<ArrayList<Number>> data = new ArrayList<>();

    /**
     * Initializes main parameters
     */
    TestDraw() {
        frame = new JFrame("Radar chart");

        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        loadData("csv/test.csv");
        final JTable table = new JTable(new CSVTableModel());

        final JScrollPane tableSP = new JScrollPane(table);
        tableSP.setPreferredSize(new Dimension(800, 100));

        // table occupies maximum amounts of space
        table.setFillsViewportHeight(true);

        diagramDrawer = new DiagramDrawer(header, axes, data);

        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.add(diagramDrawer, BorderLayout.CENTER);
        panel.add(tableSP, BorderLayout.PAGE_END);

        // add listener for the right click pop-up menu
        frame.addMouseListener(new FramePopupListener());

        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread
        SwingUtilities.invokeLater(TestDraw::new);
    }

    /**
     * Loads and parses data from csv file
     * @param fileName csv file name
     */
    private void loadData(String fileName) {
        CSVProcessor csvProcessor = new CSVProcessor();
        try {
            File file = new File(fileName);
            csvProcessor.load(file);
            csvProcessor.parse();

            ArrayList<ArrayList<String>> strData = csvProcessor.getCsvFields();
            if (strData.size() < 2 || strData.get(0).size() < 2) {
                throw new Exception("Not enough fields in csv. Can't continue");
            }

            for (int i = 1; i < strData.get(0).size(); i++) {
                header.add(strData.get(0).get(i));
            }

            for (int i = 1; i < strData.size(); i++) {
                data.add(new ArrayList<>(strData.get(i).size()));
                for (int j = 1; j < strData.get(i).size(); j++) {
                    data.get(i-1).add(NumberFormat.getInstance().parse(strData.get(i).get(j)));
                }
            }

            for (int i = 1; i < strData.size(); i++) {
                axes.add(strData.get(i).get(0));
            }

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Table model for table representation
     */
    private class CSVTableModel extends AbstractTableModel {

        public CSVTableModel() {
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) return "";
            else return header.get(col - 1);
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return data.get(0).size() + 1;       // +1 column for parameters
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            if (columnIndex == 0) return axes.get(rowIndex);
            else return data.get(rowIndex).get(columnIndex - 1);

        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            return true;
        }

        @Override
        public void setValueAt(final Object strValue, final int rowIndex, final int columnIndex) {
            try {
                String str = (String)strValue;
                if (columnIndex == 0) {
                    axes.set(rowIndex, str);
                } else {
                    if (str.isEmpty()) {
                        data.get(rowIndex).set(columnIndex - 1, 0);
                    } else {
                        final Number number = NumberFormat.getInstance().parse(str);
                        data.get(rowIndex).set(columnIndex - 1, number);
                    }
                }
                frame.repaint();
            } catch (Exception e) {
                e.printStackTrace();
                showMsg("Uncorrect value. This field must contain a number",
                                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Shows a message in the separate message dialog
     * @param msg message to show
     * @param messageCode shows the type of the message
     */
    private void showMsg(String msg, int messageCode) {
        if (messageCode == JOptionPane.WARNING_MESSAGE) {
            JOptionPane.showMessageDialog(frame, msg, "Warning", messageCode);
        } else if (messageCode == JOptionPane.ERROR_MESSAGE) {
            JOptionPane.showMessageDialog(frame, msg, "Error", messageCode);
        } else {
            JOptionPane.showMessageDialog(frame, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Right click pop-up menu
     */
    private class MyPopUp extends JPopupMenu {
        final JMenuItem anItem;
        public MyPopUp() {
            anItem = new JMenuItem("Save the chart");
            add(anItem);
            anItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent ev) {
                    try {
                        diagramDrawer.saveTo(new File("image.png"), "png");
                        showMsg("Chart saved", JOptionPane.INFORMATION_MESSAGE);
                    } catch (NullPointerException e) {
                        showMsg("Can't save image. " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
                    } catch (IOException e) {
                        showMsg("Can't save image. " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Listener for mouse to trigger pop-up menu
     */
    private class FramePopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) doPop(e);
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) doPop(e);
        }

        private void doPop(MouseEvent e) {
            MyPopUp menu = new MyPopUp();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
