/*
 *  CreoleXmlHandler.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 1/Sept/2000
 *
 *  $Id: CreoleXmlHandler.java,v 1.1 2011/01/13 16:51:26 textmine Exp $
 */

package gate.creole;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.swing.text.html.HTMLDocument.HTMLReader.HiddenAction;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import gate.*;
import gate.util.*;
import gate.xml.SimpleErrorHandler;

/** This is a SAX handler for processing <CODE>creole.xml</CODE> files.
  * It would have been better to write it using DOM or JDOM but....
  * Resource data objects are created and added to the CREOLE register.
  * URLs for resource JAR files are added to the GATE class loader.
  */
public class CreoleXmlHandler extends DefaultHandler {

  /** A stack to stuff PCDATA onto for reading back at element ends.
   *  (Probably redundant to have a stack as we only push one item
   *  onto it. Probably. Ok, so I should check, but a) it works, b)
   *  I'm bald already and c) life is short.)
   */
  private Stack contentStack = new Stack();

  /** The current resource data object */
  private ResourceData resourceData;

  /** The current parameter list */
  private ParameterList currentParamList = new ParameterList();

  /** The current parameter disjunction */
  private List currentParamDisjunction = new ArrayList();

  /** The current parameter */
  private Parameter currentParam;

  /** The current element's attribute list */
  private Attributes currentAttributes;

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The source URL of the directory file being parsed. */
  private URL sourceUrl;
  
  /**
   * The URL to the creole.xml file being parsed.
   */
  private URL creoleFileUrl;

  /** This object indicates what to do when the parser encounts an error*/
  private SimpleErrorHandler _seh = new SimpleErrorHandler();

  /** This field represents the params map required for autoinstantiation
    * Its a map from param name to param value.
    */
  private FeatureMap currentAutoinstanceParams = null;

  /** This field holds autoinstanceParams describing the resource that
    * needs to be instantiated
    */
  private List currentAutoinstances = null;


  /** This is used to capture all data within two tags before calling the actual characters method */
  private StringBuffer contentBuffer = new StringBuffer("");

  /** This is a variable that shows if characters have been read */
  private boolean readCharacterStatus = false;

  /** Construction */
  public CreoleXmlHandler(CreoleRegister register, URL directoryUrl, 
          URL creoleFileUrl) {
    this.register = register;
    this.sourceUrl = directoryUrl;
    this.creoleFileUrl = creoleFileUrl;
    currentParam = new Parameter(this.creoleFileUrl);
  } // construction

  /** The register object that we add ResourceData objects to during parsing.
    */
  private CreoleRegister register;

  /** Called when the SAX parser encounts the beginning of the XML document */
  public void startDocument() throws GateSaxException {
    if(DEBUG) Out.prln("start document");
  } // startDocument

  /** Called when the SAX parser encounts the end of the XML document */
  public void endDocument() throws GateSaxException {
    if(DEBUG) Out.prln("end document");
    if(! contentStack.isEmpty()) {
      StringBuffer errorMessage =
        new StringBuffer("document ended but element stack not empty:");
      while(! contentStack.isEmpty())
        errorMessage.append(Strings.getNl()+"  "+(String) contentStack.pop());
      throw new GateSaxException(errorMessage.toString());
    }
  } // endDocument

  /** A verboase method for Attributes*/
  private String attributes2String(Attributes atts){
    StringBuffer strBuf = new StringBuffer("");
    if (atts == null) return strBuf.toString();
    for (int i = 0; i < atts.getLength(); i++) {
     String attName  = atts.getQName(i);
     String attValue = atts.getValue(i);
     strBuf.append(" ");
     strBuf.append(attName);
     strBuf.append("=");
     strBuf.append(attValue);
    }// End for
    return strBuf.toString();
  }// attributes2String()

