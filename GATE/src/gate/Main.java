/*
 *  Main.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 1/Nov/00
 *
 *  $Id: Main.java,v 1.1 2011/01/13 16:50:46 textmine Exp $
 */

package gate;

import gate.gui.MainFrame;
import gate.gui.OptionsDialog;
import gate.gui.ShellSlacFrame;
import gate.gui.Splash;
import gate.gui.UserGroupEditor;
import gate.util.BomStrippingInputStreamReader;
import gate.util.CorpusBenchmarkTool;
import gate.util.Err;
import gate.util.Files;
import gate.util.GateException;
import gate.util.OptionsMap;
import gate.util.Out;
import gate.util.Strings;
import gnu.getopt.Getopt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/** Top-level entry point for the GATE command-line and GUI interfaces.
  * <P>
  */
public class Main {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Status flag for normal exit. */
  private static final int STATUS_NORMAL = 0;

  /** Status flag for error exit. */
  private static final int STATUS_ERROR = 1;

  /** Main routine for GATE.
    * Command-line arguments:
    * <UL>
    * <LI>
    * <B>-h</B> display a short help message
    * <LI>
    * <B>-d URL</B> define URL to be a location for CREOLE resoures
    * <LI>
    * <B>-i file</B> additional initialisation file (probably called
    *   <TT>gate.xml</TT>). Used for site-wide initialisation by the
    *   start-up scripts
    * <LI>
    * <B>-a</B> run the DB administration tool
    * </UL>
    */
  public static void main(String[] args) throws GateException {
    Main.annotatorArgsMap = null;
    // check we have a useable JDK
    if(
      System.getProperty("java.version").compareTo(Gate.getMinJdkVersion())
      < 0
    ) {
      throw new GateException(
        "GATE requires JDK " + Gate.getMinJdkVersion() + " or newer"
      );
    }

    // process command-line options
    processArgs(args);

    // GATE builtins should be loaded from the jar (or classes dir), not
    // from a web server (we load them over the web during testing to
    // make sure that users can load their own that way)
    Gate.setNetConnected(false);
    Gate.setLocalWebServer(false);


    // run the interface or do batch processing
    if(batchMode) {
      if(DEBUG) Out.prln("running batch process");
      batchProcess();
    } else if(dbAdminMode) {
      if(DEBUG) Out.prln("running dbAdmin");
      dbAdmin();
    } else {
      runGui();
    }
//    Gate.setUseXMLSerialization(false);
  } // main

  /** Register any CREOLE URLs that we got on the command line */
  private static void registerCreoleUrls() {
    CreoleRegister reg = Gate.getCreoleRegister();
    Iterator<URL> iter = pendingCreoleUrls.iterator();
    while(iter.hasNext()) {
      URL u = iter.next();
      try {
        reg.registerDirectories(u);
      } catch(GateException e) {
        Err.prln("Couldn't register CREOLE directory: " + u);
        Err.prln(e);
        System.exit(STATUS_ERROR);
      }
    }
  } // registerCreoleUrls()

  /** Main Frame of the GUI; null when no GUI running */
  private static MainFrame frame;

  /** The splash shown when Gate starts*/
  private static Splash splash;

  /**
   * Get the main frame of the GUI. If the GUI isn't running, it
   * is started.
   */
  public static MainFrame getMainFrame() throws GateException {
    if(frame == null)
      runGui();
    return frame;
  } // getMainFrame()

  /** Run the user interface. */
  private static void runGui() throws GateException {
    try {
      Class rmClass =
              Gate.class
                      .getClassLoader()
                      .loadClass(
                              "org.jdesktop.swinghelper.debug.CheckThreadViolationRepaintManager");

      RepaintManager.setCurrentManager((RepaintManager)rmClass.getConstructor()
              .newInstance());
    } catch(Exception e) {
      // the debug classes from SwingHelper are not available
    }

    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
    //show the splash
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        //build the Splash
        JPanel splashBox = new JPanel();
        splashBox.setLayout(new GridBagLayout());
        splashBox.setBackground(Color.white);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);

        String splashName =
          System.getProperty(GateConstants.APP_SPLASH_JAVA_PROPERTY_NAME);
        if(splashName == null)
          splashName = "splash";

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        JLabel gifLbl = new JLabel(MainFrame.getIcon(splashName));
        splashBox.add(gifLbl, constraints);
        GraphicsConfiguration gc = GraphicsEnvironment.
          getLocalGraphicsEnvironment().getDefaultScreenDevice().
          getDefaultConfiguration();

