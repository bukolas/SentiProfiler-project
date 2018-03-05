/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 22/01/2001
 *
 *  $Id: MainFrame.java,v 1.1 2011/01/13 16:51:38 textmine Exp $
 *
 */

package gate.gui;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.SecurityException;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.apache.log4j.*;

import junit.framework.Assert;

import com.ontotext.gate.vr.Gaze;

import gate.*;
import gate.creole.*;
import gate.creole.gazetteer.Gazetteer;
import gate.creole.annic.Constants;
import gate.event.*;
import gate.persist.PersistenceException;
import gate.security.*;
import gate.swing.*;
import gate.util.*;
import gate.util.persistence.PersistenceManager;
import gate.util.reporting.*;
import gate.util.reporting.exceptions.BenchmarkReportException;

/**
 * The main Gate GUI frame.
 */
public class MainFrame extends JFrame implements ProgressListener,
                                     StatusListener, CreoleListener {

  protected static final Logger log = Logger.getLogger(MainFrame.class);

  protected JMenuBar menuBar;

  protected JSplitPane mainSplit;

  protected JSplitPane leftSplit;

  protected JLabel statusBar;

  protected JButton alertButton;

  protected JProgressBar progressBar;

  protected JProgressBar globalProgressBar;

  protected XJTabbedPane mainTabbedPane;

  protected JScrollPane lowerScroll;

  /**
   * Popup used for right click actions on the Applications node.
   */
  protected JPopupMenu appsPopup;

  /**
   * Popup used for right click actions on the Datastores node.
   */
  protected JPopupMenu dssPopup;

  /**
   * Popup used for right click actions on the LRs node.
   */
  protected JPopupMenu lrsPopup;

  /**
   * Popup used for right click actions on the PRs node.
   */
  protected JPopupMenu prsPopup;

  protected JCheckBoxMenuItem verboseModeItem;

  protected JTree resourcesTree;

  protected JScrollPane resourcesTreeScroll;

  protected DefaultTreeModel resourcesTreeModel;

  protected DefaultMutableTreeNode resourcesTreeRoot;

  protected DefaultMutableTreeNode applicationsRoot;

  protected DefaultMutableTreeNode languageResourcesRoot;

  protected DefaultMutableTreeNode processingResourcesRoot;

  protected DefaultMutableTreeNode datastoresRoot;

  protected Splash splash;

  protected PluginManagerUI pluginManager;

  protected LogArea logArea;

  protected JScrollPane logScroll;

  protected JToolBar toolbar;

  protected static XJFileChooser fileChooser;

  static private MainFrame instance;

  protected OptionsDialog optionsDialog;

  protected CartoonMinder animator;

  protected TabHighlighter logHighlighter;

  protected NewResourceDialog newResourceDialog;

  protected HelpFrame helpFrame;

  /**
   * Holds all the icons used in the Gate GUI indexed by filename. This
   * is needed so we do not need to decode the icon everytime we need it
   * as that would use unnecessary CPU time and memory. Access to this
   * data is available through the {@link #getIcon(String)} method.
   */
  protected static Map<String, Icon> iconByName = new HashMap<String, Icon>();

  protected static java.util.Collection<Component> guiRoots =
    new ArrayList<Component>();

  /**
   * Extensions for icon files to be tried in this order.
   */
  protected static final String[] ICON_EXTENSIONS = {"", ".png", ".gif"};

  private static JDialog guiLock = null;

  static public Icon getIcon(String baseName) {
    Icon result = iconByName.get(baseName);
    for(int i = 0; i < ICON_EXTENSIONS.length && result == null; i++) {
      String extension = ICON_EXTENSIONS[i];
      String fileName = baseName + extension;
      URL iconURL;
      // if the ICON is an absolute path starting with '/', then just
      // load
      // it from that path. If it does not start with '/', treat it as
      // relative to gate/resources/img for backwards compatibility
      if(fileName.charAt(0) == '/') {
        iconURL = Files.getResource(fileName);
      }
      else {
        iconURL = Files.getGateResource("/img/" + fileName);
      }
      if(iconURL != null) {
        result = new ImageIcon(iconURL);
        iconByName.put(baseName, result);
      }
    }
    return result;
  }

  static public MainFrame getInstance() {
    if(instance == null) instance = new MainFrame();
    return instance;
  }

  /**
   * Get the file chooser.
   * @return the current file chooser
   */
  static public XJFileChooser getFileChooser() {
    return fileChooser;
  }

  /**
   * Gets the original system output stream, which was later redirected
   * to the messages pane.
   *
   * @return a {@link PrintStream} value.
   */
  public PrintStream getOriginalOut() {
    return logArea.getOriginalOut();
  }

  /**
   * Gets the original system error output stream, which was later
   * redirected to the messages pane.
   *
   * @return a {@link PrintStream} value.
   */
  public PrintStream getOriginalErr() {
    return logArea.getOriginalErr();
  }

  /**
   * Locates the handle for a given resource.
   * @param res the resource for which the handle is sought.
   * @return the {@link Handle} for the resource, if it it was found.
   */
  protected Handle findHandleForResource(Resource res){
    Handle handle = null;
    // go through all the nodes
    Enumeration nodesEnum = resourcesTreeRoot.breadthFirstEnumeration();
    while(nodesEnum.hasMoreElements() && handle == null) {
      Object node = nodesEnum.nextElement();
      if(node instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode dmtNode = (DefaultMutableTreeNode)node;
        if(dmtNode.getUserObject() instanceof Handle) {
          if(((Handle)dmtNode.getUserObject()).getTarget() == res) {
            handle = (Handle)dmtNode.getUserObject();
          }
        }
      }
    }
    return handle;
  }

  /**
   * Selects a resource if loaded in the system and not invisible.
   *
   * @param res the resource to be selected.
   * @return the {@link Handle} for the resource, null if not found.
   */
  public Handle select(Resource res) {
    // first find the handle for the resource
    Handle handle = findHandleForResource(res);
    // now select the handle if found
    if(handle != null) {
      select(handle);
    }
    return handle;
  }

  protected void select(Handle handle) {
    final JComponent largeView = handle.getLargeView();
    if(handle.viewsBuilt()
      && mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1) {
      // select
      if(largeView != null) {
        mainTabbedPane.setSelectedComponent(largeView);
      }
    }
    else {
      // show
      if(largeView != null) {
        mainTabbedPane.addTab(handle.getTitle(), handle.getIcon(), largeView,
          handle.getTooltipText());
        mainTabbedPane.setSelectedComponent(handle.getLargeView());
        // put the focus on the new tab
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (largeView != null) {
              if ((largeView instanceof JTabbedPane)
              && (((JTabbedPane)largeView).getSelectedComponent() != null)) {
                ((JTabbedPane)largeView).getSelectedComponent().requestFocus();
              } else {
                largeView.requestFocus();
              }
            }
          }
        });
      }
    }
    // show the small view
    JComponent smallView = handle.getSmallView();
    if(smallView != null) {
      lowerScroll.getViewport().setView(smallView);
    }
    else {
      lowerScroll.getViewport().setView(null);
    }
  }// protected void select(ResourceHandle handle)

  public MainFrame() {
    this(null);
  }

  public MainFrame(GraphicsConfiguration gc) {
    this(false, gc);
  } // MainFrame

  /**
   * Construct the frame.
   * @param isShellSlacGIU true for embedded uses of GATE where a simpler GUI
   *                       should be displayed.
   * @param gc graphics configuration used,
   *   see {@link javax.swing.JFrame#JFrame(java.awt.GraphicsConfiguration)}
   */
  public MainFrame(boolean isShellSlacGIU, GraphicsConfiguration gc) {
    super(gc);
    instance = this;
    guiRoots.add(this);
    if(fileChooser == null) {
      fileChooser = new XJFileChooser();
      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setAcceptAllFileFilterUsed(true);
      guiRoots.add(fileChooser);

      // the JFileChooser seems to size itself better once it's been
      // added to a top level container such as a dialog.
      JDialog dialog = new JDialog(this, "", true);
      java.awt.Container contentPane = dialog.getContentPane();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(fileChooser, BorderLayout.CENTER);
      dialog.pack();
      dialog.getContentPane().removeAll();
      dialog.dispose();
      dialog = null;
    }
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initLocalData(isShellSlacGIU);
    initGuiComponents(isShellSlacGIU);
    initListeners(isShellSlacGIU);
  } // MainFrame(boolean simple)

  protected void initLocalData(boolean isShellSlacGIU) {
    resourcesTreeRoot = new DefaultMutableTreeNode("GATE", true);
    applicationsRoot = new DefaultMutableTreeNode("Applications", true);
    if(isShellSlacGIU) {
      languageResourcesRoot = new DefaultMutableTreeNode("Documents", true);
    }
    else {
      languageResourcesRoot =
        new DefaultMutableTreeNode("Language Resources", true);
    } // if
    processingResourcesRoot =
      new DefaultMutableTreeNode("Processing Resources", true);
    datastoresRoot = new DefaultMutableTreeNode("Datastores", true);
    resourcesTreeRoot.add(applicationsRoot);
    resourcesTreeRoot.add(languageResourcesRoot);
    resourcesTreeRoot.add(processingResourcesRoot);
    resourcesTreeRoot.add(datastoresRoot);

    resourcesTreeModel = new ResourcesTreeModel(resourcesTreeRoot, true);
  }

  protected void initGuiComponents(boolean isShellSlacGUI) {
    this.getContentPane().setLayout(new BorderLayout());

    Integer width = Gate.getUserConfig().getInt(GateConstants.MAIN_FRAME_WIDTH);
    Integer height =
      Gate.getUserConfig().getInt(GateConstants.MAIN_FRAME_HEIGHT);
    this.setSize(new Dimension(width == null ? 800 : width,
      height == null ? 600 : height));

    // TODO: when upgrading to Java 1.6 use setIconImages() instead
    this.setIconImage(Toolkit.getDefaultToolkit().getImage(
      Files.getGateResource("/img/gate-icon.png")));
    resourcesTree = new ResourcesTree();
    resourcesTree.setModel(resourcesTreeModel);
    resourcesTree.setRowHeight(0);

    resourcesTree.setEditable(true);
    ResourcesTreeCellRenderer treeCellRenderer =
      new ResourcesTreeCellRenderer();
    resourcesTree.setCellRenderer(treeCellRenderer);
    resourcesTree.setCellEditor(new ResourcesTreeCellEditor(resourcesTree,
      treeCellRenderer));

    resourcesTree.setRowHeight(0);
    // expand all nodes
    resourcesTree.expandRow(0);
    resourcesTree.expandRow(1);
    resourcesTree.expandRow(2);
    resourcesTree.expandRow(3);
    resourcesTree.expandRow(4);
    resourcesTree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    resourcesTree.setEnabled(true);
    ToolTipManager.sharedInstance().registerComponent(resourcesTree);
    resourcesTreeScroll = new JScrollPane(resourcesTree);

    resourcesTree.setDragEnabled(true);
    resourcesTree.setTransferHandler(new TransferHandler() {
      // drag and drop that export a list of the selected documents
      public int getSourceActions(JComponent c) {
        return COPY;
      }
      protected Transferable createTransferable(JComponent c) {
        TreePath[] paths = resourcesTree.getSelectionPaths();
        if(paths == null) { return new StringSelection(""); }
        Handle handle;
        List<String> documentsNames = new ArrayList<String>();
        for(TreePath path : paths) {
          if(path != null) {
            Object value = path.getLastPathComponent();
            value = ((DefaultMutableTreeNode)value).getUserObject();
            if(value instanceof Handle) {
              handle = (Handle)value;
              if(handle.getTarget() instanceof Document) {
                documentsNames.add(((Document)handle.getTarget()).getName());
              }
            }
          }
        }
        return new StringSelection("ResourcesTree"
          + Arrays.toString(documentsNames.toArray()));
      }
      protected void exportDone(JComponent c, Transferable data, int action) {
      }
      public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return false;
      }
      public boolean importData(JComponent c, Transferable t) {
        return false;
      }
    });

    lowerScroll = new JScrollPane();
    JPanel lowerPane = new JPanel();
    lowerPane.setLayout(new OverlayLayout(lowerPane));

    JPanel animationPane = new JPanel();
    animationPane.setOpaque(false);
    animationPane.setLayout(new BoxLayout(animationPane, BoxLayout.X_AXIS));

    JPanel vBox = new JPanel();
    vBox.setLayout(new BoxLayout(vBox, BoxLayout.Y_AXIS));
    vBox.setOpaque(false);

    JPanel hBox = new JPanel();
    hBox.setLayout(new BoxLayout(hBox, BoxLayout.X_AXIS));
    hBox.setOpaque(false);

    vBox.add(Box.createVerticalGlue());
    vBox.add(animationPane);

    hBox.add(vBox);
    hBox.add(Box.createHorizontalGlue());

    lowerPane.add(hBox);
    lowerPane.add(lowerScroll);

    animator = new CartoonMinder(animationPane);
    Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
      animator, "MainFrame animation");
    thread.setDaemon(true);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();

    leftSplit =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT, resourcesTreeScroll, lowerPane);
    leftSplit.setResizeWeight(0.7);
    leftSplit.setContinuousLayout(true);
    leftSplit.setOneTouchExpandable(true);

    // Create a new logArea and redirect the Out and Err output to it.
    logArea = new LogArea();
    logScroll = new JScrollPane(logArea);
    // Out has been redirected to the logArea

    Out.prln("GATE " + Main.version + " build " + Main.build + " started at "
      + new Date().toString());
    Out.prln("and using Java " + System.getProperty("java.version") + " " +
      System.getProperty("java.vendor") + " on " +
      System.getProperty("os.name") + " " +
      System.getProperty("os.arch") + " " +
      System.getProperty("os.version") + ".");
    mainTabbedPane = new XJTabbedPane(JTabbedPane.TOP);
    mainTabbedPane.insertTab("Messages", null, logScroll, "GATE log", 0);

    logHighlighter = new TabHighlighter(mainTabbedPane, logScroll, Color.red);

    mainSplit =
      new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, mainTabbedPane);
    mainSplit.setDividerLocation(leftSplit.getPreferredSize().width + 10);
    this.getContentPane().add(mainSplit, BorderLayout.CENTER);
    mainSplit.setContinuousLayout(true);
    mainSplit.setOneTouchExpandable(true);

    // status and progress bars
    statusBar = new JLabel();
    statusBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

    UIManager.put("ProgressBar.cellSpacing", 0);
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
    progressBar.setBorder(BorderFactory.createEmptyBorder());
    progressBar.setForeground(new Color(150, 75, 150));
    progressBar.setStringPainted(false);

    globalProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
    globalProgressBar.setBorder(BorderFactory.createEmptyBorder());
    globalProgressBar.setForeground(new Color(150, 75, 150));
    globalProgressBar.setStringPainted(true);

    Icon alertIcon = getIcon("crystal-clear-app-error");
    alertButton = new JButton(alertIcon);
    alertButton.setToolTipText("There was no error");
    alertButton.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 5));
    alertButton.setPreferredSize(new Dimension(alertIcon.getIconWidth(),
      alertIcon.getIconHeight()));
    alertButton.setEnabled(false);

    JPanel southBox = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weightx = 1;
    southBox.add(statusBar, gbc);
    gbc.insets = new Insets(0, 3, 0, 3);
    gbc.anchor = GridBagConstraints.EAST;
    gbc.weightx = 0;
    southBox.add(progressBar, gbc);
    southBox.add(globalProgressBar, gbc);
    southBox.add(alertButton, gbc);

    this.getContentPane().add(southBox, BorderLayout.SOUTH);
    progressBar.setVisible(false);
    globalProgressBar.setVisible(false);

    // extra stuff
    newResourceDialog =
      new NewResourceDialog(this, "Resource parameters", true);

    // build the Help->About dialog
    JPanel splashBox = new JPanel();
    splashBox.setBackground(Color.WHITE);

    splashBox.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1;
    constraints.insets = new Insets(2, 2, 2, 2);
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;

    JLabel gifLbl = new JLabel(getIcon("splash"));
    splashBox.add(gifLbl, constraints);

    constraints.gridy = 2;
    constraints.gridwidth = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    String splashHtml;
    try {
      splashHtml = Files.getGateResourceAsString("splash.html");
    }
    catch(IOException e) {
      splashHtml = "GATE";
      log.error("Couldn't get splash.html resource.", e);
    }
    JLabel htmlLbl = new JLabel(splashHtml);
    htmlLbl.setHorizontalAlignment(SwingConstants.CENTER);
    splashBox.add(htmlLbl, constraints);

    constraints.gridy = 3;
    htmlLbl =
      new JLabel("<HTML><FONT color=\"blue\">Version <B>" + Main.version
        + "</B></FONT>" + ", <FONT color=\"red\">build <B>" + Main.build
        + "</B></FONT>" + "<P><B>JVM version</B>: "
        + System.getProperty("java.version") + " from "
        + System.getProperty("java.vendor") + "</HTML>");
    constraints.fill = GridBagConstraints.HORIZONTAL;
    splashBox.add(htmlLbl, constraints);

    constraints.gridy = 4;
    constraints.gridwidth = 2;
    constraints.fill = GridBagConstraints.NONE;
    final JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        splash.setVisible(false);
      }
    });
    okButton.setBackground(Color.white);
    splashBox.add(okButton, constraints);
    splash = new Splash(this, splashBox);
    // make Enter and Escape keys closing the splash window
    splash.getRootPane().setDefaultButton(okButton);
    InputMap inputMap = ((JComponent)splash.getContentPane())
      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = ((JComponent)splash.getContentPane()).getActionMap();
    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "Apply");
    actionMap.put("Apply", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        okButton.doClick();
      }
    });
    inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "Cancel");
    actionMap.put("Cancel", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        okButton.doClick();
      }
    });

    // MENUS
    menuBar = new JMenuBar();

    JMenu fileMenu = new XJMenu("File", null, this);
    fileMenu.setMnemonic(KeyEvent.VK_F);

    LiveMenu newAPPMenu = new LiveMenu(LiveMenu.APP);
    newAPPMenu.setText("New Application");
    newAPPMenu.setIcon(getIcon("applications"));
    fileMenu.add(newAPPMenu);

    LiveMenu newLRMenu = new LiveMenu(LiveMenu.LR);
    newLRMenu.setText("New Language Resource");
    newLRMenu.setIcon(getIcon("lrs"));
    fileMenu.add(newLRMenu);

    LiveMenu newPRMenu = new LiveMenu(LiveMenu.PR);
    newPRMenu.setText("New Processing Resource");
    newPRMenu.setIcon(getIcon("prs"));
    fileMenu.add(newPRMenu);

    final JMenu dsMenu = new XJMenu("Datastores",
      "Repositories for large data", this);
    dsMenu.setIcon(getIcon("datastores"));
    dsMenu.add(new XJMenuItem(new NewDSAction(), this));
    dsMenu.add(new XJMenuItem(new OpenDSAction(), this));
    fileMenu.add(dsMenu);

    fileMenu.addSeparator();
    fileMenu.add(new XJMenuItem(new LoadResourceFromFileAction(), this));

    RecentAppsMenu recentAppsMenu = new RecentAppsMenu();
    recentAppsMenu.setText("Recent Applications");
    recentAppsMenu.setIcon(getIcon("open-application"));
    fileMenu.add(recentAppsMenu);

    final JMenu loadANNIEMenu = new XJMenu("Load ANNIE System",
      "Application that adds morphosyntaxic and semantic annotations", this);
    loadANNIEMenu.setIcon(getIcon("annie-application"));
    loadANNIEMenu.add(new XJMenuItem(new LoadANNIEWithDefaultsAction(), this));
    loadANNIEMenu
      .add(new XJMenuItem(new LoadANNIEWithoutDefaultsAction(), this));
    fileMenu.add(loadANNIEMenu);

    // fileMenu.add(new XJMenuItem(new LoadCreoleRepositoryAction(),
    // this));

    // LingPipe action
    fileMenu.add(new XJMenuItem(new LoadApplicationAction(
            "Load LingPipe System", "LingPipe", "resources/lingpipe.gapp"),
            this));

    // OpenNLP action
    fileMenu.add(new XJMenuItem(new LoadApplicationAction(
            "Load OpenNLP System", "OpenNLP", "resources/opennlp.gapp"), this));

    fileMenu.add(new XJMenuItem(new ManagePluginsAction(), this));

    if(!Gate.runningOnMac()) {
      fileMenu.addSeparator();
      fileMenu.add(new XJMenuItem(new ExitGateAction(), this));
    }

    menuBar.add(fileMenu);

    JMenu optionsMenu = new XJMenu("Options", null, this);
    optionsMenu.setMnemonic(KeyEvent.VK_O);

    boolean optionsMenuHasEntries = false;

    optionsDialog = new OptionsDialog(MainFrame.this);
    if(!Gate.runningOnMac()) {
      optionsMenu.add(new XJMenuItem(new AbstractAction("Configuration") {
        private static final long serialVersionUID = 1L;
        {
          putValue(SHORT_DESCRIPTION, "Edit GATE options");
        }

        public void actionPerformed(ActionEvent evt) {
          optionsDialog.showDialog();
          optionsDialog.dispose();
        }
      }, this));
      optionsMenuHasEntries = true;
    }

    JMenu imMenu = null;
    List<Locale> installedLocales = new ArrayList<Locale>();
    try {
      // if this fails guk is not present
      Class.forName("guk.im.GateIMDescriptor");
      // add the Gate input methods
      installedLocales.addAll(Arrays.asList(new guk.im.GateIMDescriptor()
        .getAvailableLocales()));
    }
    catch(Exception e) {
      // something happened; most probably guk not present.
      // just drop it, is not vital.
    }
    try {
      // add the MPI IMs
      // if this fails mpi IM is not present
      Class.forName("mpi.alt.java.awt.im.spi.lookup.LookupDescriptor");

      installedLocales.addAll(Arrays
        .asList(new mpi.alt.java.awt.im.spi.lookup.LookupDescriptor()
          .getAvailableLocales()));
    }
    catch(Exception e) {
      // something happened; most probably MPI not present.
      // just drop it, is not vital.
    }

    Collections.sort(installedLocales, new Comparator<Locale>() {
      public int compare(Locale o1, Locale o2) {
        return o1.getDisplayName().compareTo(o2.getDisplayName());
      }
    });
    JMenuItem item;
    if(!installedLocales.isEmpty()) {
      imMenu = new XJMenu("Input Methods");
      ButtonGroup bg = new ButtonGroup();
      item = new LocaleSelectorMenuItem();
      imMenu.add(item);
      item.setSelected(true);
      imMenu.addSeparator();
      bg.add(item);
      for(Locale locale : installedLocales) {
        item = new LocaleSelectorMenuItem(locale);
        imMenu.add(item);
        bg.add(item);
      }
    }
    if(imMenu != null) {
      optionsMenu.add(imMenu);
      optionsMenuHasEntries = true;
    }

    if(optionsMenuHasEntries) {
      menuBar.add(optionsMenu);
    }

    ToolsMenu toolsMenu = new ToolsMenu("Tools", null, this);
    toolsMenu.setMnemonic(KeyEvent.VK_T);
    toolsMenu.add(new XJMenuItem(new NewAnnotDiffAction(), this));

    final JMenuItem reportClearMenuItem = new XJMenuItem(
      new AbstractAction("Clear Profiling History") {
      { putValue(SHORT_DESCRIPTION,
        "Clear profiling history otherwise the report is cumulative."); }
      public void actionPerformed(ActionEvent evt) {
        // create a new log file
        File logFile = new File(System.getProperty("java.io.tmpdir"),
          "gate-benchmark-log.txt");
        if (logFile.exists() && !logFile.delete()) {
          log.info("Error when deleting the file:\n" +
            logFile.getAbsolutePath());
        }
      }
    }, this);
    JMenu reportMenu = new XJMenu("Profiling Reports",
      "Generates profiling reports from processing resources", this);
    reportMenu.setIcon(getIcon("gazetteer"));
    reportMenu.add(new XJMenuItem(
      new AbstractAction("Start Profiling Applications") {
      { putValue(SHORT_DESCRIPTION,
        "Toggles the profiling of processing resources"); }

      // stores the value held by the benchmarking switch before we started
      // this profiling run.
      boolean benchmarkWasEnabled;

      public void actionPerformed(ActionEvent evt) {
        if (getValue(NAME).equals("Start Profiling Applications")) {
          reportClearMenuItem.setEnabled(false);
          // store old value of benchmark switch
          benchmarkWasEnabled = Benchmark.isBenchmarkingEnabled();
          Benchmark.setBenchmarkingEnabled(true);
          Layout layout = new PatternLayout("%m%n");
          File logFile = new File(System.getProperty("java.io.tmpdir"),
            "gate-benchmark-log.txt");
          Appender appender;
          try {
            appender =
              new FileAppender(layout, logFile.getAbsolutePath(), true);
          } catch (IOException e) {
            e.printStackTrace();
            return;
          }
          appender.setName("gate-benchmark");
          Benchmark.logger.addAppender(appender);
          putValue(NAME, "Stop Profiling Applications");
        } else {
          // reset old value of benchmark switch - i.e. if benchmarking was
          // disabled before the user selected "start profiling" then we
          // disable it again now, but if it was already enabled before they
          // started profiling then we assume it was turned on explicitly and
          // leave it alone.
          Benchmark.setBenchmarkingEnabled(benchmarkWasEnabled);
          Benchmark.logger.removeAppender("gate-benchmark");
          putValue(NAME, "Start Profiling Applications");
          reportClearMenuItem.setEnabled(true);
        }
      }
    }, this));
    reportMenu.add(reportClearMenuItem);
    reportMenu.addSeparator();

    final JCheckBoxMenuItem reportZeroTimesCheckBox = new JCheckBoxMenuItem();
    reportZeroTimesCheckBox.setAction(
      new AbstractAction("Report Zero Time Entries") {
      public void actionPerformed(ActionEvent evt) {
        Gate.getUserConfig().put(MainFrame.class.getName()+".reportzerotime",
          reportZeroTimesCheckBox.isSelected());
      }
    });
    reportZeroTimesCheckBox.setSelected(Gate.getUserConfig().getBoolean(
        MainFrame.class.getName()+".reportzerotimes"));
    ButtonGroup group = new ButtonGroup();
    final JRadioButtonMenuItem reportSortExecution = new JRadioButtonMenuItem();
    reportSortExecution.setAction(new AbstractAction("Sort by Execution") {
      public void actionPerformed(ActionEvent evt) {
        Gate.getUserConfig().put(
          MainFrame.class.getName()+".reportsorttime", false);
      }
    });
    reportSortExecution.setSelected(!Gate.getUserConfig().getBoolean(
      MainFrame.class.getName()+".reportsorttime"));
    group.add(reportSortExecution);
    final JRadioButtonMenuItem reportSortTime = new JRadioButtonMenuItem();
    reportSortTime.setAction(new AbstractAction("Sort by Time") {
      public void actionPerformed(ActionEvent evt) {
        Gate.getUserConfig().put(
          MainFrame.class.getName()+".reportsorttime", true);
      }
    });
    reportSortTime.setSelected(Gate.getUserConfig().getBoolean(
      MainFrame.class.getName()+".reportsorttime"));
    group.add(reportSortTime);
    reportMenu.add(new XJMenuItem(
      new AbstractAction("Report on Processing Resources") {
      { putValue(SHORT_DESCRIPTION,
        "Report time taken by each processing resource"); }
      public void actionPerformed(ActionEvent evt) {
        PRTimeReporter report = new PRTimeReporter();
        report.setBenchmarkFile(new File(System.getProperty("java.io.tmpdir"),
          "gate-benchmark-log.txt"));
        report.setSuppressZeroTimeEntries(!reportZeroTimesCheckBox.isSelected());
        report.setSortOrder(reportSortTime.isSelected() ?
          PRTimeReporter.SORT_TIME_TAKEN : PRTimeReporter.SORT_EXEC_ORDER);
        try {
          report.executeReport();
        } catch (BenchmarkReportException e) {
          e.printStackTrace();
          return;
        }
        showHelpFrame("file://" + report.getReportFile(),
          "processing times report");
      }
    }, this));
    reportMenu.add(reportZeroTimesCheckBox);
    reportMenu.add(reportSortTime);
    reportMenu.add(reportSortExecution);
    reportMenu.addSeparator();

    reportMenu.add(new XJMenuItem(
      new AbstractAction("Report on Documents Processed") {
        { putValue(SHORT_DESCRIPTION, "Report most time consuming documents"); }
        public void actionPerformed(ActionEvent evt) {
          DocTimeReporter report = new DocTimeReporter();
          report.setBenchmarkFile(new File(System.getProperty("java.io.tmpdir"),
            "gate-benchmark-log.txt"));
          String maxDocs = Gate.getUserConfig().getString(
            MainFrame.class.getName()+".reportmaxdocs");
          if (maxDocs != null) {
            report.setMaxDocumentInReport((maxDocs.equals("All")) ?
              DocTimeReporter.ALL_DOCS : Integer.valueOf(maxDocs));
          }
          String prRegex = Gate.getUserConfig().getString(
            MainFrame.class.getName()+".reportprregex");
          if (prRegex != null) {
            report.setPRMatchingRegex((prRegex.equals("")) ?
              DocTimeReporter.MATCH_ALL_PR_REGEX : prRegex);
          }
          try {
            report.executeReport();
          } catch (BenchmarkReportException e) {
            e.printStackTrace();
            return;
          }
          showHelpFrame("file://" + report.getReportFile(),
            "documents time report");
        }
      }, this));
    String maxDocs = Gate.getUserConfig().getString(
      MainFrame.class.getName()+".reportmaxdocs");
    if (maxDocs == null) { maxDocs = "10"; }
    reportMenu.add(new XJMenuItem(
      new AbstractAction("Set Max Documents (" + maxDocs + ")") {
        public void actionPerformed(ActionEvent evt) {
          Object response = JOptionPane.showInputDialog(instance,
              "Set the maximum of documents to report", "Report options",
              JOptionPane.QUESTION_MESSAGE, null,
              new Object[]{"All", "10", "20", "30", "40", "50", "100"}, "10");
          if (response != null) {
            Gate.getUserConfig().put(
              MainFrame.class.getName()+".reportmaxdocs",response);
            putValue(NAME, "Set Max Documents (" + response + ")");
          }
        }
      }, this));
    String prRegex = Gate.getUserConfig().getString(
      MainFrame.class.getName()+".reportprregex");
    if (prRegex == null || prRegex.equals("")) { prRegex = "All"; }
    reportMenu.add(new XJMenuItem(
      new AbstractAction("Set PR Matching Regex (" + prRegex + ")") {
        public void actionPerformed(ActionEvent evt) {
          Object response = JOptionPane.showInputDialog(instance,
            "Set the processing resource regex filter\n" +
            "Leave empty to not filter", "Report options",
              JOptionPane.QUESTION_MESSAGE);
          if (response != null) {
            Gate.getUserConfig().put(
              MainFrame.class.getName()+".reportprregex",response);
            if (response.equals("")) { response = "All"; }
            putValue(NAME, "Set PR Matching Regex (" + response + ")");
          }
        }
      }, this));
    toolsMenu.add(reportMenu);

    toolsMenu.add(new XJMenuItem(new NewBootStrapAction(), this));
    final JMenu corpusEvalMenu = new XJMenu("Corpus Benchmark",
      "Compares processed and human-annotated annotations", this);
    corpusEvalMenu.setIcon(getIcon("corpus-benchmark"));
    toolsMenu.add(corpusEvalMenu);
    corpusEvalMenu.add(new XJMenuItem(new NewCorpusEvalAction(), this));
    corpusEvalMenu.addSeparator();
    corpusEvalMenu.add(new XJMenuItem(
      new GenerateStoredCorpusEvalAction(), this));
    corpusEvalMenu.addSeparator();
    corpusEvalMenu.add(new XJMenuItem(
      new StoredMarkedCorpusEvalAction(), this));
    corpusEvalMenu.add(new XJMenuItem(new CleanMarkedCorpusEvalAction(), this));
    corpusEvalMenu.addSeparator();
    verboseModeItem =
      new JCheckBoxMenuItem(new VerboseModeCorpusEvalToolAction());
    corpusEvalMenu.add(verboseModeItem);
    toolsMenu.add(new XJMenuItem(
      new AbstractAction("Unicode Editor", getIcon("unicode")) {
      { putValue(SHORT_DESCRIPTION, "Editor for testing character encoding"); }
      private static final long serialVersionUID = 1L;
      public void actionPerformed(ActionEvent evt) {
        new guk.Editor();
      }
    }, this));

    // add separator.  plugin menu items will appear after this separator
    toolsMenu.addSeparator();
    toolsMenu.staticItemsAdded();
    if (toolsMenu.getMenuComponent(toolsMenu.getMenuComponentCount()-1)
        instanceof JSeparator) { // remove separator if no tools
      toolsMenu.remove(toolsMenu.getMenuComponentCount()-1);
    }

    menuBar.add(toolsMenu);

    JMenu helpMenu = new XJMenu("Help", null, MainFrame.this);
    helpMenu.setMnemonic(KeyEvent.VK_H);
    helpMenu.add(new XJMenuItem(new HelpUserGuideAction(), this));
    helpMenu.add(new XJMenuItem(new HelpUserGuideInContextAction(), this));
    helpMenu.add(new XJMenuItem(new AbstractAction("Keyboard Shortcuts") {
      public void actionPerformed(ActionEvent e) {
        showHelpFrame("sec:developer:keyboard", "shortcuts");
      }
    }, this));
    helpMenu.addSeparator();
    helpMenu.add(new XJMenuItem(new AbstractAction("Using GATE Developer") {
      { this.putValue(Action.SHORT_DESCRIPTION, "To read first"); }
      public void actionPerformed(ActionEvent e) {
        showHelpFrame("chap:developer", "Using GATE Developer");
      }
    }, this));
    helpMenu.add(new XJMenuItem(new AbstractAction("Demo Movies") {
      { this.putValue(Action.SHORT_DESCRIPTION, "Movie tutorials"); }
      public void actionPerformed(ActionEvent e) {
        showHelpFrame("http://gate.ac.uk/demos/movies.html", "movies");
      }
    }, this));
    helpMenu.add(new XJMenuItem(new HelpMailingListAction(), this));
    helpMenu.addSeparator();
    JCheckBoxMenuItem toggleToolTipsCheckBoxMenuItem =
      new JCheckBoxMenuItem(new ToggleToolTipsAction());
    javax.swing.ToolTipManager toolTipManager =
      ToolTipManager.sharedInstance();
    if (Gate.getUserConfig().getBoolean(
        MainFrame.class.getName()+".hidetooltips")) {
      toolTipManager.setEnabled(false);
      toggleToolTipsCheckBoxMenuItem.setSelected(false);
    } else {
      toolTipManager.setEnabled(true);
      toggleToolTipsCheckBoxMenuItem.setSelected(true);
    }
    helpMenu.add(toggleToolTipsCheckBoxMenuItem);
    helpMenu.add(new XJMenuItem(new AbstractAction("What's New") {
      { this.putValue(Action.SHORT_DESCRIPTION,
          "List new features and important changes"); }
      public void actionPerformed(ActionEvent e) {
        showHelpFrame("chap:changes", "changes");
      }
    }, this));
    if(!Gate.runningOnMac()) {
      helpMenu.add(new XJMenuItem(new HelpAboutAction(), this));
    }
    menuBar.add(helpMenu);

    this.setJMenuBar(menuBar);

    // popups
    lrsPopup = new XJPopupMenu();
    LiveMenu lrsMenu = new LiveMenu(LiveMenu.LR);
    lrsMenu.setText("New");
    lrsPopup.add(lrsMenu);
    guiRoots.add(lrsPopup);
    guiRoots.add(lrsMenu);

    prsPopup = new XJPopupMenu();
    LiveMenu prsMenu = new LiveMenu(LiveMenu.PR);
    prsMenu.setText("New");
    prsPopup.add(prsMenu);
    guiRoots.add(prsPopup);
    guiRoots.add(prsMenu);

    dssPopup = new XJPopupMenu();
    dssPopup.add(new NewDSAction());
    dssPopup.add(new OpenDSAction());
    guiRoots.add(dssPopup);

    // TOOLBAR
    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    JButton button = new JButton(new LoadResourceFromFileAction());
    button.setToolTipText(button.getText());
    button.setText("");
    toolbar.add(button);
    toolbar.addSeparator();

    JPopupMenu annieMenu = new JPopupMenu();
    annieMenu.add(new LoadANNIEWithDefaultsAction());
    annieMenu.add(new LoadANNIEWithoutDefaultsAction());
    JMenuButton menuButton = new JMenuButton(annieMenu);
    menuButton.setIcon(getIcon("annie-application"));
    menuButton.setToolTipText("Load ANNIE System");
    toolbar.add(menuButton);
    toolbar.addSeparator();

    LiveMenu tbNewLRMenu = new LiveMenu(LiveMenu.LR);
    menuButton = new JMenuButton(tbNewLRMenu);
    menuButton.setToolTipText("New Language Resource");
    menuButton.setIcon(getIcon("lrs"));
    toolbar.add(menuButton);

    LiveMenu tbNewPRMenu = new LiveMenu(LiveMenu.PR);
    menuButton = new JMenuButton(tbNewPRMenu);
    menuButton.setToolTipText("New Processing Resource");
    menuButton.setIcon(getIcon("prs"));
    toolbar.add(menuButton);

    LiveMenu tbNewAppMenu = new LiveMenu(LiveMenu.APP);
    menuButton = new JMenuButton(tbNewAppMenu);
    menuButton.setToolTipText("New Application");
    menuButton.setIcon(getIcon("applications"));
    toolbar.add(menuButton);
    toolbar.addSeparator();

    JPopupMenu tbDsMenu = new JPopupMenu();
    tbDsMenu.add(new NewDSAction());
    tbDsMenu.add(new OpenDSAction());
    menuButton = new JMenuButton(tbDsMenu);
    menuButton.setToolTipText("Datastores");
    menuButton.setIcon(getIcon("datastores"));
    toolbar.add(menuButton);

    toolbar.addSeparator();
    button = new JButton(new ManagePluginsAction());
    button.setToolTipText(button.getText());
    button.setText("");
    toolbar.add(button);
    toolbar.addSeparator();
    button = new JButton(new NewAnnotDiffAction());
    button.setToolTipText(button.getText());
    button.setText("");
    toolbar.add(button);
    toolbar.add(Box.createHorizontalGlue());

    this.getContentPane().add(toolbar, BorderLayout.NORTH);
  }

  protected void initListeners(boolean isShellSlacGIU) {
    Gate.getCreoleRegister().addCreoleListener(this);

    resourcesTree.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          // shows in the central tabbed pane, the selected resources
          // in the resource tree when the Enter key is pressed
          (new ShowSelectedResourcesAction()).actionPerformed(null);
        } else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
          // close selected resources from GATE
          (new CloseSelectedResourcesAction()).actionPerformed(null);
        } else if(e.getKeyCode() == KeyEvent.VK_DELETE
               && e.getModifiers() == InputEvent.SHIFT_DOWN_MASK) {
          // close recursively selected resources from GATE
          (new CloseRecursivelySelectedResourcesAction()).actionPerformed(null);
        }
      }
    });

    resourcesTree.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        TreePath path =
          resourcesTree.getClosestPathForLocation(e.getX(), e.getY());
        if(e.isPopupTrigger()
        && !resourcesTree.isPathSelected(path)) {
          // if right click outside the selection then reset selection
          resourcesTree.getSelectionModel().setSelectionPath(path);
        }
        processMouseEvent(e);
      }
      public void mouseReleased(MouseEvent e) {
          processMouseEvent(e);
      }
      public void mouseClicked(MouseEvent e) {
        processMouseEvent(e);
      }
      protected void processMouseEvent(MouseEvent e){
        // where inside the tree?
        int x = e.getX();
        int y = e.getY();
        TreePath path = resourcesTree.getClosestPathForLocation(x, y);
        JPopupMenu popup = null;
        Handle handle = null;
        if(path != null) {
          Object value = path.getLastPathComponent();
          if(value == resourcesTreeRoot) {
            // no default item for this menu
          }
          else if(value == applicationsRoot) {
            appsPopup = new XJPopupMenu();
            LiveMenu appsMenu = new LiveMenu(LiveMenu.APP);
            appsMenu.setText("New");
            appsPopup.add(appsMenu);
            guiRoots.add(appsPopup);
            guiRoots.add(appsMenu);
            appsPopup.add(new XJMenuItem(new LoadResourceFromFileAction(),
              MainFrame.this));
            Component[] components = new RecentAppsMenu().getMenuComponents();
            if (components.length > 0) {
              appsPopup.addSeparator();
              appsPopup.add("Recent Applications:");
            }
            for (Component menuItem : components) {
                // add each menu item from the application recent menu
                appsPopup.add(menuItem);
            }
            popup = appsPopup;
          }
          else if(value == languageResourcesRoot) {
            popup = lrsPopup;
          }
          else if(value == processingResourcesRoot) {
            popup = prsPopup;
          }
          else if(value == datastoresRoot) {
            popup = dssPopup;
          }
          else {
            value = ((DefaultMutableTreeNode)value).getUserObject();
            if(value instanceof Handle) {
              handle = (Handle)value;
              fileChooser.setResource(handle.getTarget().getClass().getName());
              if(e.isPopupTrigger()) { popup = handle.getPopup(); }
            }
          }
        }
        // popup menu
        if(e.isPopupTrigger()) {
          if(resourcesTree.getSelectionCount() > 1) {
            // multiple selection in tree
            popup = new XJPopupMenu();

            // add a close all action
            popup.add(new XJMenuItem(new CloseSelectedResourcesAction(),
                    MainFrame.this));

            // add a close recursively all action
            TreePath[] selectedPaths = resourcesTree.getSelectionPaths();
            for(TreePath selectedPath : selectedPaths) {
              Object userObject = ((DefaultMutableTreeNode)
                selectedPath.getLastPathComponent()).getUserObject();
              if(userObject instanceof NameBearerHandle
                && ((NameBearerHandle)userObject).getTarget()
                  instanceof Controller) {
                // there is at least one application
                popup.add(new XJMenuItem(new
                  CloseRecursivelySelectedResourcesAction(), MainFrame.this));
                break;
              }
            }

            // add a show all action
            selectedPaths = resourcesTree.getSelectionPaths();
            for(TreePath selectedPath : selectedPaths) {
              Object userObject = ((DefaultMutableTreeNode)
                selectedPath.getLastPathComponent()).getUserObject();
              if (userObject instanceof Handle
              && ( ((Handle)userObject).getLargeView() == null
                || (mainTabbedPane.indexOfComponent(
                  ((Handle)userObject).getLargeView()) == -1)) ) {
                // there is at least one resource not shown
                popup.add(new XJMenuItem(new ShowSelectedResourcesAction(),
                  MainFrame.this));
                break;
              }
            }

            // add a hide all action
            selectedPaths = resourcesTree.getSelectionPaths();
            for(TreePath selectedPath : selectedPaths) {
              Object userObject = ((DefaultMutableTreeNode)
                selectedPath.getLastPathComponent()).getUserObject();
              if (userObject instanceof Handle
              && ((Handle)userObject).viewsBuilt()
              && ((Handle)userObject).getLargeView() != null
              && (mainTabbedPane.indexOfComponent(
                ((Handle)userObject).getLargeView()) != -1)) {
                // there is at least one resource shown
                popup.add(new XJMenuItem(new CloseViewsForSelectedResourcesAction(),
                  MainFrame.this));
                break;
              }
            }

            popup.show(resourcesTree, e.getX(), e.getY());
          }
          else if(popup != null) {
            if(handle != null) {

              // add a close action
              if(handle instanceof NameBearerHandle) {
                popup.insert(new XJMenuItem(((NameBearerHandle)handle)
                        .getCloseAction(), MainFrame.this), 0);
              }

              // if application then add a close recursively action
              if(handle instanceof NameBearerHandle
              && handle.getTarget() instanceof Controller) {
                popup.insert(new XJMenuItem(((NameBearerHandle)handle)
                  .getCloseRecursivelyAction(), MainFrame.this), 1);
              }

              // add a show/hide action
              if (handle.viewsBuilt() &&
                  handle.getLargeView() != null
                  && (mainTabbedPane.indexOfComponent(
                          handle.getLargeView()) != -1)) {
               popup.insert(new XJMenuItem(new CloseViewAction(handle),
                 MainFrame.this), 2);
              } else {
                popup.insert(new XJMenuItem(new ShowResourceAction(handle),
                  MainFrame.this), 2);
              }

              // add a rename action
              popup.insert(new XJMenuItem(new RenameResourceAction(path),
                MainFrame.this), 3);

              // add a help action
              if(handle instanceof NameBearerHandle) {
                popup.insert(new XJMenuItem(new HelpOnItemTreeAction(
                  (NameBearerHandle)handle), MainFrame.this), 4);
              }
            }

            popup.show(resourcesTree, e.getX(), e.getY());
          }
        }
        else if(e.getID() == MouseEvent.MOUSE_CLICKED
             && e.getClickCount() == 2
             && handle != null) {
            // double click - show the resource
            select(handle);
        }
      }
    });

    resourcesTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        if (!Gate.getUserConfig().getBoolean(
          MainFrame.class.getName()+".treeselectview")) {
          return;
        }
        // synchronise the selected tabbed pane with
        // the resource tree selection
        if (resourcesTree.getSelectionPaths() != null
         && resourcesTree.getSelectionPaths().length == 1) {
          Object value = e.getPath().getLastPathComponent();
          Object object = ((DefaultMutableTreeNode)value).getUserObject();
          if (object instanceof Handle
              && ((Handle)object).viewsBuilt()
              && (mainTabbedPane.indexOfComponent(
                      ((Handle)object).getLargeView()) != -1)) {
            select((Handle)object);
          }
        }
      }
    });

    // define keystrokes action bindings at the level of the main window
    InputMap inputMap = ((JComponent)this.getContentPane()).
      getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inputMap.put(KeyStroke.getKeyStroke("control F4"), "Close resource");
    inputMap.put(KeyStroke.getKeyStroke("shift F4"), "Close recursively");
    inputMap.put(KeyStroke.getKeyStroke("control H"), "Hide");
    inputMap.put(KeyStroke.getKeyStroke("control shift H"), "Hide all");
    inputMap.put(KeyStroke.getKeyStroke("control S"), "Save As XML");

    // add the support of the context menu key in tables and trees
    // TODO: remove when Swing will take care of the context menu key
    if (inputMap.get(KeyStroke.getKeyStroke("CONTEXT_MENU")) == null) {
      inputMap.put(KeyStroke.getKeyStroke("CONTEXT_MENU"), "Show context menu");
    }
    ActionMap actionMap =
      ((JComponent)instance.getContentPane()).getActionMap();
    actionMap.put("Show context menu", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        KeyboardFocusManager focusManager =
          KeyboardFocusManager.getCurrentKeyboardFocusManager();
        // get the current focused component
        Component focusedComponent = focusManager.getFocusOwner();
        if (focusedComponent != null) {
          Point menuLocation = null;
          Rectangle selectionRectangle = null;
          if (focusedComponent instanceof JTable
          && ((JTable)focusedComponent).getSelectedRowCount() > 0) {
            // selection in a JTable
            JTable table = (JTable)focusedComponent;
            selectionRectangle = table.getCellRect(
              table.getSelectionModel().getLeadSelectionIndex(),
              table.convertColumnIndexToView(table.getSelectedColumn()),
              false);
          } else if (focusedComponent instanceof JTree
          && ((JTree)focusedComponent).getSelectionCount() > 0) {
            // selection in a JTree
            JTree tree = (JTree)focusedComponent;
            selectionRectangle = tree.getRowBounds(
              tree.getSelectionModel().getLeadSelectionRow());
          } else {
            // for other component set the menu location at the top left corner
            menuLocation = new Point(focusedComponent.getX()-1,
                                     focusedComponent.getY()-1);
          }
          if (menuLocation == null) {
            // menu location at the bottom left of the JTable or JTree
            menuLocation = new Point(
              new Double(selectionRectangle.getMinX()+1).intValue(),
              new Double(selectionRectangle.getMaxY()-1).intValue());
          }

          // generate a right/button 3/popup menu mouse click
          focusedComponent.dispatchEvent(
            new MouseEvent(focusedComponent, MouseEvent.MOUSE_PRESSED,
              e.getWhen(), MouseEvent.BUTTON3_DOWN_MASK,
              menuLocation.x, menuLocation.y,
              1, true, MouseEvent.BUTTON3));
        }
      }
    });

    mainTabbedPane.getModel().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        // find the handle in the resources tree for the main view
        JComponent largeView =
          (JComponent)mainTabbedPane.getSelectedComponent();
        Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
        boolean done = false;
        DefaultMutableTreeNode node = resourcesTreeRoot;
        while(!done && nodesEnum.hasMoreElements()) {
          node = (DefaultMutableTreeNode)nodesEnum.nextElement();
          done =
            node.getUserObject() instanceof Handle
              && ((Handle)node.getUserObject()).viewsBuilt()
              && ((Handle)node.getUserObject()).getLargeView() == largeView;
        }
        ActionMap actionMap =
          ((JComponent)instance.getContentPane()).getActionMap();
        if(done) {
          Handle handle = (Handle)node.getUserObject();
          if (Gate.getUserConfig().getBoolean(
            MainFrame.class.getName()+".viewselecttree")) {
            // synchronise the selection in the tabbed pane with
            // the one in the resources tree
            TreePath nodePath = new TreePath(node.getPath());
            resourcesTree.setSelectionPath(nodePath);
            resourcesTree.scrollPathToVisible(nodePath);
          }
          lowerScroll.getViewport().setView(handle.getSmallView());

          // redefine MainFrame actionMaps for the selected tab
          JComponent resource =
            (JComponent)mainTabbedPane.getSelectedComponent();
          actionMap.put("Close resource",
            resource.getActionMap().get("Close resource"));
          actionMap.put("Close recursively",
            resource.getActionMap().get("Close recursively"));
          actionMap.put("Hide", new CloseViewAction(handle));
          actionMap.put("Hide all", new HideAllAction());
          actionMap.put("Save As XML",
            resource.getActionMap().get("Save As XML"));
        }
        else {
          // the selected item is not a resource (maybe the log area?)
          lowerScroll.getViewport().setView(null);
          // disabled actions on the selected tabbed pane
          actionMap.put("Close resource", null);
          actionMap.put("Close recursively", null);
          actionMap.put("Hide", null);
          actionMap.put("Hide all", null);
          actionMap.put("Save As XML", null);
        }
      }
    });

    mainTabbedPane.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        processMouseEvent(e);
      }
      public void mouseReleased(MouseEvent e) {
        processMouseEvent(e);
      }
      protected void processMouseEvent(MouseEvent e){
        if(e.isPopupTrigger()) {
          int index = mainTabbedPane.getIndexAt(e.getPoint());
          if(index != -1) {
            JComponent view = (JComponent)mainTabbedPane.getComponentAt(index);
            Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
            boolean done = false;
            DefaultMutableTreeNode node = resourcesTreeRoot;
            while(!done && nodesEnum.hasMoreElements()) {
              node = (DefaultMutableTreeNode)nodesEnum.nextElement();
              done = node.getUserObject() instanceof Handle
                  && ((Handle)node.getUserObject()).viewsBuilt()
                  && ((Handle)node.getUserObject()).getLargeView() == view;
            }
            if(done) {
              Handle handle = (Handle)node.getUserObject();
              JPopupMenu popup = handle.getPopup();

              // add a hide action
              CloseViewAction cva = new CloseViewAction(handle);
              XJMenuItem menuItem = new XJMenuItem(cva, MainFrame.this);
              popup.insert(menuItem, 0);

              // add a hide all action
              if (mainTabbedPane.getTabCount() > 2) {
                HideAllAction haa = new HideAllAction();
                menuItem = new XJMenuItem(haa, MainFrame.this);
                popup.insert(menuItem, 1);
              }

              popup.show(mainTabbedPane, e.getX(), e.getY());
            }
          }
        }
      }
    });

    addComponentListener(new ComponentAdapter() {
      public void componentShown(ComponentEvent e) {
        leftSplit.setDividerLocation(0.7);
      }
      public void componentResized(ComponentEvent e) {
        // resize proportionally the status bar elements
        int width = MainFrame.this.getWidth();
        statusBar.setPreferredSize(new Dimension(width*65/100,
          statusBar.getPreferredSize().height));
        progressBar.setPreferredSize(new Dimension(width*20/100,
          progressBar.getPreferredSize().height));
        progressBar.setMinimumSize(new Dimension(80, 0));
        globalProgressBar.setPreferredSize(new Dimension(width*10/100,
          globalProgressBar.getPreferredSize().height));
        globalProgressBar.setMinimumSize(new Dimension(80, 0));
      }
    });

    if(isShellSlacGIU) {
      mainSplit.setDividerSize(0);
      mainSplit.getTopComponent().setVisible(false);
      mainSplit.getTopComponent().addComponentListener(new ComponentAdapter() {
        public void componentHidden(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
          mainSplit.setDividerLocation(0);
        }

        public void componentResized(ComponentEvent e) {
          mainSplit.setDividerLocation(0);
        }

        public void componentShown(ComponentEvent e) {
          mainSplit.setDividerLocation(0);
        }
      });
    } // if

    // blink the messages tab when new information is displayed
    logArea.getDocument().addDocumentListener(
      new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
        }

        protected void changeOccured() {
          logHighlighter.highlight();
        }
      });

    logArea.addPropertyChangeListener("document", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        // add the document listener
        logArea.getDocument().addDocumentListener(
          new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
              changeOccured();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
              changeOccured();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
              changeOccured();
            }

            protected void changeOccured() {
              logHighlighter.highlight();
            }
          });
      }
    });

    Gate.listeners.put("gate.event.StatusListener", MainFrame.this);
    Gate.listeners.put("gate.event.ProgressListener", MainFrame.this);
    if(Gate.runningOnMac()) {
      // mac-specific initialisation
      initMacListeners();
    }
  }// protected void initListeners()

  /**
   * Set up the handlers to support the Macintosh Application menu. This
   * makes the About, Quit and Preferences menu items map to their
   * equivalents in GATE. If an exception occurs during this process we
   * print a warning.
   */
  protected void initMacListeners() {
    // What this method effectively does is:
    //
    // com.apple.eawt.Application app = Application.getApplication();
    // app.addApplicationListener(new ApplicationAdapter() {
    // public void handleAbout(ApplicationEvent e) {
    // e.setHandled(true);
    // new HelpAboutAction().actionPerformed(null);
    // }
    // public void handleQuit(ApplicationEvent e) {
    // e.setHandled(false);
    // new ExitGateAction().actionPerformed(null);
    // }
    // public void handlePreferences(ApplicationEvent e) {
    // e.setHandled(true);
    // optionsDialog.showDialog();
    // optionsDialog.dispose();
    // }
    // });
    //
    // app.setEnabledPreferencesMenu(true);
    //
    // except that it does it all by reflection so as not to
    // compile-time
    // depend on Apple classes.
    try {
      // load the Apple classes
      final Class eawtApplicationClass =
        Gate.getClassLoader().loadClass("com.apple.eawt.Application");
      final Class eawtApplicationListenerInterface =
        Gate.getClassLoader().loadClass("com.apple.eawt.ApplicationListener");
      final Class eawtApplicationEventClass =
        Gate.getClassLoader().loadClass("com.apple.eawt.ApplicationEvent");

      // method used in the InvocationHandler
      final Method appEventSetHandledMethod =
        eawtApplicationEventClass.getMethod("setHandled", boolean.class);

      // Invocation handler used to process Apple application events
      InvocationHandler handler = new InvocationHandler() {
        private Action aboutAction = new HelpAboutAction();

        private Action exitAction = new ExitGateAction();

        public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
          Object appEvent = args[0];
          if("handleAbout".equals(method.getName())) {
            appEventSetHandledMethod.invoke(appEvent, Boolean.TRUE);
            aboutAction.actionPerformed(null);
          }
          else if("handleQuit".equals(method.getName())) {
            appEventSetHandledMethod.invoke(appEvent, Boolean.FALSE);
            exitAction.actionPerformed(null);
          }
          else if("handlePreferences".equals(method.getName())) {
            appEventSetHandledMethod.invoke(appEvent, Boolean.TRUE);
            optionsDialog.showDialog();
            optionsDialog.dispose();
          }

          return null;
        }
      };

      // Create an ApplicationListener proxy instance
      Object applicationListenerObject =
        Proxy.newProxyInstance(Gate.getClassLoader(),
          new Class[]{eawtApplicationListenerInterface}, handler);

      // get hold of the Application object
      Method getApplicationMethod =
        eawtApplicationClass.getMethod("getApplication");
      Object applicationObject = getApplicationMethod.invoke(null);

      // enable the preferences menu item
      Method setEnabledPreferencesMenuMethod =
        eawtApplicationClass.getMethod("setEnabledPreferencesMenu",
          boolean.class);
      setEnabledPreferencesMenuMethod.invoke(applicationObject, Boolean.TRUE);

      // Register our proxy instance as an ApplicationListener
      Method addApplicationListenerMethod =
        eawtApplicationClass.getMethod("addApplicationListener",
          eawtApplicationListenerInterface);
      addApplicationListenerMethod.invoke(applicationObject,
        applicationListenerObject);
    }
    catch(Throwable error) {
      // oh well, we tried
      String message =
        "There was a problem setting up the Mac "
        + "application\nmenu.  Your options/session will not be saved if "
        + "you exit\nwith \u2318Q, use the close button at the top-left"
        + "corner\nof this window instead.";
      alertButton.setAction(new AlertAction(error, message, null));
    }
  }

  public void progressChanged(int i) {
    // progressBar.setStringPainted(true);
    int oldValue = progressBar.getValue();
    // if((!stopAction.isEnabled()) &&
    // (Gate.getExecutable() != null)){
    // stopAction.setEnabled(true);
    // SwingUtilities.invokeLater(new Runnable(){
    // public void run(){
    // southBox.add(stopBtn, 0);
    // }
    // });
    // }
    if(!animator.isActive()) animator.activate();
    if(oldValue != i) {
      SwingUtilities.invokeLater(new ProgressBarUpdater(i));
    }
  }

  /**
   * Called when the process is finished.
   *
   */
  public void processFinished() {
    // progressBar.setStringPainted(false);
    // if(stopAction.isEnabled()){
    // stopAction.setEnabled(false);
    // SwingUtilities.invokeLater(new Runnable(){
    // public void run(){
    // southBox.remove(stopBtn);
    // }
    // });
    // }
    SwingUtilities.invokeLater(new ProgressBarUpdater(0));
    SwingUtilities.invokeLater(new Runnable() { public void run() {
      globalProgressBar.setVisible(false);
    }});
    animator.deactivate();
  }
  
  /**
   * Regular expression pattern for the "Start running" message.
   */
  private static final Pattern START_RUNNING_PATTERN =
    Pattern.compile("Start running .* on (\\d+) documents?");

  public void statusChanged(String text) {
    SwingUtilities.invokeLater(new StatusBarUpdater(text));
    if (text != null) {
      Matcher m = START_RUNNING_PATTERN.matcher(text);
      if (m.matches()) {
        // get the corpus size from the status text
        final int corpusSize = Integer.valueOf(m.group(1));
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          // initialise the progress bar
          globalProgressBar.setMaximum(corpusSize);
          globalProgressBar.setValue(0);
          globalProgressBar.setString("0/" + corpusSize);
          globalProgressBar.setVisible(true);
        }});
      } else if (text.startsWith("Finished running ")) {
        SwingUtilities.invokeLater(new Runnable() { public void run() {
          // update the progress bar with one document processed
          globalProgressBar.setValue(globalProgressBar.getValue() + 1);
          globalProgressBar.setString(globalProgressBar.getValue() + "/"
            + globalProgressBar.getMaximum());
        }});
      }
    }
  }

  public void resourceLoaded(CreoleEvent e) {
    final Resource res = e.getResource();
    if(Gate.getHiddenAttribute(res.getFeatures())
      || res instanceof VisualResource) return;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        NameBearerHandle handle = new NameBearerHandle(res, MainFrame.this);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
        if(res instanceof Controller) {
          resourcesTreeModel.insertNodeInto(node, applicationsRoot, 0);
        } else if(res instanceof ProcessingResource) {
          resourcesTreeModel.insertNodeInto(node, processingResourcesRoot, 0);
        }
        else if(res instanceof LanguageResource) {
          resourcesTreeModel.insertNodeInto(node, languageResourcesRoot, 0);
        }

        handle.addProgressListener(MainFrame.this);
        handle.addStatusListener(MainFrame.this);
      }
    });

    // JPopupMenu popup = handle.getPopup();
    //
    // // Create a CloseViewAction and a menu item based on it
    // CloseViewAction cva = new CloseViewAction(handle);
    // XJMenuItem menuItem = new XJMenuItem(cva, this);
    // // Add an accelerator ATL+F4 for this action
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(
    // KeyEvent.VK_H, ActionEvent.CTRL_MASK));
    // popup.insert(menuItem, 1);
    // popup.insert(new JPopupMenu.Separator(), 2);
    //
    // popup.insert(new XJMenuItem(
    // new RenameResourceAction(
    // new TreePath(resourcesTreeModel.getPathToRoot(node))),
    // MainFrame.this) , 3);
    //
    // // Put the action command in the component's action map
    // if (handle.getLargeView() != null)
    // handle.getLargeView().getActionMap().put("Hide current
    // view",cva);
    //
  }// resourceLoaded();

  public void resourceUnloaded(CreoleEvent e) {
    final Resource res = e.getResource();
    if(Gate.getHiddenAttribute(res.getFeatures())) return;
    Runnable runner = new Runnable() {
      public void run() {
        DefaultMutableTreeNode node;
        DefaultMutableTreeNode parent = null;
        if(res instanceof Controller) {
          parent = applicationsRoot;
        }else if(res instanceof ProcessingResource) {
          parent = processingResourcesRoot;
        }
        else if(res instanceof LanguageResource) {
          parent = languageResourcesRoot;
        }
        if(parent != null) {
          Enumeration children = parent.children();
          while(children.hasMoreElements()) {
            node = (DefaultMutableTreeNode)children.nextElement();
            if(((NameBearerHandle)node.getUserObject()).getTarget() == res) {
              resourcesTreeModel.removeNodeFromParent(node);
              Handle handle = (Handle)node.getUserObject();
              if(handle.viewsBuilt()) {
                if(mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1)
                  mainTabbedPane.remove(handle.getLargeView());
                if(lowerScroll.getViewport().getView() == handle.getSmallView())
                  lowerScroll.getViewport().setView(null);
              }
              handle.cleanup();
              return;
            }
          }
        }
      }
    };
    SwingUtilities.invokeLater(runner);
  }

  /** Called when a {@link gate.DataStore} has been opened */
  public void datastoreOpened(CreoleEvent e) {
    DataStore ds = e.getDatastore();
    if(ds.getName() == null || ds.getName().length() == 0){
      String name = ds.getStorageUrl();
      StringBuilder nameBuilder = new StringBuilder();
      //quick and dirty FSA
      int state = 0;
      for(int i = name.length() -1; i >= 0 && state != 2 ; i--){
        char currentChar = name.charAt(i);
        switch (state) {
          case 0:
            //consuming slashes at the end
            if(currentChar == '/'){
              //consume
            }else{
              //we got the first non-slash char
              state = 1;
              nameBuilder.insert(0, currentChar);
            }
            break;
          case 1:
            //eating up name chars
            if(currentChar == '/'){
              //we're done!
              state = 2;
            }else{
              //we got a non-slash char
              nameBuilder.insert(0, currentChar);
            }
            break;
          default:
            throw new LuckyException("A phanthom state of things!");
        }
      }
      if(nameBuilder.length() > 0) name = nameBuilder.toString();
      ds.setName(name);
    }

    NameBearerHandle handle = new NameBearerHandle(ds, MainFrame.this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
    resourcesTreeModel.insertNodeInto(node, datastoresRoot, 0);
    handle.addProgressListener(MainFrame.this);
    handle.addStatusListener(MainFrame.this);

    // JPopupMenu popup = handle.getPopup();
    // popup.addSeparator();
    // // Create a CloseViewAction and a menu item based on it
    // CloseViewAction cva = new CloseViewAction(handle);
    // XJMenuItem menuItem = new XJMenuItem(cva, this);
    // // Add an accelerator ATL+F4 for this action
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(
    // KeyEvent.VK_H, ActionEvent.CTRL_MASK));
    // popup.add(menuItem);
    // // Put the action command in the component's action map
    // if (handle.getLargeView() != null)
    // handle.getLargeView().getActionMap().put("Hide current
    // view",cva);
  }// datastoreOpened();

  /** Called when a {@link gate.DataStore} has been created */
  public void datastoreCreated(CreoleEvent e) {
    datastoreOpened(e);
  }

  /** Called when a {@link gate.DataStore} has been closed */
  public void datastoreClosed(CreoleEvent e) {
    DataStore ds = e.getDatastore();
    DefaultMutableTreeNode node;
    DefaultMutableTreeNode parent = datastoresRoot;
    if(parent != null) {
      Enumeration children = parent.children();
      while(children.hasMoreElements()) {
        node = (DefaultMutableTreeNode)children.nextElement();
        if(((NameBearerHandle)node.getUserObject()).getTarget() == ds) {
          resourcesTreeModel.removeNodeFromParent(node);
          NameBearerHandle handle = (NameBearerHandle)node.getUserObject();

          if(handle.viewsBuilt()
            && mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1) {
            mainTabbedPane.remove(handle.getLargeView());
          }
          if(lowerScroll.getViewport().getView() == handle.getSmallView()) {
            lowerScroll.getViewport().setView(null);
          }
          return;
        }
      }
    }
  }

  public void resourceRenamed(Resource resource, String oldName, String newName) {
    //first find the handle for the renamed resource
    Handle handle = findHandleForResource(resource);
    if(handle != null){
      //next see if there is a tab for this resource and rename it
      for(int i = 0; i < mainTabbedPane.getTabCount(); i++) {
        if(mainTabbedPane.getTitleAt(i).equals(oldName) &&
           mainTabbedPane.getComponentAt(i) == handle.getLargeView()) {
          mainTabbedPane.setTitleAt(i, newName);
          return;
        }
      }
    }
  }

  /**
   * Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e) {
    if(e.getID() == WindowEvent.WINDOW_CLOSING) {
      new ExitGateAction().actionPerformed(null);
    }
    super.processWindowEvent(e);
  }// processWindowEvent(WindowEvent e)

  /**
   * Returns the listeners map, a map that holds all the listeners that
   * are singletons (e.g. the status listener that updates the status
   * bar on the main frame or the progress listener that updates the
   * progress bar on the main frame). The keys used are the class names
   * of the listener interface and the values are the actual listeners
   * (e.g "gate.event.StatusListener" -> this). The returned map is the
   * actual data member used to store the listeners so any changes in
   * this map will be visible to everyone.
   * @return the listeners map
   * @deprecated Use {@link Gate#getListeners()} instead
   */
  public static java.util.Map<String, EventListener> getListeners() {
    return Gate.getListeners();
  }

  public static java.util.Collection<Component> getGuiRoots() {
    return guiRoots;
  }

  /**
   * This method will lock all input to the gui by means of a modal
   * dialog. If Gate is not currently running in GUI mode this call will
   * be ignored. A call to this method while the GUI is locked will
   * cause the GUI to be unlocked and then locked again with the new
   * message. If a message is provided it will show in the dialog.
   *
   * @param message the message to be displayed while the GUI is locked
   */
  public synchronized static void lockGUI(final String message) {
    // check whether GUI is up
    if(getGuiRoots() == null || getGuiRoots().isEmpty()) return;
    // if the GUI is locked unlock it so we can show the new message
    unlockGUI();

    // this call needs to return so we'll show the dialog from a
    // different thread
    // the Swing thread sounds good for that
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
     // build the dialog contents
        Object[] options = new Object[]{new JButton(new StopAction())};
        JOptionPane pane =
          new JOptionPane(message, JOptionPane.WARNING_MESSAGE,
            JOptionPane.DEFAULT_OPTION, null, options, null);

        // build the dialog
        Component parentComp = (Component)((ArrayList)getGuiRoots()).get(0);
        JDialog dialog;
        Window parentWindow;
        if(parentComp instanceof Window)
          parentWindow = (Window)parentComp;
        else parentWindow = SwingUtilities.getWindowAncestor(parentComp);
        if(parentWindow instanceof Frame) {
          dialog = new JDialog((Frame)parentWindow, "Please wait", true) {
            private static final long serialVersionUID = 1L;
            protected void processWindowEvent(WindowEvent e) {
              if(e.getID() == WindowEvent.WINDOW_CLOSING) {
                getToolkit().beep();
              }
            }
          };
        }
        else if(parentWindow instanceof Dialog) {
          dialog = new JDialog((Dialog)parentWindow, "Please wait", true) {
            private static final long serialVersionUID = 1L;
            protected void processWindowEvent(WindowEvent e) {
              if(e.getID() == WindowEvent.WINDOW_CLOSING) {
                getToolkit().beep();
              }
            }
          };
        }
        else {
          dialog = new JDialog(JOptionPane.getRootFrame(), "Please wait", true) {
            private static final long serialVersionUID = 1L;
            protected void processWindowEvent(WindowEvent e) {
              if(e.getID() == WindowEvent.WINDOW_CLOSING) {
                getToolkit().beep();
              }
            }
          };
        }
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(pane, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(parentComp);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        guiLock = dialog;

        guiLock.setVisible(true);
      }
    });

    // this call should not return until the dialog is up to ensure
    // proper
    // sequentiality for lock - unlock calls
    while(guiLock == null || !guiLock.isShowing()) {
      try {
        Thread.sleep(100);
      }
      catch(InterruptedException ie) {
        log.debug("Interrupted sleep when the GUI is locked.", ie);
      }
    }
  }

  public synchronized static void unlockGUI() {
    // check whether GUI is up
    if(getGuiRoots() == null || getGuiRoots().isEmpty()) return;

    if(guiLock != null) {
      guiLock.setVisible(false);
      // completely dispose the dialog (causes it to disappear even if
      // displayed on a non-visible virtual display on Linux)
      // fix for bug 1369096
      // (http://sourceforge.net/tracker/index.php?func=detail&aid=1369096&group_id=143829&atid=756796)
      guiLock.dispose();
    }
    guiLock = null;
  }

  /** Flag to protect Frame title to be changed */
  private boolean titleChangable = false;

  public void setTitleChangable(boolean isChangable) {
    titleChangable = isChangable;
  } // setTitleChangable(boolean isChangable)

  /** Override to avoid Protege to change Frame title */
  public synchronized void setTitle(String title) {
    if(titleChangable) {
      super.setTitle(title);
    } // if
  } // setTitle(String title)

  /**
   * Method is used in NewDSAction
   * @return the new datastore or null if an error occurs
   */
  protected DataStore createSerialDataStore() {
    DataStore ds = null;

    // get the URL (a file in this case)
    fileChooser.setDialogTitle("Please create a new empty directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
    fileChooser.setResource("gate.persist.SerialDataStore");
    if(fileChooser.showOpenDialog(MainFrame.this)
      == JFileChooser.APPROVE_OPTION) {
      try {
        URL dsURL = fileChooser.getSelectedFile().toURI().toURL();
        ds =
          Factory.createDataStore("gate.persist.SerialDataStore", dsURL
            .toExternalForm());
      }
      catch(MalformedURLException mue) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Invalid location for the datastore\n " + mue.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      }
      catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Datastore creation error!\n " + pe.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      } // catch
    } // if

    return ds;
  } // createSerialDataStore()

  /**
   * Method is used in OpenDSAction
   * @return the opened datastore or null if an error occurs
   */
  protected DataStore openSerialDataStore() {
    DataStore ds = null;

    // get the URL (a file in this case)
    fileChooser.setDialogTitle("Select the datastore directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
    fileChooser.setResource("gate.persist.SerialDataStore");
    if(fileChooser.showOpenDialog(MainFrame.this)
      == JFileChooser.APPROVE_OPTION) {
      try {
        URL dsURL = fileChooser.getSelectedFile().toURI().toURL();
        ds =
          Factory.openDataStore("gate.persist.SerialDataStore", dsURL
            .toExternalForm());
      }
      catch(MalformedURLException mue) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Invalid location for the datastore\n " + mue.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      }
      catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Datastore opening error!\n " + pe.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      } // catch
    } // if

    return ds;
  } // openSerialDataStore()

  /**
   * Method is used in ....OpenDSAction
   * @return the opened datastore or null if an error occurs
   */
  protected DataStore openDocServiceDataStore() {
    DataStore ds = null;
    try {
      String DSLocation =
        JOptionPane.showInputDialog(MainFrame.this,
          "Enter document service URL",
          "http://localhost:8080/docservice/services/docservice");
      // System.out.println("DEBUG: MainFrame.openDocServiceDataStore()
      // DSLocation='" + DSLocation + "'");
      ds =
        Factory.openDataStore("gleam.docservice.gate.DocServiceDataStore",
          DSLocation);
    }
    catch(Exception error) {
      String message = "Error when opening the Datastore.";
      alertButton.setAction(new AlertAction(error, message, null));
    }
    return ds;
  } // openWSDataStore()

  /*
   * synchronized void showWaitDialog() { Point location =
   * getLocationOnScreen(); location.translate(10, getHeight() -
   * waitDialog.getHeight() - southBox.getHeight() - 10);
   * waitDialog.setLocation(location); waitDialog.showDialog(new
   * Component[]{}); }
   *
   * synchronized void hideWaitDialog() { waitDialog.goAway(); }
   */

  /*
   * class NewProjectAction extends AbstractAction { public
   * NewProjectAction(){ super("New Project", new
   * ImageIcon(MainFrame.class.getResource(
   * "/gate/resources/img/newProject")));
   * putValue(SHORT_DESCRIPTION,"Create a new project"); } public void
   * actionPerformed(ActionEvent e){ fileChooser.setDialogTitle("Select
   * new project file");
   * fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
   * if(fileChooser.showOpenDialog(parentFrame) ==
   * fileChooser.APPROVE_OPTION){ ProjectData pData = new
   * ProjectData(fileChooser.getSelectedFile(), parentFrame);
   * addProject(pData); } } }
   */

  /** This class represent an action which brings up the Annot Diff tool */
  class NewAnnotDiffAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public NewAnnotDiffAction() {
      super("Annotation Diff", getIcon("annotation-diff"));
      putValue(SHORT_DESCRIPTION,
        "Compare annotations and features in one or two documents");
    }
    public void actionPerformed(ActionEvent e) {
      // find the handle in the resource tree for the displayed view
      Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
      DefaultMutableTreeNode node;
      Handle handle = null;
      while(nodesEnum.hasMoreElements()) {
        node = (DefaultMutableTreeNode)nodesEnum.nextElement();
        if ((node.getUserObject() instanceof Handle)
         && (mainTabbedPane.getSelectedComponent().equals(
            ((Handle)node.getUserObject()).getLargeView()))) {
          handle = (Handle)node.getUserObject();
          break;
        }
      }
      String documentName = null;
      if(handle != null
      && handle.getTarget() instanceof Document) {
        documentName = ((Document)handle.getTarget()).getName();
      }
      AnnotationDiffGUI frame;
      if (documentName != null) {
        // use the document displayed in the view to compute the differences
        frame = new AnnotationDiffGUI("Annotation Diff Tool",
          documentName, documentName, null, null, null, null);
      } else {
        frame = new AnnotationDiffGUI("Annotation Diff Tool");
      }
      frame.pack();
      frame.setLocationRelativeTo(MainFrame.this);
      frame.setVisible(true);
    }
  }

  /**
   * This class represent an action which brings up the corpus
   * evaluation tool
   */
  class NewCorpusEvalAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public NewCorpusEvalAction() {
      super("Default Mode");
      putValue(SHORT_DESCRIPTION, "Compares stored processed set with current" +
        " processed set and human-annotated set");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          fileChooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setResource(CorpusBenchmarkTool.class.getName());
          int state = fileChooser.showOpenDialog(MainFrame.this);
          File startDir = fileChooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          fileChooser.setDialogTitle(
            "Please select the application that you want to run");
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setResource(
            CorpusBenchmarkTool.class.getName() + ".application");
          state = fileChooser.showOpenDialog(MainFrame.this);
          File testApp = fileChooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || testApp == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setApplicationFile(testApp);
          theTool.setVerboseMode(verboseModeItem.isSelected());

          Out.prln("Please wait while GATE tools are initialised.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.printStatistics();

          Out.prln("<BR>Overall average precision: "
            + theTool.getPrecisionAverage());
          Out.prln("<BR>Overall average recall: " + theTool.getRecallAverage());
          Out.prln("<BR>Overall average fMeasure : "
            + theTool.getFMeasureAverage());
          Out.prln("<BR>Finished!");
          theTool.unloadPRs();
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "NewCorpusEvalAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class NewCorpusEvalAction

  /**
   * This class represent an action which brings up the corpus
   * evaluation tool
   */
  class StoredMarkedCorpusEvalAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public StoredMarkedCorpusEvalAction() {
      super("Human Marked Against Stored Processing Results");
      putValue(SHORT_DESCRIPTION,
        "Compares stored processed set with human-annotated set");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          fileChooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setResource(CorpusBenchmarkTool.class.getName());
          int state = fileChooser.showOpenDialog(MainFrame.this);
          File startDir = fileChooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setMarkedStored(true);
          theTool.setVerboseMode(verboseModeItem.isSelected());
          // theTool.setMarkedDS(
          // MainFrame.this.datastoreModeCorpusEvalToolAction.isDatastoreMode());

          Out
            .prln("Evaluating human-marked documents against pre-stored results.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.printStatistics();

          Out.prln("<BR>Overall average precision: "
            + theTool.getPrecisionAverage());
          Out.prln("<BR>Overall average recall: " + theTool.getRecallAverage());
          Out.prln("<BR>Overall average fMeasure : "
            + theTool.getFMeasureAverage());
          Out.prln("<BR>Finished!");
          theTool.unloadPRs();
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "StoredMarkedCorpusEvalAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class StoredMarkedCorpusEvalActionpusEvalAction

  /**
   * This class represent an action which brings up the corpus
   * evaluation tool
   */
  class CleanMarkedCorpusEvalAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public CleanMarkedCorpusEvalAction() {
      super("Human Marked Against Current Processing Results");
      putValue(SHORT_DESCRIPTION,
        "Compares current processed set with human-annotated set");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          fileChooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setResource(CorpusBenchmarkTool.class.getName());
          int state = fileChooser.showOpenDialog(MainFrame.this);
          File startDir = fileChooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          fileChooser.setDialogTitle(
            "Please select the application that you want to run");
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setResource(
            CorpusBenchmarkTool.class.getName() + ".application");
          state = fileChooser.showOpenDialog(MainFrame.this);
          File testApp = fileChooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || testApp == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setApplicationFile(testApp);
          theTool.setMarkedClean(true);
          theTool.setVerboseMode(verboseModeItem.isSelected());

          Out.prln("Evaluating human-marked documents against current processing results.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.printStatistics();

          Out.prln("Overall average precision: "
            + theTool.getPrecisionAverage());
          Out.prln("Overall average recall: " + theTool.getRecallAverage());
          Out
            .prln("Overall average fMeasure : " + theTool.getFMeasureAverage());
          Out.prln("Finished!");
          theTool.unloadPRs();
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "CleanMarkedCorpusEvalAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class CleanMarkedCorpusEvalActionpusEvalAction

  /**
   * This class represent an action which brings up the corpus
   * evaluation tool
   */
  class GenerateStoredCorpusEvalAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public GenerateStoredCorpusEvalAction() {
      super("Store Corpus for Future Evaluation");
      putValue(SHORT_DESCRIPTION, "Store corpus for future evaluation");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          fileChooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setResource(CorpusBenchmarkTool.class.getName());
          int state = fileChooser.showOpenDialog(MainFrame.this);
          File startDir = fileChooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          fileChooser.setDialogTitle(
            "Please select the application that you want to run");
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setResource(
            CorpusBenchmarkTool.class.getName() + ".application");
          state = fileChooser.showOpenDialog(MainFrame.this);
          File testApp = fileChooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || testApp == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setApplicationFile(testApp);
          theTool.setGenerateMode(true);

          Out.prln("Processing and storing documents for future evaluation.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.unloadPRs();
          Out.prln("Finished!");
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "GenerateStoredCorpusEvalAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class GenerateStoredCorpusEvalAction

  /**
   * This class represent an action which brings up the corpus
   * evaluation tool
   */
  class VerboseModeCorpusEvalToolAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public VerboseModeCorpusEvalToolAction() {
      super("Verbose Mode");
    }// VerboseModeCorpusEvalToolAction

    public boolean isVerboseMode() {
      return verboseMode;
    }

    public void actionPerformed(ActionEvent e) {
      if(!(e.getSource() instanceof JCheckBoxMenuItem)) return;
      verboseMode = ((JCheckBoxMenuItem)e.getSource()).getState();
    }// actionPerformed();

    protected boolean verboseMode = false;
  }// class f

  /**
   * This class represent an action which brings up the corpus
   * evaluation tool
   */
  /*
   * class DatastoreModeCorpusEvalToolAction extends AbstractAction {
   * public DatastoreModeCorpusEvalToolAction() { super("Use a datastore
   * for human annotated texts"); putValue(SHORT_DESCRIPTION,"Use a
   * datastore for the human annotated texts"); }//
   * DatastoreModeCorpusEvalToolAction
   *
   * public boolean isDatastoreMode() {return datastoreMode;}
   *
   * public void actionPerformed(ActionEvent e) { if (! (e.getSource()
   * instanceof JCheckBoxMenuItem)) return; datastoreMode =
   * ((JCheckBoxMenuItem)e.getSource()).getState(); }//
   * actionPerformed(); protected boolean datastoreMode = false;
   * }//class DatastoreModeCorpusEvalToolListener
   */

  /**
   * Loads ANNIE with default parameters.
   */
  class LoadANNIEWithDefaultsAction extends AbstractAction implements
                                                          ANNIEConstants {
    private static final long serialVersionUID = 1L;

    public LoadANNIEWithDefaultsAction() {
      super("with Defaults");
      putValue(SHORT_DESCRIPTION, "Load ANNIE with default parameters");
      putValue(SMALL_ICON, getIcon("annie-application"));
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          lockGUI("ANNIE is being loaded...");
          try {
            long startTime = System.currentTimeMillis();

            // load ANNIE as an application from a gapp file
            PersistenceManager.loadObjectFromFile(new File(new File(
              Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR),
                ANNIEConstants.DEFAULT_FILE));

            long endTime = System.currentTimeMillis();
            statusChanged("ANNIE loaded in "
              + NumberFormat.getInstance().format(
                (double)(endTime - startTime) / 1000) + " seconds");
          }
          catch(Exception error) {
            String message =
              "There was an error when loading the ANNIE application.";
            alertButton.setAction(new AlertAction(error, message, null));
          }
          finally {
            unlockGUI();
          }
        }
      };
      Thread thread = new Thread(runnable, "LoadANNIEWithDefaultsAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }// class LoadANNIEWithDefaultsAction

  /**
   * Loads ANNIE without default parameters.
   */
  class LoadANNIEWithoutDefaultsAction extends AbstractAction implements
                                                             ANNIEConstants {
    private static final long serialVersionUID = 1L;

    public LoadANNIEWithoutDefaultsAction() {
      super("without Defaults");
      putValue(SHORT_DESCRIPTION, "Load ANNIE without default parameters");
      putValue(SMALL_ICON, getIcon("annie-application"));
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          lockGUI("ANNIE is being loaded...");
          final SerialAnalyserController controller;
          try {
            // load ANNIE as an application from a gapp file
            controller = (SerialAnalyserController)
              PersistenceManager.loadObjectFromFile(new File(new File(
                Gate.getPluginsHome(), ANNIEConstants.PLUGIN_DIR),
                  ANNIEConstants.DEFAULT_FILE));

            statusChanged("ANNIE loaded!");
            unlockGUI();
          }
          catch(Exception error) {
            unlockGUI();
            String message =
              "There was an error when loading the ANNIE application.";
            alertButton.setAction(new AlertAction(error, message, null));
            return;
          }

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              List<ProcessingResource> prs =
                new ArrayList<ProcessingResource>(controller.getPRs());
              for(ProcessingResource pr : prs) {
                try {
                  SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                      // select last processing resource in resources tree
                      int selectedRow = resourcesTree.getRowForPath(
                        new TreePath(processingResourcesRoot.getPath()));
                      selectedRow += processingResourcesRoot.getChildCount();
                      resourcesTree.setSelectionRow(selectedRow);
                      resourcesTree.scrollRowToVisible(selectedRow);
                    }
                  });
                  // get the parameters for each ANNIE PR
                  ResourceData resData = Gate.getCreoleRegister()
                    .get(pr.getClass().getName());
                  if (resData == null) {
                    throw new ResourceInstantiationException(
                      pr.getName() + " was not possible to load.");
                  }
                  fileChooser.setResource(resData.getClassName());
                  if(newResourceDialog.show(resData, "Parameters for the new "
                    + resData.getName())) {
                    // add the PR with user parameters
                    controller.add((ProcessingResource)
                      Factory.createResource(pr.getClass().getName(),
                      newResourceDialog.getSelectedParameters()));
                  }
                  // remove the PR with default parameters
                  Factory.deleteResource(pr);
                }
                catch(ResourceInstantiationException error) {
                  String message = "There was an error when creating"
                    + " the resource: " + pr.getName() + ".";
                  alertButton.setAction(new AlertAction(error, message, null));
                }
              } // for(Object loadedPR : loadedPRs)
            }
          });
        }
      };
      Thread thread = new Thread(runnable, "LoadANNIEWithoutDefaultsAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }// class LoadANNIEWithoutDefaultsAction

  /**
   * Loads the application.
   */
  class LoadApplicationAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private String pluginDir = null;

    private String applicationFile = null;

    public LoadApplicationAction(String caption, String pluginDir,
            String applicationFile) {
      super("Load " + pluginDir + " System");
      this.pluginDir = pluginDir;
      this.applicationFile = applicationFile;
      putValue(SHORT_DESCRIPTION, "Load " + pluginDir
              + " with default parameters");
      putValue(SMALL_ICON, getIcon("open-application"));
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          lockGUI(pluginDir + " is being loaded...");
          try {
            long startTime = System.currentTimeMillis();

            // load LingPipe as an application from a gapp file
            PersistenceManager.loadObjectFromFile(new File(new File(Gate
                    .getPluginsHome(), pluginDir), applicationFile));

            long endTime = System.currentTimeMillis();
            statusChanged(pluginDir
                    + " loaded in "
                    + NumberFormat.getInstance().format(
                            (double)(endTime - startTime) / 1000) + " seconds");
          } catch(Exception error) {
            String message =
                    "There was an error when loading the " + pluginDir
                            + " application.";
            alertButton.setAction(new AlertAction(error, message, null));
          } finally {
            unlockGUI();
          }
        }
      };
      Thread thread = new Thread(runnable, "LoadApplicationAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }// class LoadApplicationAction

  class NewBootStrapAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public NewBootStrapAction() {
      super("BootStrap Wizard", getIcon("application"));
      putValue(SHORT_DESCRIPTION, "Create a generic resource to be completed");
    }// NewBootStrapAction

    public void actionPerformed(ActionEvent e) {
      BootStrapDialog bootStrapDialog = new BootStrapDialog(MainFrame.this);
      bootStrapDialog.setVisible(true);
    }// actionPerformed();
  }// class NewBootStrapAction

  class ManagePluginsAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public ManagePluginsAction() {
      super("Manage CREOLE Plugins");
      putValue(SHORT_DESCRIPTION,
        "Load, unload, add and remove CREOLE plugins");
      putValue(SMALL_ICON, getIcon("creole-plugins"));
    }

    public void actionPerformed(ActionEvent e) {
      if(pluginManager == null) {
        pluginManager = new PluginManagerUI(MainFrame.this);
        // pluginManager.setLocationRelativeTo(MainFrame.this);
        pluginManager.setModal(true);
        getGuiRoots().add(pluginManager);
        pluginManager.pack();
        // size the window so that it doesn't go off-screen
        Dimension screenSize = /* Toolkit.getDefaultToolkit().getScreenSize(); */
        getGraphicsConfiguration().getBounds().getSize();
        Dimension dialogSize = pluginManager.getPreferredSize();
        int width =
          dialogSize.width > screenSize.width
            ? screenSize.width * 3 / 4
            : dialogSize.width;
        int height =
          dialogSize.height > screenSize.height
            ? screenSize.height * 3 / 4
            : dialogSize.height;
        pluginManager.setSize(width, height);
        pluginManager.validate();
        // center the window on screen
        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
        pluginManager.setLocation(x, y);
      }
      fileChooser.setResource(PluginManagerUI.class.getName());
      pluginManager.setVisible(true);
      // free resources after the dialog is hidden
      pluginManager.dispose();
    }
  }

  /**
   * TODO: delete this method as it has moved to {@link PluginManagerUI}
   * @deprecated
   */
  class LoadCreoleRepositoryAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public LoadCreoleRepositoryAction() {
      super("Load a CREOLE Repository");
    }

    public void actionPerformed(ActionEvent e) {
      Box messageBox = Box.createHorizontalBox();
      Box leftBox = Box.createVerticalBox();
      JTextField urlTextField = new JTextField(20);
      leftBox.add(new JLabel("Type an URL"));
      leftBox.add(urlTextField);
      messageBox.add(leftBox);

      messageBox.add(Box.createHorizontalStrut(10));
      messageBox.add(new JLabel("or"));
      messageBox.add(Box.createHorizontalStrut(10));

      class URLfromFileAction extends AbstractAction {
        private static final long serialVersionUID = 1L;
        URLfromFileAction(JTextField textField) {
          super(null, getIcon("open-file"));
          putValue(SHORT_DESCRIPTION, "Click to select a directory");
          this.textField = textField;
        }

        public void actionPerformed(ActionEvent e) {
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          fileChooser.setResource("gate.CreoleRegister");
          int result = fileChooser.showOpenDialog(MainFrame.this);
          if(result == JFileChooser.APPROVE_OPTION) {
            try {
              textField.setText(fileChooser.getSelectedFile().toURI().toURL()
                .toExternalForm());
            }
            catch(MalformedURLException mue) {
              throw new GateRuntimeException(mue.toString());
            }
          }
        }

        JTextField textField;
      } // class URLfromFileAction extends AbstractAction

      Box rightBox = Box.createVerticalBox();
      rightBox.add(new JLabel("Select a directory"));
      JButton fileBtn = new JButton(new URLfromFileAction(urlTextField));
      rightBox.add(fileBtn);
      messageBox.add(rightBox);

      // JOptionPane.showInputDialog(
      // MainFrame.this,
      // "Select type of Datastore",
      // "Gate", JOptionPane.QUESTION_MESSAGE,
      // null, names,
      // names[0]);

      int res =
        JOptionPane.showConfirmDialog(MainFrame.this, messageBox,
          "Enter an URL to the directory containig the "
            + "\"creole.xml\" file", JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE, null);
      if(res == JOptionPane.OK_OPTION) {
        try {
          URL creoleURL = new URL(urlTextField.getText());
          Gate.getCreoleRegister().registerDirectories(creoleURL);
        }
        catch(Exception error) {
          String message =
            "There was a problem when registering your CREOLE directory.";
          alertButton.setAction(new AlertAction(error, message, null));
        }
      }
    }
  }// class LoadCreoleRepositoryAction extends AbstractAction

  class NewResourceAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    /** Used for creation of resource menu item and creation dialog */
    ResourceData rData;

    public NewResourceAction(ResourceData rData) {
      super(rData.getName());
      putValue(SHORT_DESCRIPTION, rData.getComment());
      this.rData = rData;
      putValue(SMALL_ICON, getIcon(rData.getIcon()));
    } // NewResourceAction(ResourceData rData)

    public void actionPerformed(ActionEvent evt) {
      Runnable runnable = new Runnable() {
        public void run() {
          newResourceDialog.setTitle("Parameters for the new "
            + rData.getName());
          fileChooser.setResource(rData.getClassName());
          newResourceDialog.show(rData);
        }
      };
      SwingUtilities.invokeLater(runnable);
    } // actionPerformed
  } // class NewResourceAction extends AbstractAction

  static class StopAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public StopAction() {
      super(" Stop! ");
      putValue(SHORT_DESCRIPTION, "Stops the current action");
    }

    public boolean isEnabled() {
      return Gate.getExecutable() != null;
    }

    public void actionPerformed(ActionEvent e) {
      Executable ex = Gate.getExecutable();
      if(ex != null) ex.interrupt();
    }
  }

  /**
   * Method is used in NewDSAction
   * @return the new datastore or null if an error occurs
   */
  protected DataStore createSearchableDataStore() {
    try {
      JPanel mainPanel = new JPanel(new GridBagLayout());

      final JTextField dsLocation = new JTextField("", 20);
      final JTextField indexLocation = new JTextField("", 20);

      JTextField btat = new JTextField("Token", 20);
      btat.setToolTipText("Examples: Token, AnnotationSetName.Token, "
        + Constants.DEFAULT_ANNOTATION_SET_NAME + ".Token");
      JCheckBox createTokensAutomatically =
        new JCheckBox("Create Tokens Automatically");
      createTokensAutomatically.setSelected(true);
      JTextField iuat = new JTextField("", 20);
      iuat.setToolTipText("Examples: Sentence, AnnotationSetName.Sentence, "
        + Constants.DEFAULT_ANNOTATION_SET_NAME + ".Sentence");

      final List<String> inputASList = new ArrayList<String>();
      inputASList.add("Key");
      final JTextField inputAS = new JTextField("", 20);
      inputAS.setText("Key");
      inputAS.setEditable(false);
      JButton editInputAS = new JButton(getIcon("edit-list"));
      editInputAS.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          ListEditorDialog listEditor =
            new ListEditorDialog(instance, inputASList, "java.lang.String");
          List result = listEditor.showDialog();
          if(result != null) {
            inputASList.clear();
            inputASList.addAll(result);
            if(inputASList.size() > 0) {
              String text =
                inputASList.get(0) == null
                  ? Constants.DEFAULT_ANNOTATION_SET_NAME
                  : inputASList.get(0);
              for(int j = 1; j < inputASList.size(); j++) {
                text +=
                  ";"
                    + (inputASList.get(j) == null
                      ? Constants.DEFAULT_ANNOTATION_SET_NAME
                      : inputASList.get(j));
              }
              inputAS.setText(text);
            }
            else {
              inputAS.setText("");
            }
          }
        }
      });

      JComboBox asie = new JComboBox(new String[]{"include", "exclude"});
      inputAS.setToolTipText("Leave blank for indexing all annotation sets");

      final List<String> fteList = new ArrayList<String>();
      fteList.add("SpaceToken");
      fteList.add("Split");
      final JTextField fte = new JTextField("", 20);
      fte.setText("SpaceToken;Split");
      fte.setEditable(false);
      JButton editFTE = new JButton(getIcon("edit-list"));
      editFTE.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          ListEditorDialog listEditor =
            new ListEditorDialog(instance, fteList, "java.lang.String");
          List result = listEditor.showDialog();
          if(result != null) {
            fteList.clear();
            fteList.addAll(result);
            if(fteList.size() > 0) {
              String text =
                fteList.get(0) == null
                  ? Constants.DEFAULT_ANNOTATION_SET_NAME
                  : fteList.get(0);
              for(int j = 1; j < fteList.size(); j++) {
                text +=
                  ";"
                    + (fteList.get(j) == null
                      ? Constants.DEFAULT_ANNOTATION_SET_NAME
                      : fteList.get(j));
              }
              fte.setText(text);
            }
            else {
              fte.setText("");
            }
          }
        }
      });

      JComboBox ftie = new JComboBox(new String[]{"include", "exclude"});
      ftie.setSelectedIndex(1);
      fte.setToolTipText("Leave blank for inclusion of all features");

      JButton dsBrowse = new JButton(getIcon("open-file"));
      JButton indexBrowse = new JButton(getIcon("open-file"));
      dsBrowse.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          // first we need to ask for a new empty directory
          fileChooser.setDialogTitle(
            "Please create a new empty directory for datastore");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setResource("gate.DataStore.data");
          if(fileChooser.showOpenDialog(MainFrame.this)
            == JFileChooser.APPROVE_OPTION) {
            try {
              dsLocation.setText(fileChooser.getSelectedFile().toURI().toURL()
                .toExternalForm());
            }
            catch(Exception e) {
              dsLocation.setText("");
            }
          }
        }
      });

      indexBrowse.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          // first we need to ask for a new empty directory
          fileChooser.setDialogTitle(
            "Please create a new empty directory for datastore");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setResource("gate.DataStore.index");
          if(fileChooser.showOpenDialog(MainFrame.this)
            == JFileChooser.APPROVE_OPTION) {
            try {
              indexLocation.setText(fileChooser.getSelectedFile().toURI()
                .toURL().toExternalForm());
            }
            catch(Exception e) {
              indexLocation.setText("");
            }
          }
        }
      });

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 0;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Datastore URL:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 0;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(dsLocation, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 0;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(dsBrowse, constraints);
      dsBrowse.setBorderPainted(false);
      dsBrowse.setContentAreaFilled(false);

      // second row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 1;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Index Location:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 1;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(indexLocation, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 1;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(indexBrowse, constraints);
      indexBrowse.setBorderPainted(false);
      indexBrowse.setContentAreaFilled(false);

      // third row row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 2;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Annotation Sets:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 1;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(asie, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(inputAS, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(editInputAS, constraints);
      editInputAS.setBorderPainted(false);
      editInputAS.setContentAreaFilled(false);

      // fourth row row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 3;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Base Token Type:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 3;
      constraints.gridwidth = 5;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(btat, constraints);

      // fifth row
      constraints = new GridBagConstraints();
      constraints.gridx = 4;
      constraints.gridy = 4;
      constraints.gridwidth = 5;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(createTokensAutomatically, constraints);

      // sixth row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 5;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Index Unit Type:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 5;
      constraints.gridwidth = 5;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(iuat, constraints);

      // seventh row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 2;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Features:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 1;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(ftie, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(fte, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(editFTE, constraints);
      editFTE.setBorderPainted(false);
      editFTE.setContentAreaFilled(false);

      boolean validEntry = false;
      while(!validEntry) {
        int returnValue =
          JOptionPane.showOptionDialog(instance, mainPanel,
            "SearchableDataStore", JOptionPane.PLAIN_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION, getIcon("empty"),
            new String[]{"OK", "Cancel"}, "OK");
        if(returnValue == JOptionPane.OK_OPTION) {
          
          // sanity check parameters
          if(dsLocation.getText().equals(indexLocation.getText())) {
            JOptionPane.showMessageDialog(instance,
                    "Datastore and index may not be in the same directory",
                    "Error", JOptionPane.ERROR_MESSAGE);
          }
          else {
            validEntry = true;
            DataStore ds =
              Factory.createDataStore("gate.persist.LuceneDataStoreImpl",
                dsLocation.getText());
    
            // we need to set Indexer
            Class[] consParam = new Class[1];
            consParam[0] = URL.class;
            Constructor constructor =
              Class.forName("gate.creole.annic.lucene.LuceneIndexer", true,
                Gate.getClassLoader()).getConstructor(consParam);
            Object indexer =
              constructor.newInstance(new URL(indexLocation.getText()));
    
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(Constants.INDEX_LOCATION_URL, new URL(indexLocation
              .getText()));
            parameters.put(Constants.BASE_TOKEN_ANNOTATION_TYPE, btat.getText());
            parameters.put(Constants.INDEX_UNIT_ANNOTATION_TYPE, iuat.getText());
            parameters.put(Constants.CREATE_TOKENS_AUTOMATICALLY,
              createTokensAutomatically.isSelected());
    
            if(inputAS.getText().trim().length() > 0) {
              ArrayList<String> inputASList1 = new ArrayList<String>();
              String[] inputASArray = inputAS.getText().trim().split(";");
              if(inputASArray != null && inputASArray.length > 0) {
                inputASList1.addAll(Arrays.asList(inputASArray));
              }
              if(asie.getSelectedIndex() == 0) {
                // user has provided values for inclusion
                parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE,
                  inputASList1);
                parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE,
                  new ArrayList<String>());
              }
              else {
                // user has provided values for exclusion
                parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE,
                  inputASList1);
                parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE,
                  new ArrayList<String>());
              }
            }
            else {
              parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE,
                new ArrayList<String>());
              parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE,
                new ArrayList<String>());
            }
    
            if(fte.getText().trim().length() > 0) {
              ArrayList<String> fteList1 = new ArrayList<String>();
              String[] inputASArray = fte.getText().trim().split(";");
              if(inputASArray != null && inputASArray.length > 0) {
                fteList1.addAll(Arrays.asList(inputASArray));
              }
              if(ftie.getSelectedIndex() == 0) {
                // user has provided values for inclusion
                parameters.put(Constants.FEATURES_TO_INCLUDE, fteList1);
                parameters.put(Constants.FEATURES_TO_EXCLUDE,
                  new ArrayList<String>());
              }
              else {
                // user has provided values for exclusion
                parameters.put(Constants.FEATURES_TO_EXCLUDE, fteList1);
                parameters.put(Constants.FEATURES_TO_INCLUDE,
                  new ArrayList<String>());
              }
            }
            else {
              parameters
                .put(Constants.FEATURES_TO_EXCLUDE, new ArrayList<String>());
              parameters
                .put(Constants.FEATURES_TO_INCLUDE, new ArrayList<String>());
            }
    
            Class[] params = new Class[2];
            params[0] =
              Class.forName("gate.creole.annic.Indexer", true, Gate
                .getClassLoader());
            params[1] = Map.class;
            Method indexerMethod = ds.getClass().getMethod("setIndexer", params);
            indexerMethod.invoke(ds, indexer, parameters);
    
            // Class[] searchConsParams = new Class[0];
            Constructor searcherConst =
              Class.forName("gate.creole.annic.lucene.LuceneSearcher", true,
                Gate.getClassLoader()).getConstructor();
            Object searcher = searcherConst.newInstance();
            Class[] searchParams = new Class[1];
            searchParams[0] =
              Class.forName("gate.creole.annic.Searcher", true, Gate
                .getClassLoader());
            Method searcherMethod =
              ds.getClass().getMethod("setSearcher", searchParams);
            searcherMethod.invoke(ds, searcher);
            return ds;
          }
        }
        else {
          validEntry = true;
        }
      }
      return null;
    }
    catch(Exception e) {
      throw new GateRuntimeException(e);
    }
  } // createSearchableDataStore()

  /**
   * Method is used in OpenDSAction
   * @return the opened datastore or null if an error occurs
   */
  protected DataStore openSearchableDataStore() {
    DataStore ds = null;

    // get the URL (a file in this case)
    fileChooser.setDialogTitle("Select the datastore directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setResource("gate.DataStore.data");
    if(fileChooser.showOpenDialog(MainFrame.this)
      == JFileChooser.APPROVE_OPTION) {
      try {
        URL dsURL = fileChooser.getSelectedFile().toURI().toURL();
        ds =
          Factory.openDataStore("gate.persist.LuceneDataStoreImpl", dsURL
            .toExternalForm());
      }
      catch(MalformedURLException mue) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Invalid location for the datastore\n " + mue.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      }
      catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Datastore opening error!\n " + pe.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      } // catch
    } // if

    return ds;
  } // openSerialDataStore()

  class NewDSAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public NewDSAction() {
      super("Create Datastore");
      putValue(SHORT_DESCRIPTION, "Create a new datastore");
      putValue(SMALL_ICON, getIcon("datastore"));
    }

    public void actionPerformed(ActionEvent e) {
      Map<String,String> dsTypes = DataStoreRegister.getDataStoreClassNames();
      HashMap<String,String> dsTypeByName = new HashMap<String,String>();
      for(Map.Entry<String, String> entry : dsTypes.entrySet()) {
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()) {
        JLabel label = new JLabel("Select a type of Datastore:");
        final JList list = new JList(dsTypeByName.keySet().toArray());
        String initialSelection = Gate.getUserConfig().getString(
          MainFrame.class.getName()+".datastoretype");
        if (dsTypeByName.containsKey(initialSelection)) {
          list.setSelectedValue(initialSelection, true);
        } else {
          list.setSelectedIndex(0);
        }
        list.setVisibleRowCount(Math.min(10, list.getModel().getSize()));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JOptionPane optionPane = new JOptionPane(
          new Object[]{label, new JScrollPane(list)},
          JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
          getIcon("datastore"), new String[]{"OK", "Cancel", "Help"}, "OK");
        final JDialog dialog = new JDialog(
          MainFrame.this, "Create a datastore", true);
        dialog.setContentPane(optionPane);
        list.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              optionPane.setValue("OK");
              dialog.setVisible(false);
            }
          }
        });
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            Object value = optionPane.getValue();
            if (value == null || value.equals(JOptionPane.UNINITIALIZED_VALUE)) {
              return;
            }
            if (dialog.isVisible()
            && e.getSource() == optionPane
            && e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
              if (optionPane.getValue().equals("Help")) {
                // don't close the dialog
                optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                showHelpFrame("sec:datastores", "gate.persist.SerialDataStore");
              } else {
                dialog.setVisible(false);
              }
            }
          }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(MainFrame.this);
        dialog.setVisible(true);
        Object answer = optionPane.getValue();
        if(answer == null) { return; }
        String className = dsTypeByName.get(list.getSelectedValue());
        if(answer.equals("OK") && !list.isSelectionEmpty()) {
          Gate.getUserConfig().put(MainFrame.class.getName()+".datastoretype",
            list.getSelectedValue());
          if(className.equals("gate.persist.SerialDataStore")) {
            createSerialDataStore();
          }
          else if(className.equals("gate.persist.LuceneDataStoreImpl")) {
            createSearchableDataStore();
          }
          else if(className.equals("gate.persist.OracleDataStore")) {
            JOptionPane.showMessageDialog(MainFrame.this,
              "Oracle datastores can only be created "
                + "by your Oracle administrator!", "GATE",
              JOptionPane.ERROR_MESSAGE);
          }
          else {
            throw new UnsupportedOperationException("Unimplemented option!\n"
              + "Use a serial datastore");
          }
        }
      }
      else {
        // no ds types
        JOptionPane.showMessageDialog(MainFrame.this,
          "Could not find any registered types " + "of datastores...\n"
            + "Check your GATE installation!", "GATE",
          JOptionPane.ERROR_MESSAGE);

      }
    }
  }// class NewDSAction extends AbstractAction

  class LoadResourceFromFileAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public LoadResourceFromFileAction() {
      super("Restore Application from File");
      putValue(SHORT_DESCRIPTION,
        "Restores a previously saved application from a file");
      putValue(SMALL_ICON, getIcon("open-application"));
    }

    public void actionPerformed(ActionEvent e) {
      ExtensionFileFilter filter = new ExtensionFileFilter(
        "GATE Application files", "gapp");
      fileChooser.addChoosableFileFilter(filter);
      fileChooser.setDialogTitle("Select a file for this resource");
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fileChooser.setResource("lastapplication");

      if (fileChooser.showOpenDialog(MainFrame.this)
        == JFileChooser.APPROVE_OPTION) {
        final File file = fileChooser.getSelectedFile();
        Runnable runnable = new Runnable() { public void run() {
        try {
          Object resource = PersistenceManager.loadObjectFromFile(file);
          if(resource instanceof Resource) {
            Map<String, String> locations = fileChooser.getLocations();
            Resource res = (Resource) resource;
            // save also the location of the application with its name
            locations.put("application."+res.getName(),file.getCanonicalPath());
            locations.put("application.zip."+res.getName(),
              file.getCanonicalPath().replaceFirst("\\.[^.]{3,5}$", ".zip"));
            // add this application to the list of recent applications
            String list = locations.get("applications");
            if (list == null) { list = ""; }
            list = list.replaceFirst("\\Q"+res.getName()+"\\E;?", "");
            list = res.getName() + ";" + list;
            locations.put("applications", list);
            fileChooser.setLocations(locations);
          }
        }
        catch(MalformedURLException e) {
          log.error("Error when saving the resource URL.", e);
        }
        catch (final Exception error) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              String message = error.getMessage();
              alertButton.setAction(new AlertAction(error, message, null));
            }
          });
        }
        finally {
          processFinished();
        }
        }};
        Thread thread = new Thread(runnable, "LoadResourceFromFileAction");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    }
  }

  /**
   * Closes the view associated to a resource. Does not remove the
   * resource from the system, only its view.
   */
  class CloseViewAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public CloseViewAction(Handle handle) {
      super("Hide");
      putValue(SHORT_DESCRIPTION, "Hide this resource view");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control H"));
      this.handle = handle;
    }

    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(new Runnable() { public void run() {
        mainTabbedPane.remove(handle.getLargeView());
        mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
        // remove all GUI resources used by this handle
        handle.removeViews();
      }});
    }

    Handle handle;
  }

  class CloseViewsForSelectedResourcesAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public CloseViewsForSelectedResourcesAction() {
      super("Hide all");
      putValue(SHORT_DESCRIPTION, "Hide the selected resources");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runner = new Runnable() {
        public void run() {
          TreePath[] paths = resourcesTree.getSelectionPaths();
          for(TreePath path : paths) {
            final Object value = ((DefaultMutableTreeNode)
              path.getLastPathComponent()).getUserObject();
            if(value instanceof Handle) {
              SwingUtilities.invokeLater(new Runnable() { public void run() {
                new CloseViewAction((Handle)value).actionPerformed(null);
              }});
            }
          }
        }
      };
      Thread thread = new Thread(runner,
        "CloseViewsForSelectedResourcesAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class RenameResourceAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    RenameResourceAction(TreePath path) {
      super("Rename");
      putValue(SHORT_DESCRIPTION, "Rename this resource");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F2"));
      this.path = path;
    }

    public void actionPerformed(ActionEvent e) {
      resourcesTree.startEditingAtPath(path);
    }

    TreePath path;
  }

  class CloseSelectedResourcesAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public CloseSelectedResourcesAction() {
      super("Close all");
      putValue(SHORT_DESCRIPTION, "Close the selected resources");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runner = new Runnable() {
        public void run() {
          TreePath[] paths = resourcesTree.getSelectionPaths();
          for(TreePath path : paths) {
            final Object userObject = ((DefaultMutableTreeNode)
              path.getLastPathComponent()).getUserObject();
            if(userObject instanceof NameBearerHandle) {
              SwingUtilities.invokeLater(new Runnable() { public void run() {
                ((NameBearerHandle)userObject).getCloseAction()
                  .actionPerformed(null);
              }});
            }
          }
        }
      };
      Thread thread = new Thread(runner, "CloseSelectedResourcesAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class CloseRecursivelySelectedResourcesAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public CloseRecursivelySelectedResourcesAction() {
      super("Close Recursively all");
      putValue(SHORT_DESCRIPTION, "Close recursively the selected resources");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runner = new Runnable() {
        public void run() {
          TreePath[] paths = resourcesTree.getSelectionPaths();
          for(TreePath path : paths) {
            final Object userObject = ((DefaultMutableTreeNode)
              path.getLastPathComponent()).getUserObject();
            if(userObject instanceof NameBearerHandle) {
              SwingUtilities.invokeLater(new Runnable() { public void run() {
                ((NameBearerHandle)userObject).getCloseRecursivelyAction()
                  .actionPerformed(null);
              }});
            }
          }
        }
      };
      Thread thread = new Thread(runner,
        "CloseRecursivelySelectedResourcesAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class HideAllAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public HideAllAction() {
      super("Hide all");
      putValue(SHORT_DESCRIPTION, "Hide all resource views");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift H"));
    }

    public void actionPerformed(ActionEvent e) {
      // for each element in the tree look if it is in the tab panel
      // if yes, remove it from the tab panel
      Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
      DefaultMutableTreeNode node;
      while(nodesEnum.hasMoreElements()) {
        node = (DefaultMutableTreeNode)nodesEnum.nextElement();
        if ((node.getUserObject() instanceof Handle)
         && (mainTabbedPane.indexOfComponent(
            ((Handle)node.getUserObject()).getLargeView()) != -1)) {
          final Handle handle = (Handle)node.getUserObject();
          SwingUtilities.invokeLater(new Runnable() { public void run() {
            (new CloseViewAction(handle)).actionPerformed(null);
          }});
        }
      }
    }
  }

  class ShowSelectedResourcesAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public ShowSelectedResourcesAction() {
      super("Show all");
      putValue(SHORT_DESCRIPTION, "Show the selected resources");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("Enter"));
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runner = new Runnable() {
        public void run() {
          TreePath[] paths = resourcesTree.getSelectionPaths();
          if (paths == null) { return; }
          if (paths.length > 10) {
            Object[] possibleValues =
              { "Open the "+paths.length+" objects", "Don't open" };
            int selectedValue =
              JOptionPane.showOptionDialog(instance, "Do you want to open "
              +paths.length+" objects in the central tabbed pane ?",
              "Warning", JOptionPane.DEFAULT_OPTION,
              JOptionPane.QUESTION_MESSAGE, null,
              possibleValues, possibleValues[1]);
            if (selectedValue == 1
             || selectedValue == JOptionPane.CLOSED_OPTION) {
              return;
            }
          }
          for (TreePath path : paths) {
            if(path != null) {
              Object value = path.getLastPathComponent();
              value = ((DefaultMutableTreeNode)value).getUserObject();
              if(value instanceof Handle) {
                final Handle handle = (Handle)value;
                SwingUtilities.invokeLater(new Runnable() { public void run() {
                  select(handle);
                }});
              }
            }
          }
        }
      };
      Thread thread = new Thread(runner, "ShowSelectedResourcesAction");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class ShowResourceAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    Handle handle;
    public ShowResourceAction(Handle handle) {
      super("Show");
      putValue(SHORT_DESCRIPTION, "Show this resource");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("Enter"));
      this.handle = handle;
    }

    public void actionPerformed(ActionEvent e) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { select(handle); }
      });
    }
  }

  /**
   * Closes the view associated to a resource. Does not remove the
   * resource from the system, only its view.
   */
  class ExitGateAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public ExitGateAction() {
      super("Exit GATE");
      putValue(SHORT_DESCRIPTION, "Closes the application");
      putValue(SMALL_ICON, getIcon("crystal-clear-action-exit"));
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F4"));
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          // save the options
          OptionsMap userConfig = Gate.getUserConfig();
          try {
          if(userConfig.getBoolean(GateConstants.SAVE_OPTIONS_ON_EXIT)) {
            // save the window size
            Integer width = MainFrame.this.getWidth();
            Integer height = MainFrame.this.getHeight();
            userConfig.put(GateConstants.MAIN_FRAME_WIDTH, width);
            userConfig.put(GateConstants.MAIN_FRAME_HEIGHT, height);
            Gate.writeUserConfig();
          }
          else {
            // don't save options on close
            // save the option not to save the options
            OptionsMap originalUserConfig = Gate.getOriginalUserConfig();
            originalUserConfig.put(GateConstants.SAVE_OPTIONS_ON_EXIT, false);
            userConfig.clear();
            userConfig.putAll(originalUserConfig);
            Gate.writeUserConfig();
          }
          }
          catch(GateException error) {
            String message = "Failed to save config data.";
            alertButton.setAction(new AlertAction(error, message, null));
          }

          // save the session;
          File sessionFile = Gate.getUserSessionFile();
          if(userConfig.getBoolean(GateConstants.SAVE_SESSION_ON_EXIT)) {
            // save all the open applications
            try {
              ArrayList<Resource> appList = new ArrayList<Resource>(
                Gate.getCreoleRegister().getAllInstances("gate.Controller"));
              // remove all hidden instances
              Iterator appIter = appList.iterator();
              while(appIter.hasNext()) {
                if(Gate.getHiddenAttribute(((Controller)appIter.next())
                  .getFeatures())) { appIter.remove(); }
              }
              // When saving the session file, save URLs relative to GATE home
              // but do not warn about them
              gate.util.persistence.PersistenceManager.saveObjectToFile(
                appList, sessionFile, true, false);
            }
            catch(Exception error) {
              String message = "Failed to save session data.";
              alertButton.setAction(new AlertAction(error, message, null));
            }
          }
          else {
            // we don't want to save the session
            if(sessionFile.exists() && !sessionFile.delete()) {
              log.error("Error when deleting the session file.");
            }
          }

          // restore out and err streams as we're about to hide the
          // windows
          System.setErr(logArea.getOriginalErr());
          System.setOut(logArea.getOriginalOut());
          // now we need to dispose all GUI roots
          List<Component> roots = new ArrayList<Component>(getGuiRoots());
          while(!roots.isEmpty()) {
            Component aRoot = roots.remove(0);
            if(aRoot instanceof Window) {
              Window window = (Window)aRoot;
              roots.addAll(Arrays.asList(window.getOwnedWindows()));
              window.setVisible(false);
              window.dispose();
            }
          }

          // only hidden when closed
          if(helpFrame != null) helpFrame.dispose();

          // trying to release all resources occupied by all
          try {
            //make a list of lists of resources of various kinds
            List<List<Resource>> listOfListOfResources =
              new ArrayList<List<Resource>>();
//            listOfListOfResoruces.add(Gate.getCreoleRegister().getAllInstances(
//                    gate.VisualResource.class.getName()));
            listOfListOfResources.add(Gate.getCreoleRegister().getAllInstances(
                    gate.LanguageResource.class.getName()));
            listOfListOfResources.add(Gate.getCreoleRegister().getAllInstances(
                    gate.ProcessingResource.class.getName()));
            listOfListOfResources.add(Gate.getCreoleRegister().getAllInstances(
                    gate.Controller.class.getName()));

            for(List<Resource> resources :listOfListOfResources){
              // we need to call the clean up method for each of these resources
              for(Resource aResource : resources) {
                try {
                  Factory.deleteResource(aResource);
                } catch(Throwable e) {
                  // this may throw somekind of exception
                  // but we just ignore it as anyway we are closing everything
                  log.error(
                    "Some problems occurred when cleaning up the resources.", e);
                }
              }
            }

            // close all the opened datastores
            if(Gate.getDataStoreRegister() != null) {
              Set dataStores = new HashSet(Gate.getDataStoreRegister());
              for(Object aDs : dataStores) {
                try {
                  if(aDs instanceof DataStore) {
                    ((DataStore)aDs).close();
                  }
                }
                catch(Throwable e) {
                  log.error(
                    "Some problems occurred when closing the datastores.", e);
                }
              }
            }

          }
          catch(GateException e) {
            // we just ignore this
            log.error("A problem occurred when exiting from GATE.", e);
          }

        }// run
      };// Runnable
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "Shutdown thread");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class OpenDSAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public OpenDSAction() {
      super("Open Datastore");
      putValue(SHORT_DESCRIPTION, "Open a datastore");
      putValue(SMALL_ICON, getIcon("datastore"));
    }

    public void actionPerformed(ActionEvent e) {
      Map<String,String> dsTypes = DataStoreRegister.getDataStoreClassNames();
      HashMap<String,String> dsTypeByName = new HashMap<String,String>();
      for(Map.Entry<String, String> entry : dsTypes.entrySet()) {
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()) {
        JLabel label = new JLabel("Select a type of Datastore:");
        final JList list = new JList(dsTypeByName.keySet().toArray());
        String initialSelection = Gate.getUserConfig().getString(
          MainFrame.class.getName()+".datastoretype");
        if (dsTypeByName.containsKey(initialSelection)) {
          list.setSelectedValue(initialSelection, true);
        } else {
          list.setSelectedIndex(0);
        }
        list.setVisibleRowCount(Math.min(10, list.getModel().getSize()));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final JOptionPane optionPane = new JOptionPane(
          new Object[]{label, new JScrollPane(list)},
          JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
          getIcon("datastore"), new String[]{"OK", "Cancel", "Help"}, "OK");
        final JDialog dialog = new JDialog(
          MainFrame.this, "Open a datastore", true);
        dialog.setContentPane(optionPane);
        list.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              optionPane.setValue("OK");
              dialog.setVisible(false);
            }
          }
        });
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            Object value = optionPane.getValue();
            if (value == null || value.equals(JOptionPane.UNINITIALIZED_VALUE)) {
              return;
            }
            if (dialog.isVisible()
            && e.getSource() == optionPane
            && e.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) {
              if (optionPane.getValue().equals("Help")) {
                // don't close the dialog
                optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                showHelpFrame("sec:datastores", "gate.persist.SerialDataStore");
              } else {
                dialog.setVisible(false);
              }
            }
          }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(MainFrame.this);
        dialog.setVisible(true);
        Object answer = optionPane.getValue();
        if(answer == null) { return; }
        String className = dsTypeByName.get(list.getSelectedValue());
        if(answer.equals("OK") && !list.isSelectionEmpty()) {
          Gate.getUserConfig().put(MainFrame.class.getName()
            + ".datastoretype", list.getSelectedValue());
          if(className.indexOf("SerialDataStore") != -1) {
            openSerialDataStore();
          }
          else if(className.indexOf("LuceneDataStoreImpl") != -1) {
            openSearchableDataStore();
          }
          else if(className.indexOf("DocServiceDataStore") != -1) {
            openDocServiceDataStore();
          }
          else if(className.equals("gate.persist.OracleDataStore")
            || className.equals("gate.persist.PostgresDataStore")) {
            List dbPaths = new ArrayList();
            for(Object o : DataStoreRegister.getConfigData().keySet()) {
              String keyName = (String) o;
              if(keyName.startsWith("url")) {
                dbPaths.add(DataStoreRegister.getConfigData().get(keyName));
              }
            }
            if(dbPaths.isEmpty())
              throw new GateRuntimeException(
                "JDBC URL not configured in gate.xml");
            // by default make it the first
            String storageURL = (String)dbPaths.get(0);
            if(dbPaths.size() > 1) {
              Object[] paths = dbPaths.toArray();
              answer =
                JOptionPane.showInputDialog(MainFrame.this,
                  "Select a database", "GATE", JOptionPane.QUESTION_MESSAGE,
                  null, paths, paths[0]);
              if(answer != null)
                storageURL = (String)answer;
              else return;
            }
            DataStore ds = null;
            AccessController ac = null;
            try {
              // 1. login the user
              // ac = new AccessControllerImpl(storageURL);
              ac = Factory.createAccessController(storageURL);
              Assert.assertNotNull(ac);
              ac.open();

              Session mySession;
              User usr;
              Group grp;
              try {
                String userName = "";
                String userPass = "";
                String group = "";

                JPanel listPanel = new JPanel();
                listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS));

                JPanel panel1 = new JPanel();
                panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
                panel1.add(new JLabel("User name: "));
                panel1.add(new JLabel("Password: "));
                panel1.add(new JLabel("Group: "));

                JPanel panel2 = new JPanel();
                panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
                JTextField usrField = new JTextField(30);
                panel2.add(usrField);
                JPasswordField pwdField = new JPasswordField(30);
                panel2.add(pwdField);
                JComboBox grpField = new JComboBox(ac.listGroups().toArray());
                grpField.setSelectedIndex(0);
                panel2.add(grpField);

                listPanel.add(panel1);
                listPanel.add(Box.createHorizontalStrut(20));
                listPanel.add(panel2);

                if(OkCancelDialog.showDialog(MainFrame.this.getContentPane(),
                  listPanel, "Please enter login details")) {

                  userName = usrField.getText();
                  userPass = new String(pwdField.getPassword());
                  group = (String)grpField.getSelectedItem();

                  if(userName.equals("") || userPass.equals("")
                    || group.equals("")) {
                    JOptionPane
                      .showMessageDialog(
                        MainFrame.this,
                        "You must provide non-empty user name, password and group!",
                        "Login error", JOptionPane.ERROR_MESSAGE);
                    return;
                  }
                }
                else if(OkCancelDialog.userHasPressedCancel) { return; }

                grp = ac.findGroup(group);
                usr = ac.findUser(userName);
                mySession = ac.login(userName, userPass, grp.getID());

                // save here the user name, pass and group in
                // local gate.xml

              }
              catch(gate.security.SecurityException ex) {
                JOptionPane.showMessageDialog(MainFrame.this, ex.getMessage(),
                  "Login error", JOptionPane.ERROR_MESSAGE);
                ac.close();
                return;
              }

              if(!ac.isValidSession(mySession)) {
                JOptionPane.showMessageDialog(MainFrame.this,
                  "Incorrect session obtained. "
                    + "Probably there is a problem with the database!",
                  "Login error", JOptionPane.ERROR_MESSAGE);
                ac.close();
                return;
              }

              // 2. open the oracle datastore
              ds = Factory.openDataStore(className, storageURL);
              // set the session, so all get/adopt/etc work
              ds.setSession(mySession);

              // 3. add the security data for this datastore
              // this saves the user and group information, so it
              // can
              // be used later when resources are created with
              // certain rights
              FeatureMap securityData = Factory.newFeatureMap();
              securityData.put("user", usr);
              securityData.put("group", grp);
              DataStoreRegister.addSecurityData(ds, securityData);
            }
            catch(PersistenceException pe) {
              JOptionPane.showMessageDialog(MainFrame.this,
                "Datastore open error!\n " + pe.toString(), "GATE",
                JOptionPane.ERROR_MESSAGE);
            }
            catch(gate.security.SecurityException se) {
              JOptionPane.showMessageDialog(MainFrame.this,
                "User identification error!\n " + se.toString(), "GATE",
                JOptionPane.ERROR_MESSAGE);
              try {
                if(ac != null) ac.close();
                if(ds != null) ds.close();
              }
              catch(gate.persist.PersistenceException ex) {
                JOptionPane.showMessageDialog(MainFrame.this,
                  "Persistence error!\n " + ex.toString(), "GATE",
                  JOptionPane.ERROR_MESSAGE);
              }
            }

          }
          else {
            JOptionPane.showMessageDialog(MainFrame.this,
              "Support for this type of datastores is not implemenented!\n",
              "GATE", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
      else {
        // no ds types
        JOptionPane.showMessageDialog(MainFrame.this,
          "Could not find any registered types " + "of datastores...\n"
            + "Check your GATE installation!", "GATE",
          JOptionPane.ERROR_MESSAGE);

      }
    }
  }// class OpenDSAction extends AbstractAction

  /**
   * A menu that self populates based on CREOLE register data before
   * being shown. Used for creating new resources of all possible types.
   */
  class LiveMenu extends XJMenu {
    public LiveMenu(int type) {
      super();
      this.type = type;
      init();
    }

    protected void init() {
      addMenuListener(new MenuListener() {
        public void menuCanceled(MenuEvent e) {
          // do nothing
        }
        public void menuDeselected(MenuEvent e) {
          // clear the status
          statusChanged("");
        }
        public void menuSelected(MenuEvent e) {
          removeAll();
          // find out the available types of LRs and repopulate the menu
          CreoleRegister reg = Gate.getCreoleRegister();
          List<String> resTypes;
          switch(type){
            case LR:
              resTypes = reg.getPublicLrTypes();
              break;
            case PR:
              resTypes = new ArrayList<String>( reg.getPublicPrTypes() );
              //GATE default controllers are now also PRs, but we don't want
              //them here
              resTypes.removeAll(reg.getPublicControllerTypes());
              break;
            case APP:
              resTypes = reg.getPublicControllerTypes();
              break;
            default:
              throw new GateRuntimeException("Unknown LiveMenu type: " + type);
          }

          if(resTypes != null) {
            if (!resTypes.isEmpty()) {
              HashMap<String, ResourceData> resourcesByName
                = new HashMap<String, ResourceData>();
              Iterator<String> resIter = resTypes.iterator();
              while(resIter.hasNext()) {
                ResourceData rData = reg.get(resIter.next());
                resourcesByName.put(rData.getName(), rData);
              }
              List<String> resNames =
                new ArrayList<String>(resourcesByName.keySet());
              Collections.sort(resNames);
              resIter = resNames.iterator();
              while(resIter.hasNext()) {
                ResourceData rData = resourcesByName.get(resIter.next());
                add(new XJMenuItem(new NewResourceAction(rData), MainFrame.this));
              }
            } else if (type == PR) {
              // empty PR menu -> add an action to load ANNIE plugin
              add(new AbstractAction("Add ANNIE Resources to this Menu") {
                { putValue(SHORT_DESCRIPTION, "Load the ANNIE plugin."); }
                public void actionPerformed(ActionEvent e) {
                try {
                  URL pluginUrl = new File(Gate.getPluginsHome(),
                    ANNIEConstants.PLUGIN_DIR).toURI().toURL();
                  Gate.getCreoleRegister().registerDirectories(pluginUrl);
                  Gate.addAutoloadPlugin(pluginUrl);
                } catch(Exception ex) {
                  log.error("Unable to load ANNIE plugin.", ex);
                }
              }});
            }
          }

          // fire the status listener events
          switch(type){
            case LR:
              statusChanged("Data used for annotating");
              break;
            case PR:
              statusChanged("Processes that annotate data");
              break;
            case APP:
              statusChanged("Run processes on data");
              break;
            default:
              statusChanged("Unknown resource: " + type);
          }
        }
      });
    }

    protected int type;

    /**
     * Switch for using LR data.
     */
    public static final int LR = 1;

    /**
     * Switch for using PR data.
     */
    public static final int PR = 2;

    /**
     * Switch for using Controller data.
     */
    public static final int APP = 3;
  }

  /**
   *  Recent applications menu that remembers previously loaded applications.
   */
  class RecentAppsMenu extends XJMenu {
    public RecentAppsMenu() {
      super();
      init();
      addMenuItems();
    }

    protected void init() {
      addMenuListener(new MenuListener() {
        public void menuCanceled(MenuEvent e) {
          // do nothing
        }
        public void menuDeselected(MenuEvent e) {
          // clear the status
          statusChanged("");
        }
        public void menuSelected(MenuEvent e) {
          removeAll();
          addMenuItems();
          if (getMenuComponentCount() == 0) {
            add("No Recent Applications");
          }
        }
      });
    }

    protected void addMenuItems() {
    final Map<String, String> locations = fileChooser.getLocations();
    final String list = locations.get("applications");
    if (list == null || list.equals("")) { return; }
    for (final String name : list.split(";")) {
      final String location = locations.get("application." + name);
      final XJMenuItem item = new XJMenuItem(new AbstractAction(name,
        MainFrame.getIcon("open-application")) {
        { this.putValue(Action.SHORT_DESCRIPTION, location); }
        public void actionPerformed(ActionEvent e) {
          Runnable runnable = new Runnable() { public void run() {
          try { File file = new File(location);
            PersistenceManager.loadObjectFromFile(file);
          } catch(Exception e) {
            String message = "Couldn't reload application.\n" + e.getMessage();
            alertButton.setAction(new AlertAction(e, message, null));
            if (e instanceof IOException) {
              // remove selected element from the applications list
              locations.put("applications", list.replaceFirst(name + ";?", ""));
              fileChooser.setLocations(locations);
            }
          } finally { processFinished(); }}};
          Thread thread = new Thread(runnable ,"Reload application");
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
      }}, MainFrame.this);
      item.addMenuKeyListener(new MenuKeyListener() {
        public void menuKeyTyped(MenuKeyEvent e) { /* do nothing */ }
        public void menuKeyPressed(MenuKeyEvent e) {
          if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            // remove selected element from the applications list
            locations.put("applications", list.replaceFirst(name + ";?", ""));
            fileChooser.setLocations(locations);
            // TODO: update the menu
//            item.setVisible(false);
//            item.revalidate();
          }
        }
        public void menuKeyReleased(MenuKeyEvent e) {/* do nothing */ }
      });
      add(item);
    }
    add(new XJMenuItem(new AbstractAction("Clear List") {
      public void actionPerformed(ActionEvent e) {
        // clear the applications list
        locations.put("applications", "");
        fileChooser.setLocations(locations);
    }}, MainFrame.this));
    }
  }

  /**
   * The "Tools" menu, which includes some static menu items
   * (added in initGuiComponents) and some dynamic items contributed
   * by plugins.
   * @author ian
   */
  class ToolsMenu extends XJMenu implements CreoleListener {

    /**
     * The first position in the menu that can be used by dynamic
     * items.
     */
    protected int firstPluginItem = 0;

    protected IdentityHashMap<Resource, List<JMenuItem>> itemsByResource =
      new IdentityHashMap<Resource, List<JMenuItem>>();

    public ToolsMenu(String name, String description, StatusListener listener) {
      super(name, description, listener);
      Gate.getCreoleRegister().addCreoleListener(this);
    }

    /**
     * Called when the static items have been added to inform the menu
     * that it can start doing its dynamic stuff.
     */
    public void staticItemsAdded() {
      firstPluginItem = getItemCount();
      processExistingTools();
    }

    /**
     * Populate the menu with all actions coming from tools that are
     * already loaded.
     */
    protected void processExistingTools() {
      Set<String> toolTypes = Gate.getCreoleRegister().getToolTypes();
      for(String type : toolTypes) {
        List<Resource> instances = Gate.getCreoleRegister()
                    .get(type).getInstantiations();
        for(Resource res : instances) {
          if(res instanceof ActionsPublisher) {
            toolLoaded(res);
          }
        }
      }
    }

    /**
     * If the resource just loaded is a tool (according to the creole
     * register) then see if it publishes any actions and if so, add
     * them to the menu in the appropriate places.
     */
    public void resourceLoaded(CreoleEvent e) {
      Resource res = e.getResource();
      if(Gate.getCreoleRegister().get(res.getClass().getName()).isTool()
              && res instanceof ActionsPublisher) {
        toolLoaded(res);
      }
    }

    /**
     * Add the actions published by the given tool to their appropriate
     * places on the Tools menu.
     * @param res the tool instance (which must implement ActionsPublisher)
     */
    protected void toolLoaded(Resource res) {
      List<Action> actions = ((ActionsPublisher)res).getActions();
      List<JMenuItem> items = new ArrayList<JMenuItem>();
      for(Action a : actions) {
        items.add(addMenuItem(a));
      }
      itemsByResource.put(res, items);
    }

    protected JMenuItem addMenuItem(Action a) {
      // start by searching this menu
      XJMenu menuToUse = this;
      int firstIndex = firstPluginItem;
      // if the action has a menu path set, navigate the path to find the
      // right menu.
      String[] menuPath = (String[])a.getValue(GateConstants.MENU_PATH_KEY);
      if(menuPath != null) {
        PATH_ELEMENT: for(String pathElement : menuPath) {
          int i;
          for(i = firstIndex; i < menuToUse.getItemCount(); i++) {
            JMenuItem item = menuToUse.getItem(i);
            if(item instanceof XJMenu && item.getText().equals(pathElement)) {
              // found the menu for this path element, move on to the next one
              firstIndex = 0;
              menuToUse = (XJMenu)item;
              continue PATH_ELEMENT;
            }
            else if(item.getText().compareTo(pathElement) < 0) {
              // we've gone beyond where this menu should be in alpha
              // order
              break;
            }
          }
          // if we get to here, we didn't find a menu to use - add one
          XJMenu newMenu = new XJMenu(pathElement, pathElement, this.listener);
          menuToUse.add(newMenu);
          firstIndex = 0;
          menuToUse = newMenu;
        }
      }

      // we now have the right menu, add the action at the right place
      int pos = firstIndex;
      while(pos < menuToUse.getItemCount()) {
        JMenuItem item = menuToUse.getItem(pos);
        if(item != null && item.getText().compareTo((String)a.getValue(Action.NAME)) > 0) {
          break;
        }
        pos++;
      }
      // finally, add the menu item and return it
      return menuToUse.insert(new XJMenuItem(a, this.listener), pos);
    }

    /**
     * If the resource just unloaded is one that contributed some menu
     * items then remove them again.
     */
    public void resourceUnloaded(CreoleEvent e) {
      Resource res = e.getResource();
      List<JMenuItem> items = itemsByResource.remove(res);
      if(items != null) {
        for(JMenuItem item : items) {
          removeMenuItem(item);
        }
      }
    }

    protected void removeMenuItem(JMenuItem itemToRemove) {
      Action a = itemToRemove.getAction();
      // start by searching this menu
      XJMenu menuToUse = this;
      int firstIndex = firstPluginItem;
      // keep track of the parent menu
      XJMenu parentMenu = null;
      // if the action has a menu path set, navigate the path to find the
      // right menu.
      String[] menuPath = (String[])a.getValue(GateConstants.MENU_PATH_KEY);
      if(menuPath != null) {
        PATH_ELEMENT: for(String pathElement : menuPath) {
          int i;
          for(i = firstIndex; i < menuToUse.getItemCount(); i++) {
            JMenuItem item = menuToUse.getItem(i);
            if(item instanceof XJMenu && item.getText().equals(pathElement)) {
              // found the menu for this path element, move on to the next one
              firstIndex = 0;
              parentMenu = menuToUse;
              menuToUse = (XJMenu)item;
              continue PATH_ELEMENT;
            }
          }
          // we've reached the end of a menu without finding the sub-menu
          // we were looking for.  This shouldn't happen, but if it does then
          // we can ignore it as if there's no menu to remove the thing from
          // then there's nothing to remove either.
          return;
        }
      }

      // we have a menu to remove the item from: remove it
      menuToUse.remove(itemToRemove);
      if(menuToUse.getItemCount() == 0 && parentMenu != null) {
        // sub-menu is empty, remove it from parent
        parentMenu.remove(menuToUse);
      }
    }

    // remaining CreoleListener methods not used
    public void datastoreClosed(CreoleEvent e) { }
    public void datastoreCreated(CreoleEvent e) { }
    public void datastoreOpened(CreoleEvent e) { }
    public void resourceRenamed(Resource resource, String oldName,
            String newName) { }
  }

  /**
   * Overrides default JTree behaviour for tooltips.
   */
  class ResourcesTree extends JTree {
    private static final long serialVersionUID = 1L;

    public ResourcesTree() {
      myToolTip = new ResourceToolTip();
    }

    /**
     * Overrides <code>JTree</code>'s <code>getToolTipText</code>
     * method in order to allow custom tips to be used.
     *
     * @param event the <code>MouseEvent</code> that initiated the
     *          <code>ToolTip</code> display
     * @return a string containing the tooltip or <code>null</code> if
     *         <code>event</code> is null
     */
    public String getToolTipText(MouseEvent event) {
      String res = super.getToolTipText(event);
      if(event != null) {
        Point p = event.getPoint();
        int selRow = getRowForLocation(p.x, p.y);
        if(selRow != -1) {
          TreePath path = getPathForRow(selRow);
          Object lastPath = path.getLastPathComponent();
          Object value = ((DefaultMutableTreeNode)lastPath).getUserObject();
          myToolTip.setValue(value);
        }
      }
      // if we always return the same text, the tooltip manager thinks
      // the
      // size and location don't need changing.
      return res;
    }

    public JToolTip createToolTip() {
      return myToolTip;
    }

    ResourceToolTip myToolTip;
  }

  /**
   * Implementation of a custom tool tip to be used for showing extended
   * information about CREOLE resources.
   *
   */
  class ResourceToolTip extends JToolTip {
    private static final long serialVersionUID = 1L;
    public ResourceToolTip() {
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      tipComponent = new JPanel();
      tipComponent.setOpaque(false);
      tipComponent.setLayout(new BoxLayout(tipComponent, BoxLayout.X_AXIS));
      tipComponent.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

      iconLabel = new JLabel(getIcon("annie-application"));
      iconLabel.setText(null);
      iconLabel.setOpaque(false);
      tipComponent.add(iconLabel);

      textLabel = new JLabel();
      textLabel.setOpaque(false);
      tipComponent.add(Box.createHorizontalStrut(10));
      tipComponent.add(textLabel);

      add(tipComponent);
    }

    /**
     * Label used for the icon
     */
    JLabel iconLabel;

    /**
     * Label used for the text
     */
    JLabel textLabel;

    /**
     * The actual component displaying the tooltip.
     */
    JPanel tipComponent;

    public void setTipText(String tipText) {
      // textLabel.setText(tipText);
    }

    /**
     * Sets the value to be displayed
     *
     * @param value to be displayed as tooltip
     */
    public void setValue(Object value) {
      if(value != null) {
        if(value instanceof String) {
          String text = (String) value;
          if (text.equals("GATE")) {
            textLabel.setText("Resources tree root ");
            iconLabel.setIcon(getIcon("root"));
          } else if (text.equals("Applications")) {
            textLabel.setText("Applications: run processes on data ");
            iconLabel.setIcon(getIcon("applications"));
          } else if (text.equals("Language Resources")) {
            textLabel.setText("Language Resources: data used for annotating ");
            iconLabel.setIcon(getIcon("lrs"));
          } else if (text.equals("Processing Resources")) {
            textLabel.setText(
              "Processing Resources: processes that annotate data ");
            iconLabel.setIcon(getIcon("prs"));
          } else if (text.equals("Datastores")) {
            textLabel.setText("Datastores: repositories for large data ");
            iconLabel.setIcon(getIcon("datastores"));
          } else {
            textLabel.setText(text);
            iconLabel.setIcon(null);
          }
        }
        else if(value instanceof NameBearerHandle) {
          NameBearerHandle handle = (NameBearerHandle)value;
          textLabel.setText(handle.getTooltipText());
          iconLabel.setIcon(handle.getIcon());
        }
        else {
          textLabel.setText(null);
          iconLabel.setIcon(null);
        }
      }
    }

    public Dimension getPreferredSize() {
      Dimension d = tipComponent.getPreferredSize();
      Insets ins = getInsets();
      return new Dimension(d.width + ins.left + ins.right, d.height + ins.top
        + ins.bottom);
    }

  }

  class HelpAboutAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public HelpAboutAction() {
      super("About");
      putValue(SHORT_DESCRIPTION, "Show developers names and version");
    }

    public void actionPerformed(ActionEvent e) {
      splash.showSplash();
    }
  }

  class HelpMailingListAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    String keywords;
    public HelpMailingListAction() {
      super("Search in Mailing List");
      putValue(SHORT_DESCRIPTION,
        "Search keywords in GATE users mailing list");
      this.keywords = null;
    }
    public HelpMailingListAction(String keywords) {
      super("Search in Mailing List");
      this.keywords = keywords;
    }
    public void actionPerformed(ActionEvent e) {
      if (keywords == null) {
        keywords = JOptionPane.showInputDialog(instance,
            "Please enter your search keywords.",
            (String) this.getValue(NAME),
            JOptionPane.QUESTION_MESSAGE);
        if (keywords == null || keywords.trim().length() == 0) { return; }
      }
      try {
      showHelpFrame("http://sourceforge.net/search/index.php?" +
       "group_id=143829&form_submit=Search&type_of_search=mlists" +
       "&q=" + java.net.URLEncoder.encode(keywords, "UTF-8"),
       "mailing list");

      } catch (UnsupportedEncodingException error) {
        String message = "The Character Encoding is not supported.";
        alertButton.setAction(new AlertAction(error, message, null));
      } finally {
        keywords = null;
      }
    }
  }

  class HelpUserGuideAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public HelpUserGuideAction() {
      super("User Guide Contents");
      putValue(SHORT_DESCRIPTION, "Contents of the online user guide");
    }

    public void actionPerformed(ActionEvent e) {
      showHelpFrame("", "help contents");
    }
  }

  public void showHelpFrame(String urlString, String resourceName) {
    final URL url;
    if (resourceName == null) { resourceName = "unknown"; }
    if (urlString != null
    && !urlString.startsWith("http://")
    && !urlString.startsWith("file://")) {
      urlString = "http://gate.ac.uk/userguide/" + urlString;
    }
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      JOptionPane.showMessageDialog(MainFrame.this,
        (urlString == null) ?
        "There is no help page for this resource !\n\n" +
        "Find the developper of the resource:\n" +
        resourceName + "\n" + "and force him/her to put one."
        :
        "The URL of the page for " + resourceName + " is invalid.\n"
        + urlString,
        "GATE", JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    Runnable runnable = new Runnable() {
      public void run() {
        StringBuilder actualURL = new StringBuilder(url.toString());
        if (url.toString().startsWith("http://gate.ac.uk/userguide/")) {
          // add gateVersion=... to the end of the URL
          int insertPoint = actualURL.length();
          if(url.getRef() != null) {
            // adjust for a #something on the end
            insertPoint -= url.getRef().length() + 1;
          }
          if(url.getQuery() == null) {
            actualURL.insert(insertPoint, '?');
          }
          else {
            actualURL.insert(insertPoint, "&");
          }
          actualURL.insert(insertPoint + 1, "gateVersion=" + gate.Main.version);
        }

        Action[] actions = {
          new AbstractAction("Show configuration") {
            public void actionPerformed(ActionEvent e) {
              optionsDialog.showDialog();
              optionsDialog.dispose();
        }}};

        String commandLine = Gate.getUserConfig().getString(
          MainFrame.class.getName()+".browsercommandline");

        if(commandLine == null || commandLine.equals("")
        || commandLine.equals("Set dynamically when you display help.")) {
          // try to find the default browser
          Process process = null;
          try {
            // Windows
            commandLine = "rundll32 url.dll,FileProtocolHandler "
              + actualURL.toString();
            try { process = Runtime.getRuntime().exec(commandLine);
            } catch (IOException ioe2) {/* skip to next try catch */}
            if (process == null || process.waitFor() != 0) {
            // Linux
            commandLine = "xdg-open " + actualURL.toString();
            try { process = Runtime.getRuntime().exec(commandLine);
            } catch (IOException ioe3) {/* skip to next try catch */}
            if (process == null || process.waitFor() != 0) {
            // Linux KDE
            commandLine = "kfmclient exec " + actualURL.toString();
            try { process = Runtime.getRuntime().exec(commandLine);
            } catch (IOException ioe4) {/* skip to next try catch */}
            if (process == null || process.waitFor() != 0) {
            // Linux Gnome
            commandLine = "gnome-open " + actualURL.toString();
            try { process = Runtime.getRuntime().exec(commandLine);
            } catch (IOException ioe5) {/* skip to next try catch */}
            if (process == null || process.waitFor() != 0) {
            // Mac
            commandLine = "open " + actualURL.toString();
            try { process = Runtime.getRuntime().exec(commandLine);
            } catch (IOException ioe1) {/* skip to next try catch */}
            if (process == null || process.waitFor() != 0) {
            String message = "Unable to determine the default browser.\n"
              + "Will use a Java browser. To use a custom command line\n"
              + "go to the Options menu then Configuration.";
            alertButton.setAction(new AlertAction(null, message, actions));
            // Java help browser
            displayJavaHelpBrowser(actualURL.toString());
            }}}}}
          } catch(SecurityException se) {
            JOptionPane.showMessageDialog(instance,
              se.getMessage(), "Help Error", JOptionPane.ERROR_MESSAGE);
            log.error("Help browser Error", se);
          } catch (InterruptedException ie) {
            JOptionPane.showMessageDialog(instance,
              ie.getMessage(), "Help Error", JOptionPane.ERROR_MESSAGE);
            log.error("Help browser Error", ie);
          }

        } else if(!commandLine.equals("Internal Java browser.")) {
          // external browser
          commandLine = commandLine.replaceFirst("%file", actualURL.toString());
          try {
            Runtime.getRuntime().exec(commandLine);
          }
          catch(Exception error) {
            String message = "Unable to call the custom browser command.\n" +
              "(" +  commandLine + ")\n\n" +
              "Please go to the Options menu then Configuration.";
            alertButton.setAction(new AlertAction(error, message, actions));
          }

        } else {
          displayJavaHelpBrowser(actualURL.toString());
      }
      }
    };
    Thread thread = new Thread(runnable, "showHelpFrame");
    thread.start();
  }

  private void displayJavaHelpBrowser(String urlString) {
    if (helpFrame == null) {
      helpFrame = new HelpFrame();
      helpFrame.setSize(800, 600);
      helpFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
      // center on screen
      Dimension frameSize = helpFrame.getSize();
      Dimension ownerSize = Toolkit.getDefaultToolkit().getScreenSize();
      Point ownerLocation = new Point(0, 0);
      helpFrame.setLocation(ownerLocation.x
         + (ownerSize.width - frameSize.width) / 2, ownerLocation.y
         + (ownerSize.height - frameSize.height) / 2);
    }
    try {
      helpFrame.setPage(new URL(urlString));
    } catch (IOException error) {
      String message = "Error when loading help page.";
      alertButton.setAction(new AlertAction(error, message, null));
      return;
    }
    helpFrame.setVisible(false);
    helpFrame.setVisible(true);
  }

  class HelpUserGuideInContextAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public HelpUserGuideInContextAction() {
      super("Contextual User Guide");
      putValue(SHORT_DESCRIPTION, "Online help for the selected component");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F1"));
    }

    public void actionPerformed(ActionEvent e) {
      // get the handle for the selected tab pane resource view
      // then call HelpOnItemTreeAction with this handle
      JComponent largeView = (JComponent)
        mainTabbedPane.getSelectedComponent();
      if (largeView == null) { return; }
      Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
      boolean done = false;
      DefaultMutableTreeNode node = resourcesTreeRoot;
      while(!done && nodesEnum.hasMoreElements()) {
        node = (DefaultMutableTreeNode) nodesEnum.nextElement();
        done = node.getUserObject() instanceof Handle
            && ((Handle)node.getUserObject()).viewsBuilt()
            && ((Handle)node.getUserObject()).getLargeView() == largeView;
      }
      if(done && (Handle)node.getUserObject() instanceof NameBearerHandle) {
        new HelpOnItemTreeAction((NameBearerHandle)node.getUserObject())
          .actionPerformed(null);
      } else if (mainTabbedPane.getTitleAt(mainTabbedPane
                  .getSelectedIndex()).equals("Messages")) {
        showHelpFrame("sec:developer:gui", "messages pane");
      } else {
        showHelpFrame(null, node.getUserObject().getClass().getName());
      }
    }
  }

  class HelpOnItemTreeAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    HelpOnItemTreeAction(NameBearerHandle resource) {
      super("Help");
      putValue(SHORT_DESCRIPTION, "Help on this resource");
      this.resource = resource;
    }

    public void actionPerformed(ActionEvent e) {
      String helpURL = null;
      String resourceClassName = resource.getTarget().getClass().getName();

      if (resource.getTarget() instanceof Resource) {
        // search the help URL associated to the resource
        ResourceData rd = Gate.getCreoleRegister().get(resourceClassName);
        helpURL = rd.getHelpURL();
      }

      if(helpURL == null) {
        // otherwise search in the associated VRs of the resource
        List<String> vrList = Gate.getCreoleRegister()
          .getLargeVRsForResource(resourceClassName);
        for(String vrClass : vrList) {
          ResourceData vrd = Gate.getCreoleRegister().get(vrClass);
          if(vrd != null && vrd.getHelpURL() != null) {
            helpURL = vrd.getHelpURL();
            break;
          }
        }
      }
      showHelpFrame(helpURL, resourceClassName);
    }

    NameBearerHandle resource;
  }

  class ToggleToolTipsAction extends AbstractAction {
    public ToggleToolTipsAction() {
      super("Show Tooltips");
      putValue(SHORT_DESCRIPTION,
        "Tooltips appear in help balloon like this one");
    }
    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          javax.swing.ToolTipManager toolTipManager =
            ToolTipManager.sharedInstance();
          if (toolTipManager.isEnabled()) {
            toolTipManager.setEnabled(false);
            Gate.getUserConfig().put(
              MainFrame.class.getName()+".hidetooltips", false);
          } else {
            toolTipManager.setEnabled(true);
            Gate.getUserConfig().put(
              MainFrame.class.getName()+".hidetooltips", true);
          }
        }
      };
      Thread thread = new Thread(runnable, "ToggleToolTipsAction");
      thread.start();
    }
  }


  protected class ResourcesTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 1L;
    public ResourcesTreeCellRenderer() {
      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
        hasFocus);
      if(value == resourcesTreeRoot) {
        setIcon(MainFrame.getIcon("root"));
        setToolTipText("Resources tree root ");
      }
      else if(value == applicationsRoot) {
        setIcon(MainFrame.getIcon("applications"));
        setToolTipText("Run processes on data ");
      }
      else if(value == languageResourcesRoot) {
        setIcon(MainFrame.getIcon("lrs"));
        setToolTipText("Data used for annotating ");
      }
      else if(value == processingResourcesRoot) {
        setIcon(MainFrame.getIcon("prs"));
        setToolTipText("Processes that annotate data ");
      }
      else if(value == datastoresRoot) {
        setIcon(MainFrame.getIcon("datastores"));
        setToolTipText("Repositories for large data ");
      }
      else {
        // not one of the default root nodes
        value = ((DefaultMutableTreeNode)value).getUserObject();
        if(value instanceof Handle) {
          setIcon(((Handle)value).getIcon());
          setText(((Handle)value).getTitle());
          setToolTipText(((Handle)value).getTooltipText());
        }
      }
      return this;
    }

  }

  protected class ResourcesTreeCellEditor extends DefaultTreeCellEditor {
    ResourcesTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
      super(tree, renderer);
    }

    /**
     * This is the original implementation from the super class with
     * some changes (i.e. shorter timer: 500 ms instead of 1200)
     */
    protected void startEditingTimer() {
      if(timer == null) {
        timer = new javax.swing.Timer(500, this);
        timer.setRepeats(false);
      }
      timer.start();
    }

    /**
     * This is the original implementation from the super class with
     * some changes (i.e. correct discovery of icon)
     */
    public Component getTreeCellEditorComponent(JTree tree, Object value,
      boolean isSelected, boolean expanded, boolean leaf, int row) {
      Component retValue =
        super.getTreeCellEditorComponent(tree, value, isSelected, expanded,
          leaf, row);
      // lets find the actual icon
      if(renderer != null) {
        renderer.getTreeCellRendererComponent(tree, value, isSelected,
          expanded, leaf, row, false);
        editingIcon = renderer.getIcon();
      }
      return retValue;
    }
  }// protected class ResourcesTreeCellEditor extends
  // DefaultTreeCellEditor {

  protected class ResourcesTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = 1L;
    ResourcesTreeModel(TreeNode root, boolean asksAllowChildren) {
      super(root, asksAllowChildren);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
      DefaultMutableTreeNode aNode =
        (DefaultMutableTreeNode)path.getLastPathComponent();
      Object userObject = aNode.getUserObject();
      if(userObject instanceof Handle) {
        Handle handle = (Handle)userObject;
        Object target = handle.getTarget();
        if(target instanceof Resource) {
          Gate.getCreoleRegister().setResourceName((Resource)target,
            (String)newValue);
        } else if(target instanceof NameBearer){
          //not a resource, we need to do it manually
          ((NameBearer)target).setName((String)newValue);
          //next see if there is a tab for this resource and rename it
          for(int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            if(mainTabbedPane.getComponentAt(i) == handle.getLargeView()) {
              mainTabbedPane.setTitleAt(i, (String)newValue);
              break;
            }
          }
        }
      }
      nodeChanged(aNode);

    }
  }

  class ProgressBarUpdater implements Runnable {
    ProgressBarUpdater(int newValue) {
      value = newValue;
    }

    public void run() {
      if(value == 0)
        progressBar.setVisible(false);
      else progressBar.setVisible(true);
      progressBar.setValue(value);
    }

    int value;
  }

  class StatusBarUpdater implements Runnable {
    StatusBarUpdater(String text) {
      this.text = text;
    }

    public void run() {
      statusBar.setText(text);
    }

    String text;
  }

  /**
   * During longer operations it is nice to keep the user entertained so
   * (s)he doesn't fall asleep looking at a progress bar that seems have
   * stopped. Also there are some operations that do not support
   * progress reporting so the progress bar would not work at all so we
   * need a way to let the user know that things are happening. We chose
   * for purpose to show the user a small cartoon in the form of an
   * animated gif. This class handles the displaying and updating of
   * those cartoons.
   */
  class CartoonMinder implements Runnable {

    CartoonMinder(JPanel targetPanel) {
      active = false;
      dying = false;
      this.targetPanel = targetPanel;
      imageLabel = new JLabel(getIcon("working"));
      imageLabel.setOpaque(false);
      imageLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    }

    public boolean isActive() {
      boolean res;
      synchronized(lock) {
        res = active;
      }
      return res;
    }

    public void activate() {
      // add the label in the panel
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          targetPanel.add(imageLabel);
        }
      });
      // wake the dormant thread
      synchronized(lock) {
        active = true;
      }
    }

    public void deactivate() {
      // send the thread to sleep
      synchronized(lock) {
        active = false;
      }
      // clear the panel
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          targetPanel.removeAll();
          targetPanel.repaint();
        }
      });
    }

    public void dispose() {
      synchronized(lock) {
        dying = true;
      }
    }

    public void run() {
      boolean isDying;
      synchronized(lock) {
        isDying = dying;
      }
      while(!isDying) {
        boolean isActive;
        synchronized(lock) {
          isActive = active;
        }
        if(isActive && targetPanel.isVisible()) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // targetPanel.getParent().validate();
              // targetPanel.getParent().repaint();
              // ((JComponent)targetPanel.getParent()).paintImmediately(((JComponent)targetPanel.getParent()).getBounds());
              // targetPanel.doLayout();

              // targetPanel.requestFocus();
              targetPanel.getParent().getParent().invalidate();
              targetPanel.getParent().getParent().repaint();
              // targetPanel.paintImmediately(targetPanel.getBounds());
            }
          });
        }
        // sleep
        try {
          Thread.sleep(300);
        }
        catch(InterruptedException ie) {
          log.debug("Animation interrupted", ie);
        }

        synchronized(lock) {
          isDying = dying;
        }
      }// while(!isDying)
    }

    boolean dying;

    boolean active;

    final String lock = "lock";

    JPanel targetPanel;

    JLabel imageLabel;
  }

  class LocaleSelectorMenuItem extends JRadioButtonMenuItem {
    private static final long serialVersionUID = 1L;
    public LocaleSelectorMenuItem(Locale locale) {
      super(locale.getDisplayName());
      me = this;
      myLocale = locale;
      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for(Component aRoot : MainFrame.getGuiRoots()) {
            if(aRoot instanceof Window) {
              me.setSelected(aRoot.getInputContext()
                .selectInputMethod(myLocale));
            }
          }
        }
      });
    }

    public LocaleSelectorMenuItem() {
      super("System Default  >>" + Locale.getDefault().getDisplayName() + "<<");
      me = this;
      myLocale = Locale.getDefault();
      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          for(Component aRoot : MainFrame.getGuiRoots()) {
            if(aRoot instanceof Window) {
              me.setSelected(aRoot.getInputContext()
                .selectInputMethod(myLocale));
            }
          }
        }
      });
    }

    Locale myLocale;

    JRadioButtonMenuItem me;
  }// //class LocaleSelectorMenuItem extends JRadioButtonMenuItem

  /**
   * This class represent an action which brings up the Gazetteer Editor
   * tool
   */
  class NewGazetteerEditorAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    public NewGazetteerEditorAction() {
      super("Gazetteer Editor", getIcon("gazetteer"));
      putValue(SHORT_DESCRIPTION, "Start the Gazetteer Editor");
    }

    public void actionPerformed(ActionEvent e) {
      com.ontotext.gate.vr.Gaze editor = new com.ontotext.gate.vr.Gaze();
      try {
        JFrame frame = new JFrame();
        editor.init();
        frame.setTitle("Gazetteer Editor");
        frame.getContentPane().add(editor);

        Set<LanguageResource> gazetteers = new HashSet<LanguageResource>(
          Gate.getCreoleRegister().getLrInstances(
          "gate.creole.gazetteer.DefaultGazetteer"));
        if(gazetteers.isEmpty()) { return; }
        for(LanguageResource gazetteer : gazetteers) {
          Gazetteer gaz = (Gazetteer) gazetteer;
          if(gaz.getListsURL().toString().endsWith(
            System.getProperty("gate.slug.gazetteer"))) editor.setTarget(gaz);
        }

        frame.setSize(Gaze.SIZE_X, Gaze.SIZE_Y);
        frame.setLocation(Gaze.POSITION_X, Gaze.POSITION_Y);
        frame.setVisible(true);
        editor.setVisible(true);
      }
      catch(ResourceInstantiationException error) {
        String message = "Failed to instanciate the gazetteer editor.";
        Action[] actions = {
          new AbstractAction("Load plugins manager") {
            public void actionPerformed(ActionEvent e) {
              (new ManagePluginsAction()).actionPerformed(null);
        }}};
        alertButton.setAction(new AlertAction(error, message, actions));
      }
    }// actionPerformed();
  }// class NewOntologyEditorAction

  class AlertAction extends AbstractAction {
    private Timer timer =
      new java.util.Timer("MainFrame alert tooltip hide timer", true);

    /**
     * Action for the alert button that shows a message in a popup.
     * A detailed dialog can be shown when the button or popup are clicked.
     * Log the message and error as soon as the action is created.
     * @param error can be null in case of info message.
     * @param message text to be displayed in a popup and dialogue
     * @param actions actions the user can choose in the dialogue
     */
    public AlertAction(Throwable error, String message, Action[] actions) {
      if (error == null) {
        log.info(message);
      } else {
        log.error(message, error);
      }
      String description = "<html>" + (error == null ?
        "Important information:<br>" : "There was a problem:<br>") +
        message.substring(0, Math.min(300, message.length()))
          .replaceAll("(.{40,50}(?:\\b|\\.|/))", "$1<br>") + "</html>";
      final int lines = description.split("<br>").length;
      putValue(Action.SMALL_ICON, MainFrame.getIcon("crystal-clear-app-error"));
      putValue(Action.SHORT_DESCRIPTION, description);

      this.error = error;
      this.message = message;
      if (actions == null) {
        this.actions = new Action[1];
      } else {
        this.actions = new Action[actions.length+1];
        System.arraycopy(actions, 0, this.actions, 0, actions.length);
      }
      // add a 'search in mailing list' action
      this.actions[this.actions.length-1] = new HelpMailingListAction(message);
      // show for a few seconds a popup with the error message
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (!instance.isShowing()) { return; }
          alertButton.setEnabled(true);
          JToolTip toolTip = alertButton.createToolTip();
          toolTip.setTipText(alertButton.getToolTipText());
          PopupFactory popupFactory = PopupFactory.getSharedInstance();
          final Popup popup = popupFactory.getPopup(alertButton, toolTip,
            instance.getLocationOnScreen().x+instance.getWidth()/2-100,
            instance.getLocationOnScreen().y+instance.getHeight()-30-(lines*10));
          toolTip.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
              popup.hide();
              alertButton.doClick();
            }
          });
          popup.show();
          Date timeToRun = new Date(System.currentTimeMillis() + 4000);
          timer.schedule(new TimerTask() {
            public void run() {
              SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                  popup.hide(); // hide the tooltip after some time
                }
              });
            }
          }, timeToRun);
        }
      });
    }
    public void actionPerformed(ActionEvent e) {
      ErrorDialog.show(error, message, instance, getIcon("root"), actions);
    }
    Throwable error;
    String message;
    Action[] actions;
  }

} // class MainFrame