  /** Called when the SAX parser encounts the beginning of an XML element */
  public void startElement (String uri, String qName, String elementName,
                                                             Attributes atts) throws SAXException {

    // call characterActions
    if(readCharacterStatus) {
      readCharacterStatus = false;
      charactersAction(new String(contentBuffer).toCharArray(),0,contentBuffer.length());
    }

    if(DEBUG) {
      Out.pr("startElement: ");
      Out.println(
        elementName + " " +
        attributes2String(atts)
      );
    }

    // create a new ResourceData when it's a RESOURCE element
    if(elementName.toUpperCase().equals("RESOURCE")) {
      resourceData = new ResourceData();
      resourceData.setFeatures(Factory.newFeatureMap());
      currentAutoinstances = new ArrayList();
    }// End if RESOURCE

    // record the attributes of this element
    currentAttributes = atts;

    // When an AUTOINSTANCE element is found a params FeatureMap will
    // be prepared in order for the resource to be instantiated
    if (elementName.toUpperCase().equals("AUTOINSTANCE")){
      currentAutoinstanceParams = Factory.newFeatureMap();
    }// End if AUTOINSTANCE

    //When an HIDDEN-AUTOINSTANCE element is found a params FeatureMap will
    // be prepared in order for the resource to be instantiated
    if (elementName.toUpperCase().equals("HIDDEN-AUTOINSTANCE")){
      currentAutoinstanceParams = Factory.newFeatureMap();
      Gate.setHiddenAttribute(currentAutoinstanceParams, true);
    }// End if AUTOINSTANCE
    
    // When a PARAN start element is found, the parameter would be instantiated
    // with a value and added to the autoinstanceParams
    if (elementName.toUpperCase().equals("PARAM")){
      // The autoinstanceParams should always be != null because of the fact
      // that PARAM is incuded into an AUTOINSTANCE element.
      // IF a AUTOINSTANCE starting element would be missing then the
      // parser would signal this later....
      if (currentAutoinstanceParams == null)
        currentAutoinstanceParams = Factory.newFeatureMap();
      // Take the param's name and value
      String paramName = currentAttributes.getValue("NAME");
      String paramStrValue = currentAttributes.getValue("VALUE");
      if (paramName == null)
        throw new GateRuntimeException ("Found in creole.xml a PARAM element" +
        " for resource "+ resourceData.getClassName()+ " without a NAME"+
        " attribute. Check the file and try again.");
      if (paramStrValue == null)
        throw new GateRuntimeException("Found in creole.xml a PARAM element"+
        " for resource "+ resourceData.getClassName()+ " without a VALUE"+
        " attribute. Check the file and try again.");
      // Add the paramname and its value to the autoinstanceParams
      currentAutoinstanceParams.put(paramName,paramStrValue);
    }// End if PARAM

    // process attributes of parameter and GUI elements
    if(elementName.toUpperCase().equals("PARAMETER")) {
      if(DEBUG) {
        for(int i=0, len=currentAttributes.getLength(); i<len; i++) {
          Out.prln(currentAttributes.getLocalName(i));
          Out.prln(currentAttributes.getValue(i));
        }// End for
      }// End if
      currentParam.comment = currentAttributes.getValue("COMMENT");
      currentParam.helpURL = currentAttributes.getValue("HELPURL");
      currentParam.defaultValueString = currentAttributes.getValue("DEFAULT");
      currentParam.optional =
        Boolean.valueOf(currentAttributes.getValue("OPTIONAL")).booleanValue();
      currentParam.name = currentAttributes.getValue("NAME");
      currentParam.runtime =
        Boolean.valueOf(currentAttributes.getValue("RUNTIME")).booleanValue();
      currentParam.itemClassName =
                                currentAttributes.getValue("ITEM_CLASS_NAME");
      // read the suffixes and transform them to a Set of Strings
      String suffixes = currentAttributes.getValue("SUFFIXES");
      Set suffiexesSet = null;
      if (suffixes != null){
        suffiexesSet = new HashSet();
        StringTokenizer strTokenizer = new StringTokenizer(suffixes,";");
        while(strTokenizer.hasMoreTokens()){
           suffiexesSet.add(strTokenizer.nextToken());
        }// End while
      }// End if
      currentParam.suffixes = suffiexesSet;
    }else if(elementName.toUpperCase().equals("GUI")){
      String typeValue = currentAttributes.getValue("TYPE");
      if (typeValue != null){
        if (typeValue.toUpperCase().equals("LARGE"))
          resourceData.setGuiType(ResourceData.LARGE_GUI);
        if (typeValue.toUpperCase().equals("SMALL"))
          resourceData.setGuiType(ResourceData.SMALL_GUI);
      }// End if
    }// End if

    // if there are any parameters awaiting addition to the list, add them
    // (note that they're not disjunctive or previous "/OR" would have got 'em)
    if(elementName.toUpperCase().equals("OR")) {
      if(! currentParamDisjunction.isEmpty()) {
        currentParamList.addAll(currentParamDisjunction);
        currentParamDisjunction = new ArrayList();
      }// End if
    }// End if
  } // startElement()

  /** Utility function to throw exceptions on stack errors. */
  private void checkStack(String methodName, String elementName)
  throws GateSaxException {
    if(contentStack.isEmpty())
      throw new GateSaxException(
        methodName + " called for element " + elementName + " with empty stack"
      );
  } // checkStack