        splash = new Splash(null, gc, splashBox);
        splash.showSplash();
      }
    });

    // initialise the library and load user CREOLE directories
    try{
      Gate.init();
    }catch(Throwable t){
      int selection = JOptionPane.showOptionDialog(
        null,
        "Error during initialisation:\n" + t.toString() +
        "\nDo you still want to start GATE?",
        "GATE", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE,
        null, new String[]{"Cancel", "Start anyway"},
        "Cancel");
      if(selection != 1){
        t.printStackTrace();
        System.exit(1);
      }
    }


    //create the main frame, show it and hide the splash
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        GraphicsConfiguration gc = GraphicsEnvironment.
        getLocalGraphicsEnvironment().getDefaultScreenDevice().
        getDefaultConfiguration();

        //this needs to run before any GUI component is constructed.
        //the initial gate splash is exempted from this rule.
        applyUserPreferences();

        //all the defaults tables have been updated; build the GUI
        if(Gate.isSlugGui()) {
          frame = new ShellSlacFrame();
          if(DEBUG) Out.prln("constructing SLUG GUI");
        }
        else {
          frame = new MainFrame(gc);
          if(DEBUG) Out.prln("constructing GUI");
        } // if - SLUG

        // run the GUI
        frame.setTitleChangable(true);
        if(Gate.isSlugGui()) {
          frame.setTitle("SLUG application");
        }
        else {
          frame.setTitle(name + " " + version + " build " + build);
        } // if - SLUG

        // Set title from Java properties
        String title =
          System.getProperty(GateConstants.TITLE_JAVA_PROPERTY_NAME);
        if(title != null) {
          frame.setTitle(title);
        } // if
        frame.setTitleChangable(false);

        // Set icon from Java properties
        // iconName could be absolute or "gate:/img/..."
        String iconName =
          System.getProperty(GateConstants.APP_ICON_JAVA_PROPERTY_NAME);
        if(iconName != null) {
          try {
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
                  new URL(iconName)));
          } catch(MalformedURLException mue){
            mue.printStackTrace(Err.getPrintWriter());
          }
        } // if

        // Validate frames that have preset sizes
        frame.validate();

        // Center the window
        Rectangle screenBounds = gc.getBounds();
        Dimension screenSize = screenBounds.getSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
          frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
          frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);

        frame.setVisible(true);
        if(splash != null) {
          splash.setVisible(false);
          splash.dispose();
        }

        if(!Gate.isSlugGui()) {
          //load session if required and available;
          //do everything from a new thread.
          Runnable runnable = new Runnable(){
            public void run(){
              try{
                File sessionFile = Gate.getUserSessionFile();
                if(sessionFile.exists()){
                  MainFrame.lockGUI("Loading saved session...");
                  gate.util.persistence.PersistenceManager.loadObjectFromFile(sessionFile);
                }
              }catch(Exception e){
                Err.prln("Failed to load session data:");
                e.printStackTrace(Err.getPrintWriter());
              }finally{
                MainFrame.unlockGUI();
              }
            }
          };
          Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                     runnable, "Session loader");
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        } // if - when no SLUG GUI load session
      }
    });
    registerCreoleUrls();
  } // runGui()

  /** Run the db admin interface. */
  private static void dbAdmin() throws GateException {
    try { UserGroupEditor.main(null); } catch(Exception e) {
      throw new GateException(e);
    }
  } // dbAdmin()

  /**
   * Reads the user config data and applies the required settings.
   * This must be called <b>after</b> {@link Gate#init()} but <b>before</b>
   * any GUI components are created.
   */
  public static void applyUserPreferences(){
    //look and feel
    String lnfClassName = Gate.getUserConfig().
                          getString(GateConstants.LOOK_AND_FEEL);
    if(lnfClassName == null){
      //if running on Linux, default to Metal rather than GTK because GTK LnF
      //doesn't play nicely with most Gnome themes
      if(System.getProperty("os.name").toLowerCase().indexOf("linux") != -1){
        //running on Linux
        lnfClassName = UIManager.getCrossPlatformLookAndFeelClassName();
      }else{
        lnfClassName = UIManager.getSystemLookAndFeelClassName();
      }
      Gate.getUserConfig().put(GateConstants.LOOK_AND_FEEL, lnfClassName);
    }
    try {
      UIManager.setLookAndFeel(lnfClassName);
    } catch(Exception e) {
      System.err.print("Could not set your preferred Look and Feel. The error was:\n" +
              e.toString() + "\nReverting to using Java Look and Feel");
      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      }catch(Exception e1) {
        //we just can't catch a break here. Let's forget about look and feel.
        System.err.print(
                "Could not set the cross-platform Look and Feel either. The error was:\n" +
                e1.toString() + "\nGiving up on Look and Feel.");
      }
    }

    //read the user config data
    OptionsMap userConfig = Gate.getUserConfig();

    //text font
    Font font = userConfig.getFont(GateConstants.TEXT_COMPONENTS_FONT);
    if(font == null){
      String fontName = Gate.guessUnicodeFont();
      if(fontName != null){
        font = new Font(fontName, Font.PLAIN, 12);
      }else{
        font = UIManager.getFont("TextPane.font");
      }
    }

    if(font != null){
      OptionsDialog.setTextComponentsFont(font);
    }

    //menus font
    font = userConfig.getFont(GateConstants.MENUS_FONT);
    if(font == null){
      String fontName = Gate.guessUnicodeFont();
      if(fontName != null){
        font = new Font(fontName, Font.PLAIN, 12);
      }else{
        font = UIManager.getFont("Menu.font");
      }
    }

    if(font != null){
      OptionsDialog.setMenuComponentsFont(font);
    }

    //other gui font
    font = userConfig.getFont(GateConstants.OTHER_COMPONENTS_FONT);
    if(font == null){
      String fontName = Gate.guessUnicodeFont();
      if(fontName != null){
        font = new Font(fontName, Font.PLAIN, 12);
      }else{
        font = UIManager.getFont("Button.font");
      }
    }

    if(font != null){
      OptionsDialog.setComponentsFont(font);
    }


  }



  // find out the version and build numbers
  static {
    // find out the version number
    try {
      InputStream ver = Files.getGateResourceAsStream("version.txt");
      if (ver==null) {
        throw new IOException();
      }
      BufferedReader reader = new BomStrippingInputStreamReader(ver, "UTF-8");
      Main.version = reader.readLine();
    } catch(IOException ioe) {
      Main.version = "5.0";
    }

    // find out the build number
    try{
      InputStream build = Files.getGateResourceAsStream("build.txt");
      if (build==null) {
        throw new IOException();
      }
      BufferedReader reader = new BomStrippingInputStreamReader(build, "UTF-8");
      Main.build = reader.readLine();
    } catch(IOException ioe) {
      Main.build = "0000";
    }
  } // static initializer finding build and version


