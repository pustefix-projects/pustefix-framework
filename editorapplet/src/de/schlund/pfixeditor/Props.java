package de.schlund.pfixeditor;

import java.lang.reflect.Method;
import java.io.*;
import java.text.*;

import java.util.*;
import java.util.Properties;
import java.util.zip.*;


/**
 * @author rouven
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Props {
  private static Properties props, defaultProps;
  private static boolean isInit=false;

 /**
   * The <code>XPropertiesHandler</code> needs to get Jext's
   * <code>Properties</code> object to achieve its purpose.
   * @return The current <code>Properties</code> object
   */

  public static Properties getProperties()
  {
    return props;
  }
  
  /**
   * Load a set of properties.
   * @param in An <code>InputStream</code> is specified to load properties from a JAR file
   * @deprecated Maintained only for plugins compliance. Use <code>loadXMLProps()</code>
   * instead of this method.
   */

  public static void loadProps(InputStream in)
  {
    try
    {
      props.load(new BufferedInputStream(in));
      in.close();
    } catch (IOException ioe) { }
  }

  /**
   * Init the properties.
   */

  public static void init()
  {    
    if (isInit)
      return;
    defaultProps=props=new Properties();
    
    setProperty("editor.splitted.orientation", "Vertical");
    setProperty("editor.autoScroll", "3");
    setProperty("editor.antiAliasing", "off");
    setProperty("editor.dirDefaultDialog", "on");
    setProperty("editor.linesInterval", "5");
    setProperty("editor.linesIntervalEnabled", "off");
    setProperty("editor.linesHighlightColor", "#e6e6ff");
    setProperty("editor.tabStop", "on");
    setProperty("editor.font", "Monospaced");
    setProperty("editor.fontSize", "12");
    setProperty("editor.tabSize", "8");
    setProperty("editor.tabIndent", "off");
    setProperty("editor.enterIndent", "on");
    setProperty("editor.blinkingCaret", "on");
    setProperty("editor.softTab", "on");
    setProperty("editor.saveSession", "on");
    setProperty("editor.colorize.mode", "plain");
    setProperty("editor.bgColor", "#ffffff");
    setProperty("editor.fgColor", "#000000");
    setProperty("editor.lineHighlight", "on");
    setProperty("editor.lineHighlightColor", "#e0e0e0");
    setProperty("editor.wrapGuideColor", "#ff0000");
    setProperty("editor.wrapGuideOffset", "0");
    setProperty("editor.wrapGuideEnabled", "off");
    setProperty("editor.bracketHighlight", "on");
    setProperty("editor.bracketHighlightColor", "#00ff00");
    setProperty("editor.eolMarkers", "off");
    setProperty("editor.eolMarkerColor", "#009999");
    setProperty("editor.caretColor", "#ff0000");
    setProperty("editor.selectionColor", "#ccccff");
    setProperty("editor.blockCaret", "off");
    
    // =========================================================== -->
    // HTML SYNTAX MODE SETTINGS                                    -->
    // ============================================================ -->
    setProperty("mode.html.name", "HTML/XML");
    setProperty("mode.html.tokenMarker", "org.gjt.sp.jedit.syntax.HTMLTokenMarker");
    setProperty("mode.html.bracketHighlight", "on");
    setProperty("mode.html.commentStart", "&lt;!--");
    setProperty("mode.html.commentEnd", "--&gt;");
//    setProperty("mode.html.indentOpenBrackets", "&lt");
//    setProperty("mode.html.indentCloseBrackets", "/&gt");
//    setProperty("mode.html.indentPrevLine", "\\s*(((if|while)\\s*\\(|else|case|default)[^;]*|for\\s*\\(.*)");
    setProperty("mode.pfix.name", "html");
    // ============================================================ -->
    // EDIT MENU SHORTCUTS                                          -->
    // ============================================================ -->
    setProperty("undo.shortcut", "C+u");
    setProperty("find.shortcut", "C+f");
    setProperty("replace.shortcut", "C+r");
    setProperty("redo.shortcut", "C+z");
    setProperty("copy.shortcut", "C+c");
    setProperty("cut.shortcut", "C+x");
    setProperty("paste.shortcut", "C+v");
    setProperty("parse.shortcut", "C+p");
    setProperty("select_all.shortcut", "C+a");
    setProperty("find_all.shortcut","F3");
    setProperty("find_next.shortcut","C+n");
    isInit=true;    
    //-- ============================================================ -->
    //-- FIND/REPLACE DIALOGS TEXT DATAS                              -->
    //-- ============================================================ -->
  setProperty("find.title", "Find");
  setProperty("find.label", "Find:");
  setProperty("find.incremental.label", "Incremental:");
  setProperty("find.button", "Find");
  setProperty("find.tip", "Find specified string");
  setProperty("find.mnemonic", "F");
  setProperty("find.ignorecase.label", "Ignore Case");
  setProperty("find.savevalues.label", "Save Values");
  setProperty("find.allFiles.label", "In All Files");
  setProperty("find.useregexp.label", "Use Extended RegExp");
  setProperty("find.all.title", "Find All");
  setProperty("find.all.button", "Find All");
  setProperty("find.all.label", "Search:");
  setProperty("find.all.tip", "Find all matches");
  setProperty("find.all.mnemonic", "F");
  setProperty("find.all.highlight.label", "Highlight");
  setProperty("find.matchnotfound", "Match not found in {0}.\nRestart from beginning ?");

  setProperty("replace.title", "Replace");
  setProperty("replace.label", "Replace with:");
  setProperty("replace.button", "Replace");
  setProperty("replace.tip", "Replace string to find");
  setProperty("replace.mnemonic", "R");
  setProperty("replace.all.button", "Replace All");
  setProperty("replace.all.tip", "Replace all matches");
  setProperty("replace.all.mnemonic", "A");
  setProperty("replace.script", "Script:");
  //-- ============================================================ -->
  //-- GENERAL MESSAGES                                             -->
  //-- ============================================================ -->
  setProperty("general.save.question", "Do you want to save changes in {0} ?");
  setProperty("general.save.title", "Save...");
  setProperty("general.deleteFile.question", "Do you want to delete file {0} ?");
  setProperty("general.deleteFile.title", "Delete...");
  setProperty("general.ok.button", "Ok");
  setProperty("general.ok.mnemonic", "O");
  setProperty("general.cancel.button", "Cancel");
  setProperty("general.cancel.mnemonic", "C");
  setProperty("general.open.button", "Open");
  setProperty("general.open.tip", "Open selected file");
  setProperty("general.open.mnemonic", "O");
  setProperty("general.unknown", "Unknown");
  setProperty("savestates", "on");
  props=new Properties(defaultProps);
/*
    usrProps = SETTINGS_DIRECTORY + ".jext-props.xml";

    File dir = new File(SETTINGS_DIRECTORY);
    if (!dir.exists())
    {
      dir.mkdir();

      dir = new File(SETTINGS_DIRECTORY + "plugins" + File.separator);
      if (!dir.exists())
        dir.mkdir();

      dir = new File(SETTINGS_DIRECTORY + "scripts" + File.separator);
      if (!dir.exists())
        dir.mkdir();

      dir = new File(SETTINGS_DIRECTORY + "xinsert" + File.separator);
      if (!dir.exists())
        dir.mkdir();
    }

    defaultProps = props = new Properties();

    /////////////////////////////////////////////////////////////////
    // DEPRECATED BY THE METHOD loadXMLProps()
    /////////////////////////////////////////////////////////////////
    //    loadProps(Jext.class.getResourceAsStream("jext-gui.keys"));
    //    loadProps(Jext.class.getResourceAsStream("jext-gui.text"));
    //    loadProps(Jext.class.getResourceAsStream("jext.props"));
    //    loadProps(Jext.class.getResourceAsStream("jext.tips"));
    /////////////////////////////////////////////////////////////////

    // loads specified language pack
    File lang = new File(SETTINGS_DIRECTORY + ".lang");

    if (lang.exists())
    {
      try
      {
        BufferedReader reader = new BufferedReader(new FileReader(lang));
        String language = reader.readLine();
        reader.close();

        if (language != null && !language.equals("English"))
        {
          File langPack = new File(JEXT_HOME + File.separator + "lang" +
                                               File.separator + language + "_pack.jar");
          if (langPack.exists())
          {
            languagePack = new ZipFile(langPack);
            languageEntries = new ArrayList();
            Enumeration entries = languagePack.entries();

            while (entries.hasMoreElements())
              languageEntries.add(entries.nextElement());

            setLanguage(language);
          } else
            lang.delete();
        }
      } catch (IOException ioe) { }
    }

    //loadXMLProps(Jext.class.getResourceAsStream("jext.props.xml"), "jext.props.xml");
    loadXMLProps(Jext.class.getResourceAsStream("jext-text.props.xml"), "jext-text.props.xml");
    loadXMLProps(Jext.class.getResourceAsStream("jext-keys.props.xml"), "jext-keys.props.xml");
    loadXMLProps(Jext.class.getResourceAsStream("jext-defs.props.xml"), "jext-defs.props.xml");
    loadXMLProps(Jext.class.getResourceAsStream("jext-tips.props.xml"), "jext-tips.props.xml");

    Properties pyProps = new Properties();
    pyProps.put("python.cachedir", SETTINGS_DIRECTORY + "pythoncache" + File.separator);
    PythonInterpreter.initialize(System.getProperties(), pyProps, new String[0]);

    initModes();
    initPlugins();

    if (usrProps != null)
    {
      props = new Properties(defaultProps);

      try
      {
        loadXMLProps(new FileInputStream(USER_PROPS), ".jext-props.xml");

        if (DELETE_OLD_SETTINGS)
        {
          String pVersion = getProperty("properties.version");
          if (pVersion == null || BUILD.compareTo(pVersion) > 0)
          {
            File userSettings = new File(USER_PROPS);
            if (userSettings.exists())
            {
              userSettings.delete();
              defaultProps = props = new Properties();
              //loadXMLProps(Jext.class.getResourceAsStream("jext.props.xml"), "jext.props.xml");
              loadXMLProps(Jext.class.getResourceAsStream("jext-text.props.xml"), "jext-text.props.xml");
              loadXMLProps(Jext.class.getResourceAsStream("jext-keys.props.xml"), "jext-keys.props.xml");
              loadXMLProps(Jext.class.getResourceAsStream("jext-defs.props.xml"), "jext-defs.props.xml");
              loadXMLProps(Jext.class.getResourceAsStream("jext-tips.props.xml"), "jext-tips.props.xml");
              JARClassLoader.reloadPluginsProperties();
              props = new Properties(defaultProps);
            }
          }
        }

      } catch (FileNotFoundException fnfe) {
      } catch (IOException ioe) { }
    }

    Search.load();
    initUI();

    if (Utilities.JDK_VERSION.charAt(2) >= '4')
    {
      try
      {
        Class cl = Class.forName("org.jext.JavaSupport");
        Method m = cl.getMethod("initJavaSupport", new Class[0]);
        if (m !=  null)
          m.invoke(null, new Object[0]);
      } catch (Exception e) { }
    }

    // Add our protocols to java.net.URL's list
    System.getProperties().put("java.protocol.handler.pkgs", "org.jext.protocol|" +
                               System.getProperty("java.protocol.handler.pkgs", ""));

    initActions();
    JARClassLoader.initPlugins();
    sortModes();

    assocPluginsToModes();
    */
  }


  /**
   * Load a set of properties from an XML file.
   * @param in An <code>InputStream</code> is specified to load properties from a JAR file
   * @param fileName The XML filename
   */

  public static void loadXMLProps(InputStream in, String fileName)
  {
    //XPropertiesReader.read(in, fileName);
  }

  /**
   * Load a set of actions from an XML file.
   * @param in An <code>InputStream</code> is specified to load properties from a JAR file
   * @param fileName The XML filename
   */

  public static void loadXMLActions(InputStream in, String fileName)
  {
    //PyActionsReader.read(in, fileName);
  }

  /**
   * Load a set of actions from an XML file.
   * @param in An <code>InputStream</code> is specified to load properties from a JAR file
   * @param fileName The XML filename
   */

  public static void loadXMLOneClickActions(InputStream in, String fileName)
  {
    //OneClickActionsReader.read(in, fileName);
  }

  /**
   * Returns an input stream corresponding to selected language.
   * @param in The default stream
   * @param fileName The requested file
   */

  /**
   * Set a property.
   * @param name Property's name
   * @param value The value to store as <code>name</code>
   */

  public static void setProperty(String name, String value)
  {
    if (name == null || value == null)
      return;
    props.put(name, value);
  }
  
  /**
   * Returns true if the property value equals to "on" or "true"
   * @param name The name of the property to read
   */

  public static boolean getBooleanProperty(String name)
  {
    String p = getProperty(name);
    if (p == null)
      return false;
    else
      return p.equals("on") || p.equals("true");
  }

  /**
   * Returns true if the property value equals to "on" or "true"
   * @param name The name of the property to read
   */

  public static boolean getBooleanProperty(String name, String def)
  {
    String p = getProperty(name, def);
    if (p == null)
      return false;
    else
      return p.equals("on") || p.equals("true");
  }

  /**
   * If we store properties, we need to read them, too !
   * @param name The name of the property to read
   * @return The value of the specified property
   */

  public static String getProperty(String name)
  {
    return props.getProperty(name);
  }

  /**
   * Fetches a property, returning the default value if it's not
   * defined.
   * @param name The property
   * @param def The default value
   */

  public static final String getProperty(String name, String def)
  {
    return props.getProperty(name, def);
  }

  /**
   * Returns the property with the specified name, formatting it with
   * the <code>java.text.MessageFormat.format()</code> method.
   * @param name The property
   * @param args The positional parameters
   */

  public static final String getProperty(String name, Object[] args)
  {
    if (name == null)
      return null;

    if (args == null)
      return props.getProperty(name, name);
    else
      return MessageFormat.format(props.getProperty(name, name), args);
  }

  /**
   * Unsets (clears) a property.
   * @param name The property
   */

  public static void unsetProperty(String name)
  {
    if (defaultProps.get(name) != null)
      props.put(name, "");
    else
      props.remove(name);
  }
}