  /** Called when the SAX parser encounts the end of an XML element.
    * This is where ResourceData objects get values set, and where
    * they are added to the CreoleRegister when we parsed their complete
    * metadata entries.
    */
  public void endElement (String uri, String qName, String elementName)
                                                    throws GateSaxException, SAXException {
    // call characterActions
    if(readCharacterStatus) {
      readCharacterStatus = false;
      charactersAction(new String(contentBuffer).toCharArray(),0,contentBuffer.length());
    }

    if(DEBUG) Out.prln("endElement: " + elementName);

    //////////////////////////////////////////////////////////////////
    if(elementName.toUpperCase().equals("RESOURCE")) {
      // check for validity of the resource data
      if(! resourceData.isValid())
        throw new GateSaxException(
          "Invalid resource data: " + resourceData.getValidityMessage()
        );

      //set the URL to the creole.xml file on the resource data object
      resourceData.setXmlFileUrl(creoleFileUrl);
      // add the new resource data object to the creole register
      register.put(resourceData.getClassName(), resourceData);
      // if the resource is auto-loading, try and load it
      if(resourceData.isAutoLoading())
        try {
          Class resClass = resourceData.getResourceClass();
//          Resource res = Factory.createResource(
//              resourceData.getClassName(), Factory.newFeatureMap()
//          );
//          resourceData.makeInstantiationPersistant(res);
        } catch(ClassNotFoundException e) {
          throw new GateSaxException(
            "Couldn't load autoloading resource: " +
            resourceData.getName() + "; problem was: " + e
          );
        }// End try

      // if there are any parameters awaiting addition to the list, add them
      // (note that they're not disjunctive or the "/OR" would have got them)
      if(! currentParamDisjunction.isEmpty()) {
        currentParamList.addAll(currentParamDisjunction);
        currentParamDisjunction = new ArrayList();
      }// End if

      // add the parameter list to the resource (and reinitialise it)
      resourceData.setParameterList(currentParamList);
      currentParamList = new ParameterList();

      if(DEBUG) Out.println("added: " + resourceData);
      // Iterate through autoinstances and try to instanciate them
      if ( currentAutoinstances != null && !currentAutoinstances.isEmpty()){
        Iterator iter = currentAutoinstances.iterator();
        while (iter.hasNext()){
          FeatureMap autoinstanceParams = (FeatureMap) iter.next();
          iter.remove();
          FeatureMap autoinstanceFeatures = null;
          //if the hidden attribute was set in the parameters, create a feature 
          //map and move the hidden attribute there.
          if(Gate.getHiddenAttribute(autoinstanceParams)){
            autoinstanceFeatures = Factory.newFeatureMap();
            Gate.setHiddenAttribute(autoinstanceFeatures, true);
            autoinstanceParams.remove(GateConstants.HIDDEN_FEATURE_KEY);
          }
          // Try to create the resource.
          try {
            // Resource res = 
            Factory.createResource(
                              resourceData.getClassName(), autoinstanceParams, 
                              autoinstanceFeatures);
            //resourceData.makeInstantiationPersistant(res);
            // all resource instantiations are persistent
          } catch(ResourceInstantiationException e) {
            throw new GateSaxException(
              "Couldn't auto-instantiate resource: " +
              resourceData.getName() + "; problem was: " + e
            );
          }// End try
        }// End while
      }// End if
      currentAutoinstances = null;
    // End RESOURCE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("AUTOINSTANCE") ||
            elementName.toUpperCase().equals("HIDDEN-AUTOINSTANCE")) {
      //checkStack("endElement", "AUTOINSTANCE");
      // Cache the auto-instance into the autoins
      if (currentAutoinstanceParams != null)
        currentAutoinstances.add(currentAutoinstanceParams);
    // End AUTOINSTANCE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("PARAM")) {
    // End PARAM processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("NAME")) {
      checkStack("endElement", "NAME");
      resourceData.setName((String) contentStack.pop());
    // End NAME processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("JAR")) {
      checkStack("endElement", "JAR");

      // add jar file name
      String jarFileName = (String) contentStack.pop();
      if(resourceData != null) {
        resourceData.setJarFileName(jarFileName);
      }

      // add jar file URL if there is one
      if(sourceUrl != null) {
        String sourceUrlName = sourceUrl.toExternalForm();
        String separator = "/";

        if(sourceUrlName.endsWith(separator))
          separator = "";
        URL jarFileUrl = null;

        try {
          jarFileUrl = new URL(sourceUrlName + separator + jarFileName);
          if(resourceData != null) {
            resourceData.setJarFileUrl(jarFileUrl);
          }

          // We no longer need to add the jar URL to the class loader, as this
          // is done before the SAX parse
        } catch(MalformedURLException e) {
          throw new GateSaxException("bad URL " + jarFileUrl + e);
        }// End try
      }// End if
    // End JAR processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CLASS")) {
      checkStack("endElement", "CLASS");
      resourceData.setClassName((String) contentStack.pop());
    // End CLASS processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("COMMENT")) {
      checkStack("endElement", "COMMENT");
      resourceData.setComment((String) contentStack.pop());
    // End COMMENT processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("HELPURL")) {
      checkStack("endElement", "HELPURL");
      resourceData.setHelpURL((String) contentStack.pop());
    // End HELPURL processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("INTERFACE")) {
      checkStack("endElement", "INTERFACE");
      resourceData.setInterfaceName((String) contentStack.pop());
    // End INTERFACE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("ICON")) {
      checkStack("endElement", "ICON");
      resourceData.setIcon((String) contentStack.pop());
    // End ICON processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("OR")) {
      currentParamList.add(currentParamDisjunction);
      currentParamDisjunction = new ArrayList();
    // End OR processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("PARAMETER")) {
      checkStack("endElement", "PARAMETER");
      currentParam.typeName = (String) contentStack.pop();
      currentParamDisjunction.add(currentParam);
      if(DEBUG)
        Out.prln("added param: " + currentParam);
      currentParam = new Parameter(creoleFileUrl);
    // End PARAMETER processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("AUTOLOAD")) {
      resourceData.setAutoLoading(true);
    // End AUTOLOAD processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("PRIVATE")) {
      resourceData.setPrivate(true);
    // End PRIVATE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("TOOL")) {
      resourceData.setTool(true);
    // End TOOL processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("MAIN_VIEWER")) {
      resourceData.setIsMainView(true);
    // End MAIN_VIEWER processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("RESOURCE_DISPLAYED")){
      checkStack("endElement", "RESOURCE_DISPLAYED");
      String resourceDisplayed = (String) contentStack.pop();
      resourceData.setResourceDisplayed(resourceDisplayed);
      Class resourceDisplayedClass = null;
      try{
        resourceDisplayedClass = Gate.getClassLoader().
                                 loadClass(resourceDisplayed);
      } catch (ClassNotFoundException ex){
        throw new GateRuntimeException(
          "Couldn't get resource class from the resource name :" +
          resourceDisplayed + " " +ex );
      }// End try
    // End RESOURCE_DISPLAYED processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("ANNOTATION_TYPE_DISPLAYED")){
      checkStack("endElement", "ANNOTATION_TYPE_DISPLAYED");
      resourceData.setAnnotationTypeDisplayed((String) contentStack.pop());
    // End ANNOTATION_TYPE_DISPLAYED processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("GUI")) {

    // End GUI processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CREOLE")) {
    // End CREOLE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CREOLE-DIRECTORY")) {
    // End CREOLE-DIRECTORY processing
    //////////////////////////////////////////////////////////////////
    } else { // arbitrary elements get added as features of the resource data
      if(resourceData != null)
        resourceData.getFeatures().put(
          elementName.toUpperCase(),
          ((contentStack.isEmpty()) ? null : (String) contentStack.pop())
        );
    }
    //////////////////////////////////////////////////////////////////

  } // endElement

