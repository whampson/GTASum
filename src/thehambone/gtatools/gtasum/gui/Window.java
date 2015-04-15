
package thehambone.gtatools.gtasum.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import thehambone.gtatools.gtasum.Main;

/**
 *
 * @author thehambone
 */
public class Window extends JFrame {
    private static final String ABOUT_PAGE_TEXT = "GTASum allows you to quickly and easily "
            + "update the checksum on any GTA III-era save file, "
            + "which can be useful if you are editing gamesaves "
            + "without a proper gamesave editor.";
    private static Window currentWindow;
    private DropPanel dropPanel;
    private OutputPanel outputPanel;
    private JSplitPane splitPane;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu viewMenu;
    private JMenu windowMenu;
    private JMenu helpMenu;
    private JMenuItem fileOpenMenuItem;
    private JMenuItem fileExitMenuItem;
    private JCheckBoxMenuItem viewShowOutputCheckBoxMenuItem;
    private JCheckBoxMenuItem windowAlwaysOnTopCheckBoxMenuItem;
    private JMenuItem helpAboutMenuItem;
    private JDialog aboutDialog;
    private int defaultSplitPaneDividerSize;
    private boolean hasOutputPanelBeenVisible = false;
    
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            System.err.printf("[ERROR]: %s\n", sw.toString());
            JOptionPane.showMessageDialog(null, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        Window window = new Window(String.format("%s %s", Main.PROGRAM_TITLE, Main.PROGRAM_VERSION));
        System.out.printf("%s %s\n", Main.PROGRAM_TITLE, Main.PROGRAM_VERSION);
        System.out.printf("Created by %s\n", Main.PROGRAM_AUTHOR);
        Window.setCurrentWindow(window);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
    public static Window getCurrentWindow() {
        return currentWindow;
    }
    public static void setCurrentWindow(Window window) {
        currentWindow = window;
    }
    
    public Window(String title) {
        super(title);
        initComponents();
        initMenuBar();
        initAboutDialog();
    }
    private void initComponents() {
        setLayout(new BorderLayout());
        dropPanel = new DropPanel();
        outputPanel = new OutputPanel();
        outputPanel.setVisible(false);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dropPanel, outputPanel);
        splitPane.setResizeWeight(0.75);
        defaultSplitPaneDividerSize = splitPane.getDividerSize();
        setOutputPanelVisible(false);
        add(splitPane, BorderLayout.CENTER);
    }
    private void initMenuBar() {
        menuBar = new JMenuBar();
        
        fileMenu = new JMenu("File");
        viewMenu = new JMenu("View");
        windowMenu = new JMenu("Window");
        helpMenu = new JMenu("Help");
        
        fileOpenMenuItem = new JMenuItem("Open...");
        fileExitMenuItem = new JMenuItem("Exit");
        viewShowOutputCheckBoxMenuItem = new JCheckBoxMenuItem("Show Output");
        windowAlwaysOnTopCheckBoxMenuItem = new JCheckBoxMenuItem("Always On Top");
        helpAboutMenuItem = new JMenuItem("About");
        
        fileOpenMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("GTA III-era Save Files (*.b)", "b");
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setFileFilter(fileNameExtensionFilter);
                int option = fileChooser.showOpenDialog(getContentPane());
                if (option != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File[] fileList = fileChooser.getSelectedFiles();
                if (!hasOutputPanelBeenVisible) {
                    setOutputPanelVisible(true);
                }
                dropPanel.importFiles(new ArrayList<>(Arrays.asList(fileList)));
            }
        });
        fileExitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        viewShowOutputCheckBoxMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setOutputPanelVisible(viewShowOutputCheckBoxMenuItem.isSelected());
            }
        });
        windowAlwaysOnTopCheckBoxMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlwaysOnTop(windowAlwaysOnTopCheckBoxMenuItem.isSelected());
            }
        });
        helpAboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aboutDialog.setModal(true);
                aboutDialog.setLocationRelativeTo(dropPanel);
                aboutDialog.setVisible(true);
                aboutDialog.setModal(false);
            }
        });
        
        fileOpenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        
        fileMenu.add(fileOpenMenuItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(fileExitMenuItem);
        viewMenu.add(viewShowOutputCheckBoxMenuItem);
        windowMenu.add(windowAlwaysOnTopCheckBoxMenuItem);
        helpMenu.add(helpAboutMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(windowMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    private void initAboutDialog() {
        aboutDialog = new JDialog(this, String.format("About %s", Main.PROGRAM_TITLE));
        aboutDialog.setLayout(new BorderLayout());
        aboutDialog.getRootPane().registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aboutDialog.dispose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        JPanel topPanel = new JPanel();
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel(Main.PROGRAM_TITLE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 18));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        JLabel versionLabel = new JLabel(String.format("Version %s (%s)", Main.PROGRAM_VERSION, Main.PROGRAM_BUILD_DATE));
        versionLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel centerPanel = new JPanel();
        centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        centerPanel.setLayout(new BorderLayout());
        JLabel infoLabel = new JLabel();
        infoLabel.setText(String.format("<html><p style='width: 200px;'>%s</p></html>", ABOUT_PAGE_TEXT));
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.setLayout(new BorderLayout());
        JLabel authorLabel = new JLabel(String.format("<html>Author: <a href='%s'>%s</a></html>", Main.PROGRAM_AUTHOR_URL, Main.PROGRAM_AUTHOR));
        authorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        authorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                try {
                    openURI(new URI(Main.PROGRAM_AUTHOR_URL));
                } catch (URISyntaxException ex) {
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    System.err.printf("[ERROR]: %s\n", sw.toString());
                    JOptionPane.showMessageDialog(dropPanel, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JLabel contactLabel = new JLabel(String.format("<html>Bug reports go to <a href='mailto:%s'>%s</a></html>", Main.PROGRAM_AUTHOR_EMAIL, Main.PROGRAM_AUTHOR_EMAIL));
        contactLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        contactLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                try {
                    openURI(new URI(String.format("mailto:%s", Main.PROGRAM_AUTHOR_EMAIL)));
                } catch (URISyntaxException ex) {
                    StringWriter sw = new StringWriter();
                    ex.printStackTrace(new PrintWriter(sw));
                    System.err.printf("[ERROR]: %s\n", sw.toString());
                    JOptionPane.showMessageDialog(dropPanel, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(versionLabel, BorderLayout.CENTER);
        
        centerPanel.add(infoLabel, BorderLayout.CENTER);
        
        bottomPanel.add(authorLabel, BorderLayout.NORTH);
        bottomPanel.add(contactLabel, BorderLayout.SOUTH);
        
        aboutDialog.add(topPanel, BorderLayout.NORTH);
        aboutDialog.add(centerPanel, BorderLayout.CENTER);
        aboutDialog.add(bottomPanel, BorderLayout.SOUTH);
        aboutDialog.pack();
        
    }
    private void openURI(URI uRI) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uRI);
            } catch (IOException ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                System.err.printf("[ERROR]: %s\n", sw.toString());
                JOptionPane.showMessageDialog(dropPanel, "An error has occured:\n\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public void setOutputPanelVisible(boolean isVisible) {
        if (viewShowOutputCheckBoxMenuItem != null) {
            viewShowOutputCheckBoxMenuItem.setSelected(isVisible);
        }
        outputPanel.setVisible(isVisible);
        if (isVisible) {
            hasOutputPanelBeenVisible = true;
            splitPane.setDividerSize(defaultSplitPaneDividerSize);
        } else {
            splitPane.setDividerSize(0);
        }
        splitPane.setDividerLocation(0.75);
    }
    public boolean hasOutputPanelBeenVisible() {
        return hasOutputPanelBeenVisible;
    }
}