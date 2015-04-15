
package thehambone.gtatools.gtasum.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author thehambone
 */
public class OutputPanel extends JPanel {
    private static final PrintStream STDOUT = new PrintStream(new FileOutputStream(FileDescriptor.out));
    private static final PrintStream STDERR = new PrintStream(new FileOutputStream(FileDescriptor.err));
    private final DocumentOutputStream documentOutputStreamOut;
    private final DocumentOutputStream documentOutputStreamErr;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private JPopupMenu popupMenu;
    private JMenuItem clearOutputMenuItem;
    public OutputPanel() {
        initComponents();
        initPopupMenu();
        documentOutputStreamOut = new DocumentOutputStream(textArea);
        documentOutputStreamErr = new DocumentOutputStream(textArea);
        documentOutputStreamOut.teeOutput(STDOUT);
        documentOutputStreamErr.teeOutput(STDERR);
        System.setOut(new PrintStream(documentOutputStreamOut));
        System.setErr(new PrintStream(documentOutputStreamErr));
    }
    private void initComponents() {
        setLayout(new BorderLayout());
        this.textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        textArea.setComponentPopupMenu(popupMenu);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON3 && evt.getClickCount() == 1 && !textArea.getText().isEmpty()) {
                    popupMenu.show(textArea, evt.getX(), evt.getY());
                }
            }
        });
        scrollPane = new JScrollPane(textArea);
        add(scrollPane);
    }
    private void initPopupMenu() {
        popupMenu = new JPopupMenu();       
        clearOutputMenuItem = new JMenuItem("Clear Output");  
        clearOutputMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        });
        popupMenu.add(clearOutputMenuItem);
    }
    
    private class DocumentOutputStream extends OutputStream {
        private final JTextArea textArea;
        private final Document doc;
        private PrintStream tee;
        public DocumentOutputStream(JTextArea textArea) {
            this.textArea = textArea;
            this.doc = textArea.getDocument();
        }
        public void teeOutput(PrintStream tee) {
            this.tee = tee;
        }
        @Override
        public void write(int b) throws IOException {
            try {
                doc.insertString(doc.getLength(), String.valueOf(b), null);
                textArea.setCaretPosition(doc.getLength());
                if (tee != null) {
                    tee.write(b);
                }
            } catch (BadLocationException ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                System.err.printf("[ERROR]: %s\n", sw.toString());
                JOptionPane.showMessageDialog(null, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                doc.insertString(doc.getLength(), new String(b, off, len), null);
                textArea.setCaretPosition(doc.getLength());
                if (tee != null) {
                    tee.write(b, off, len);
                }
            } catch (BadLocationException ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                System.err.printf("[ERROR]: %s\n", sw.toString());
                JOptionPane.showMessageDialog(null, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }
    }
}