  /** Called when the SAX parser encounts text (PCDATA) in the XML doc */
  public void characters(char [] text,int start,int length) throws SAXException {
    if(!readCharacterStatus) {
      contentBuffer = new StringBuffer(new String(text,start,length));
    } else {
      contentBuffer.append(new String(text,start,length));
    }
    readCharacterStatus = true;
  }

  /**
   * This method is called when all characters between specific tags have been read completely
   */
  public void charactersAction(char[] text, int start, int length)
  throws SAXException {
    // Get the trimmed text between elements
    String content = new String(text, start, length).trim();
    // If the entire text is empty or is made from whitespaces then we simply
    // return
    if (content.length() == 0) return;
    contentStack.push(content);
    if(DEBUG) Out.println(content);
  } // characters

  /** Called when the SAX parser encounts white space */
  public void ignorableWhitespace(char ch[], int start, int length)
  throws SAXException {
  } // ignorableWhitespace

  /** Called for parse errors. */
  public void error(SAXParseException ex) throws SAXException {
    _seh.error(ex);
  } // error

  /** Called for fatal errors. */
  public void fatalError(SAXParseException ex) throws SAXException {
    _seh.fatalError(ex);
  } // fatalError

  /** Called for warnings. */
  public void warning(SAXParseException ex) throws SAXException {
    _seh.warning(ex);
  } // warning

} // CreoleXmlHandler
