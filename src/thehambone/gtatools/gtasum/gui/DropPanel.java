
package thehambone.gtatools.gtasum.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import thehambone.gtatools.gtasum.checksum.Checksum;

/**
 *
 * @author thehambone
 */
public class DropPanel extends JPanel {
    private JLabel dropMessage;
    private DropTarget dropTarget;
    private DropTargetHandler dropTargetHandler;
    
    public DropPanel() {
        initComponents();
    }
    private void initComponents() {
        setLayout(new BorderLayout());
        dropMessage = new JLabel("Drop your GTA gamesaves here!");
        dropMessage.setFont(dropMessage.getFont().deriveFont(Font.PLAIN, 18));
        dropMessage.setHorizontalAlignment(SwingConstants.CENTER);
        add(dropMessage, BorderLayout.CENTER);
    }
    public void importFiles(ArrayList<File> fileList) {
        for (Iterator<File> i = fileList.iterator(); i.hasNext();) {
            File f = i.next();
            String fileName = f.getName();
            boolean skipFile = false;
            if (f.isDirectory()) {
                i.remove();
                System.out.printf("[INFO] Skipping directory - %s\n", fileName);
                showIsDirectoryError(fileName);
                continue;
            }
            if (fileName.contains(".")) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (!extension.equalsIgnoreCase("b")) {
                    skipFile = showFileTypeWarning(fileName);
                }
            } else {
                skipFile = showFileTypeWarning(fileName);
            }
            if (skipFile) {
                i.remove();
                System.out.printf("[INFO]: Skipping file - %s\n", fileName);
            }
        }
        int filesUpdated = 0;
        for (File f : fileList) {
            try {
                filesUpdated += Checksum.calculateChecksum(f) ? 1 : 0;
            } catch (IOException ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                System.err.printf("[ERROR]: %s\n", sw.toString());
                JOptionPane.showMessageDialog(this, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        Toolkit.getDefaultToolkit().beep();
        String filesUpdatedString = String.format("%d %s updated.", filesUpdated, filesUpdated == 1 ? "file" : "files");
        System.out.printf("[INFO]: %s\n", filesUpdatedString);
        JOptionPane.showMessageDialog(this, filesUpdatedString, "Complete", JOptionPane.INFORMATION_MESSAGE);
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(375, 225);
    }
    @Override
    public void addNotify() {
        super.addNotify();
        try {
            getMyDropTarget().addDropTargetListener(getDropTargetHandler());
        } catch (TooManyListenersException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            System.err.printf("[ERROR]: %s\n", sw.toString());
            JOptionPane.showMessageDialog(this, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    public void removeNotify() {
        super.removeNotify();
        getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
    }
    private DropTarget getMyDropTarget() {
        if (dropTarget == null) {
            dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null);
        }
        return dropTarget;
    }
    private DropTargetHandler getDropTargetHandler() {
        if (dropTargetHandler == null) {
            dropTargetHandler = new DropTargetHandler();
        }
        return dropTargetHandler;
    }
    /**
     * Displays a warning stating that the specified file may not be a valid GTA save file.
     * @param fileName
     * @return boolean reflecting whether or not the user chooses to skip this particular file
     */
    private boolean showFileTypeWarning(String fileName) {
        int option = JOptionPane.showOptionDialog(this, "<html><p style='width: 200px;'>\"" + fileName + "\" may not be a GTA III-era save file.<br>"
                + "<br>"
                + "Continue?</p></html>", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
        return option != JOptionPane.YES_OPTION;
    }
    private void showIsDirectoryError(String fileName) {
        JOptionPane.showMessageDialog(this, "<html><p style='width: 200px;'>\"" + fileName + "\" is a directory.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private class DropTargetHandler implements DropTargetListener {
        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            processDrag(dtde);
        }
        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            processDrag(dtde);
        }
        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // Do nothing
        }
        @Override
        public void dragExit(DropTargetEvent dte) {
            // Do nothing
        }
        @Override
        @SuppressWarnings("unchecked")
        public void drop(DropTargetDropEvent dtde) {
            Transferable transferable = dtde.getTransferable();
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrop(dtde.getDropAction());
                try {
                    List<File> transferData = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (transferData != null && !transferData.isEmpty()) {
                        if (!Window.getCurrentWindow().hasOutputPanelBeenVisible()) {
                            Window.getCurrentWindow().setOutputPanelVisible(true);
                        }
                        importFiles(new ArrayList<>(transferData));
                        dtde.dropComplete(true);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    System.err.printf("[ERROR]: %s\n", sw.toString());
                    JOptionPane.showMessageDialog(DropPanel.this, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                dtde.rejectDrop();
            }
        }
        private void processDrag(DropTargetDragEvent dtde){
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else {
                dtde.rejectDrag();
            }
        }
    }
}