/**

<BR>
<B>Options processing: </B>

<BR>
<TABLE>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -a annotator arg(s)
    </TH>
    <TH ALIGN=left>
    A CREOLE annotator to run on the collection, with zero or more
    arguments. The set of such annotators will be run in the sequence
    they appear in the arguments list. The arguments list must end with the
    start of another option; otherwise add a "-" after the arguments to
    terminate the list.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -b
    </TH>
    <TH ALIGN=left>
    Batch mode. Don't start the GUI, just process options and exit after
    any actions (e.g. running annotators).
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -c collname
    </TH>
    <TH ALIGN=left>
    Name of the collection to use. If the collection already exists then
    it will be used as it stands, otherwise it will be created. See also
    -f.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -d
    </TH>
    <TH ALIGN=left>
    Destroy the collection after use. (The default is to save it to
    disk.)
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -f file(s)
    </TH>
    <TH ALIGN=left>
    One or more files to create a collection with. If the collection
    being used (see -c) already exists, these files are ignored.
    Otherwise they are used to create the collection.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -h
    </TH>
    <TH ALIGN=left>
    Print a usage message and exit.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -p creolepath
    </TH>
    <TH ALIGN=left>
    Sets the search path for CREOLE modules.
    </TH>
  </TR>
  <TR>
    <TH ALIGN=left COLSPAN=15>
    -v classname(s)
    </TH>
    <TH ALIGN=left>
    Verbose: turns on debugging output. Takes zero or more class names
    to debug.
    </TH>
  </TR>
</TABLE>

*/
  /** Name of the collection we were asked to process. */
  private static String collName;

  /** Search path for CREOLE modules. */
  private static String creolePath;

  /** List of files we were asked to build a collection from. */
  private static List fileNames = new ArrayList();

  /** List of annotators we were asked to run on the collection. */
  private static List annotatorNames = new ArrayList();

  /** Map of annotator arguments. */
  private static Map annotatorArgsMap = new HashMap();

  /** List of classes we were asked to debug. */
  private static List debugNames = new ArrayList();

  /** Are we in batch mode? */
  public static boolean batchMode = false;

  /** Are we in db admin mode? */
  public static boolean dbAdminMode = false;

  /** Don't save collection after batch? */
  private static boolean destroyColl = false;

  /** Verbose? */
  private static boolean verbose = false;

  private static boolean runCorpusBenchmarkTool = false;

  public static String name = "GATE Developer";
  public static String version;
  public static String build;

  /** Process arguments and set up member fields appropriately.
    * Will shut down the process (via System.exit) if there are
    * incorrect arguments, or if the arguments ask for something
    * simple like printing the help message.
    */
  public static void processArgs(String[] args) {

    Getopt g = new Getopt("GATE main", args, "hd:ei:asj");
    int c;
    while( (c = g.getopt()) != -1 )
      switch(c) {
        // -a
        case 'a':
          dbAdminMode = true;
          break;
        // -h
        case 'h':
          help();
          usage();
          System.exit(STATUS_NORMAL);
          break;
        // -d creole-dir
        case 'd':
          String urlString = g.getOptarg();
          URL u = null;
          try {
            u = new URL(urlString);
          } catch(MalformedURLException e) {
            Err.prln("Bad URL: " + urlString);
            Err.prln(e);
            System.exit(STATUS_ERROR);
          }
          pendingCreoleUrls.add(u);
          Out.prln(
            "CREOLE Directory " + urlString + " queued for registration"
          );
          break;
        // -i gate.xml site-wide init file
        case 'i':
          String optionString = g.getOptarg();
          URL u2 = null;
          File f = new File(optionString);
          try {
            u2 = f.toURI().toURL();
          } catch(MalformedURLException e) {
            Err.prln("Bad initialisation file: " + optionString);
            Err.prln(e);
            System.exit(STATUS_ERROR);
          }
          Gate.setSiteConfigFile(f);
          if(DEBUG)
            Out.prln(
              "Initialisation file " + optionString +
              " recorded for initialisation"
            );
          break;
        // -e runs the CorpusBenchmarkTool (e for evaluate)
        case 'e':
          try {
            CorpusBenchmarkTool.main(args);
          } catch (GateException ex) {
            Out.prln("Error running the evaluation tool: " + ex.getMessage());
            System.exit(-1);
          }
          break;
        // -s runs the SLUG GUI
        case 's':
          Gate.setSlugGui(true);
          break;
/*
        // -c collname
        case '-c':
          collName = g.getOptarg();
          break;

        // -b
        case '-b':
          batchMode = true;
          break;

        // -a annotator(s)
        case '-a':
          if(++i == args.length) { usage(); return; }
          String annotatorName = g.getOptarg();
          annotatorNames.add(annotatorName);
// collect any args for the annotator
          break;

        // -d
        case '-d':
          destroyColl = true;
          break;

        // -f file(s)
        case '-f':
          while(++i < args.length)
            if(args[i].toCharArray()[0] == '-') { // start of another option
              i--;
              break;
            }
            else
              fileNames.add(args[i]);
          break;

        // -p creolepath
        case '-p':
          if(++i < args.length)
            creolePath = args[i];
          else
            { usage(); return; }
          break;

        // -v classname(s)
        case '-v':
          verbose = true;
          Debug.setDebug(true);
          while(++i < args.length) {
            if(args[i].toCharArray()[0] == '-') { // start of another option
              i--;
              break;
            }
            else
              debugNames.add(args[i]);
          } // while
          break;
*/

        case '?':
          // leave the warning to getopt
          System.exit(STATUS_ERROR);
          break;

        default:
          // shouldn't happen!
          Err.prln("getopt() returned " + c + "\n");
          System.exit(STATUS_ERROR);
          break;
      } // getopt switch

  } // processArgs()

  /** Run commands as a batch process. */
  private static void batchProcess() throws GateException{
    // initialise the library and load user CREOLE directories
    Gate.init();
    registerCreoleUrls();

/*
    // turn debugging on where requested
    if(verbose) {
      for(ArrayIterator i = debugNames.begin(); ! i.atEnd(); i.advance()) {
        try { Debug.setDebug(Class.forName(((String) i.get())), true); }
        catch(ClassNotFoundException e) {
          System.err.println(
            "can't debug class " + (String) i.get() + ": " + e.toString()
          );
        }
      } // for
    } // debugging on

    // collection: does it exist and can we open it?
    if(collName == null) {
      System.err.println("no collection name given");
      usage();
      return;
    }
    File collDir = new File(collName);
    JdmCollection coll = null;
    if(collDir.exists()) { // open collection
      Debug.prnl("opening collection " + collName);
      try {
        coll = new JdmCollection(collName);
      } catch (JdmException e) {
        System.err.println(
          "Couldn't open collection " + collName + " " + e.toString()
        );
        return;
      }
    } else { // create collection and add documents
      Debug.prnl("creating collection " + collName);
      JdmAttributeSequence attrs = new JdmAttributeSequence();
      try {
        coll = new JdmCollection(collName, attrs);
      } catch (JdmException e) {
        System.err.println(
          "Couldn't create collection " + collName + " " + e.toString()
        );
        return;
      }

      // add the documents to the collection
      for(ArrayIterator i = fileNames.begin(); ! i.atEnd(); i.advance()) {
        Debug.prnl("adding document " + (String) i.get());
        try {
          JdmDocument doc = coll.createDocument(
            (String) i.get(),
            null,
            new JdmAnnotationSet(),
            new JdmAttributeSequence()
          );
        } catch (JdmException e) {
          System.err.println(
             "Can't add document " + (String) i.get() + ": " + e.toString()
          );
        } // catch
      } // for each filename
    } // collection create

    // run the annotators on each document in the collection
    // for each document
    JdmDocument doc = null;
    if(coll.length() > 0)
      try{ doc = coll.firstDocument(); } catch(JdmException e) { }
    for(int i = 0; i<coll.length(); i++) {
      if(doc == null) continue; // first and next doc shouldn't throw excptns!

      // for each annotator
      for(ArrayIterator j = annotatorNames.begin(); !j.atEnd(); j.advance()) {
        String annotatorName = (String) j.get();
        Debug.prnl(
          "calling annotator " + annotatorName + " on doc " + doc.getId()
        );

        // load the annotator class
        Annotator annotator = null;
        Class annotatorClass = null;
        try {
          // cheat and assume that all annotators are on CLASSPATH
          annotatorClass = Class.forName(annotatorName);
        } catch (Exception ex) {
          System.err.println(
            "Could load class for CREOLE object " + annotatorName + ": " +
            ex.toString()
          );
          continue;
        }

        // construct the annotator
        try {
          annotator = (Annotator) annotatorClass.newInstance();
        } catch (Throwable ex) { // naughty chap
          System.err.println(
            "Could create instance of CREOLE object " + annotatorName + ": " +
            ex.toString()
          );
          continue;
        }

        // annotate this document
        String[] args = (String[]) annotatorArgsMap.get(annotatorName);
        if(args == null) args = new String[0];
        annotator.annotate(doc, args);
      } // for each annotator

      doc = null;
      try { doc = coll.nextDocument(); } catch(JdmException e) { }
    } // for each doc, annotate

    // save collection?
    if(! destroyColl) {
      Debug.prnl("saving the collection");
      try {
        coll.sync();
      } catch (JdmException e) {
        System.err.println(
          "Can't save collection " + collName + ": " + e.toString()
        );
      }
    } else {
      Debug.prnl("destroying collection");
      try { coll.destroy(); } catch(JdmException e) {
        // if we didn't sync we can't destroy, but that's not an error
      }
    }

    Debug.prnl("done batch process");
*/
  } // batchProcess()

  /** Display a usage message */
  public static void usage() {
    Out.prln(
      "Usage: java gate.Main " +
      "[ -h [-d CREOLE-URL]" +
      ""
    );
  } // usage()

  /** Display a help message */
  public static void help() {
    String nl = Strings.getNl();
    Out.prln(
      "For help on command-line options and other information " + nl +
      "see the user manual in your GATE distribution or at " + nl +
      "http://gate.ac.uk/userguide/"
    );
  } // help()

  /** The list of pending URLs to add to the CREOLE register */
  private static List<URL> pendingCreoleUrls = new ArrayList<URL>();

} // class Main
