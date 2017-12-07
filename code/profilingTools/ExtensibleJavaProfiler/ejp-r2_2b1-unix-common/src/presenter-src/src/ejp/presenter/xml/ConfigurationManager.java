/*
 $Id: ConfigurationManager.java,v 1.10 2005/02/14 12:06:21 vauclair Exp $

 Copyright (C) 2002-2005 Sebastien Vauclair

 This file is part of Extensible Java Profiler.

 Extensible Java Profiler is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Extensible Java Profiler is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Extensible Java Profiler; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package ejp.presenter.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import ejp.presenter.api.filters.parameters.AbstractParameter;
import ejp.presenter.api.util.CustomLogger;
import ejp.presenter.gui.RunProgramDialog;

/**
 * Manager for filter customizations and <i>Run program </i> dialog settings.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.10 $<br>$Date: 2005/02/14 12:06:21 $</code>
 */
public class ConfigurationManager
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS - XML NAMES
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * XML settings element.
   */
  public static final String SETTINGS_ELEMENT = "settings";

  /**
   * XML filter customization element.
   */
  public static final String FILTER_CUSTOMIZATION_ELEMENT = "filter-customization";

  /**
   * XML full name attribute for filter customization element.
   */
  public static final String FULL_NAME_ATTR = "full-name";

  /**
   * XML run program dialog element.
   */
  public static final String RUN_PROGRAM_DIALOG_ELEMENT = "run-program-dialog";

  /**
   * XML source paths attribute for run program dialog element.
   */
  public static final String SOURCE_PATHS_ATTR = "source-paths";

  /**
   * XML command template attribute for run program dialog element.
   */
  public static final String COMMAND_TEMPLATE_ATTR = "command-template";

  /**
   * XML default filters element.
   */
  public static final String DEFAULT_FILTERS_ELEMENT = "default-filters";

  /**
   * XML full names attribute for default filters element.
   */
  public static final String FULL_NAMES_ATTR = "full-names";

  /**
   * XML parameter element.
   */
  public static final String PARAMETER_ELEMENT = "parameter";

  /**
   * XML name attribute for parameter element.
   */
  public static final String NAME_ATTR = "name";

  /**
   * XML value attribute for parameter element.
   */
  public static final String VALUE_ATTR = "value";

  // ///////////////////////////////////////////////////////////////////////////
  // PUBLIC FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Possible (non-fatal) exception occured during initialization.
   * 
   * This is <code>null</code> iff no exception occured.
   */
  public final Exception exception;

  // ///////////////////////////////////////////////////////////////////////////
  // PROTECTED FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Single instance.
   * 
   * This is <code>null</code> until <code>createInstance()</code> has been
   * called.
   */
  protected static ConfigurationManager instance = null;

  /**
   * File to which XML data will be written to.
   */
  protected final File defaultFile;

  /**
   * Currently processed document.
   * 
   * After initialization, this is never <code>null</code> (either loaded file
   * or new document).
   */
  protected Document document = null;

  /**
   * Current root element (<code>"settings"</code> element), if any.
   * 
   * This field should be used only by the constructor and
   * <code>getSettingsElement()</code> method.
   */
  protected Element rootElement = null;

  /**
   * Current <code>"run-program-dialog"</code> element, if any.
   * 
   * This field should be used only by the constructor (through
   * <code>handleRunProgramDialog()</code> and
   * <code>getSettingsElement()</code> methods.
   */
  protected Element runProgramDialogElement = null;

  /**
   * Current <code>"default-filters"</code> element, if any.
   * 
   * This field should be used only by the constructor (through
   * {@link #handleDefaultFilters(Node)}and {@link #getDefaultFiltersElement()}
   * methods.
   */
  protected Element defaultFiltersElement = null;

  /**
   * Current <code>"filter-customization"</code> element for given filter
   * name, if any.
   * 
   * This field should be used only by the constructor (through
   * <code>handleFilterCustomization()</code> and
   * <code>getFilterCustomizationElement()</code> methods.
   * 
   * <p>
   * Mapping <code>String</code> (filter full name) -><code>Element</code>
   */
  protected Hashtable filterElements = new Hashtable();

  /**
   * Mapping <code>String</code> (filter full name + parameter name) ->
   * <code>String</code> (parameter value for this filter).
   */
  protected final Hashtable values = new Hashtable();

  // ///////////////////////////////////////////////////////////////////////////
  // CONSTRUCTOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new <code>ConfigurationManager</code> instance.
   * 
   * If configuration file does not exist, it is created.
   * 
   * @param aDefaultFile
   *          configuration file.
   * @exception ParserConfigurationException
   *              if a fatal error occurs.
   */
  protected ConfigurationManager(File aDefaultFile) throws ParserConfigurationException
  {
    defaultFile = aDefaultFile;

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(true);
    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);

    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setErrorHandler(new ErrorHandler()
    {
      public void warning(SAXParseException aException)
      {
        CustomLogger.INSTANCE.warning(aException.toString());
      }

      public void error(SAXParseException aException) throws SAXException
      {
        throw aException;
      }

      public void fatalError(SAXParseException aException) throws SAXException
      {
        throw aException;
      }
    });

    // try to parse file or build a new empty document
    try
    {
      document = builder.parse(aDefaultFile);
    }
    catch (Exception e) // SAXException, IOException
    {
      document = builder.newDocument(); // initialize document
      exception = e;
      return;
    }

    CustomLogger.INSTANCE.finest("Processing configuration file " + aDefaultFile);

    Exception resultException = null;
    rootElement = document.getDocumentElement();
    if (rootElement != null)
    {
      NodeList nodes = rootElement.getChildNodes();
      int nb = nodes.getLength();
      for (int i = 0; i < nb; i++)
      {
        Node node = nodes.item(i);
        String name = node.getNodeName();

        if (name.equalsIgnoreCase(RUN_PROGRAM_DIALOG_ELEMENT))
        {
          handleRunProgramDialog(node);
        }
        else if (name.equalsIgnoreCase(FILTER_CUSTOMIZATION_ELEMENT))
        {
          handleFilterCustomization(node);
        }
        else if (name.equalsIgnoreCase(DEFAULT_FILTERS_ELEMENT))
        {
          handleDefaultFilters(node);
        }
        else
          resultException = new IllegalStateException("Illegal node name \"" + name + "\"");
      }
    }
    exception = resultException;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // HANDLING OF SINGLE INSTANCE
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Initializes the single instance.
   * 
   * If default file does not exist, it is created.
   * 
   * @param aDefaultFile
   *          configuration file.
   * @exception IllegalStateException
   *              if single instance already exists.
   * @exception ParserConfigurationException
   *              if a fatal error occured.
   */
  public static void createInstance(File aDefaultFile) throws IllegalStateException,
      ParserConfigurationException
  {
    if (instance != null)
      throw new IllegalStateException("Single instance already exists");

    instance = new ConfigurationManager(aDefaultFile);
  }

  /**
   * Return single instance.
   * 
   * @return single instance, or <code>null</code> if it was not initialized.
   */
  public static ConfigurationManager getInstance()
  {
    return instance;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // PUBLIC ACCESSOR
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Gets a filter's parameter value.
   * 
   * @param aFilterName
   *          fully-qualified filter name.
   * @param aParameterName
   *          parameter name.
   * @return <code>null</code> iff the single instance is not initialized or
   *         no value is set for this combination of names.
   */
  public static String getParameterValue(String aFilterName, String aParameterName)
  {
    if (instance == null)
      return null;

    return (String) instance.values.get(tuple(aFilterName, aParameterName));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // PUBLIC MEMBERS TO SAVE DEFAULT VALUES
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Save default <i>run program dialog </i> values.
   * 
   * @param aSourcePaths
   *          source paths.
   * @param aCommandTemplate
   *          command template.
   */
  public void saveRunProgramDialogValues(String aSourcePaths, String aCommandTemplate)
  {
    Element rpd = getRunProgramDialogElement();
    NamedNodeMap attributes = rpd.getAttributes();

    Attr tempAttr = document.createAttribute(SOURCE_PATHS_ATTR);
    tempAttr.setValue(aSourcePaths);
    attributes.setNamedItem(tempAttr);

    tempAttr = document.createAttribute(COMMAND_TEMPLATE_ATTR);
    tempAttr.setValue(aCommandTemplate);
    attributes.setNamedItem(tempAttr);

    writeDocumentToDefaultFile();
  }

  /**
   * Saves default filter names.
   * 
   * <p>
   * TODO (later) unused for the moment - remove?
   * 
   * @param aFullNames
   *          full filter names, separated by <code>" "</code>.
   */
  public void saveDefaultFiltersValues(String aFullNames)
  {
    Element dfe = getDefaultFiltersElement();

    dfe.setAttribute(FULL_NAMES_ATTR, aFullNames);

    writeDocumentToDefaultFile();
  }

  /**
   * Save default values for a filter's parameters.
   * 
   * @param aFilterName
   *          fully-qualified filter name.
   * @param aParameters
   *          parameter instances.
   */
  public void saveParametersValues(String aFilterName, AbstractParameter[] aParameters)
  {
    Element fc = getFilterCustomizationElement(aFilterName);

    // remove all children (parameter elements)
    Node child;
    while ((child = fc.getFirstChild()) != null)
      fc.removeChild(child);

    for (int i = 0; i < aParameters.length; i++)
    {
      Element newChild = document.createElement(PARAMETER_ELEMENT);
      newChild.setAttribute(NAME_ATTR, aParameters[i].name);
      newChild.setAttribute(VALUE_ATTR, aParameters[i].getValueAsText());
      fc.appendChild(newChild);
    }

    writeDocumentToDefaultFile();
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Builds up an indentifier from two strings.
   * 
   * @param aString1
   *          first (heading) string.
   * @param aString2
   *          second (tailing) string.
   * @return a <code>String</code> value composed of both names.
   */
  protected static String tuple(String aString1, String aString2)
  {
    return aString1 + "$" + aString2;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // SPECIFIC HANDLERS FOR LOADING
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Handles a <i>run program dialog </i> node.
   * 
   * @param aNode
   *          the node.
   */
  protected void handleRunProgramDialog(Node aNode)
  {
    runProgramDialogElement = (Element) aNode;

    NamedNodeMap attributes = aNode.getAttributes();

    String sourcePaths = getAttributeValue(attributes, SOURCE_PATHS_ATTR);
    if (sourcePaths != null)
      RunProgramDialog.setDefaultSourcePaths(sourcePaths);

    String commandTemplate = getAttributeValue(attributes, COMMAND_TEMPLATE_ATTR);
    if (commandTemplate != null)
      RunProgramDialog.setDefaultCommandTemplate(commandTemplate);
  }

  /**
   * Handles a filter customization node.
   * 
   * @param aNode
   *          the node.
   */
  protected void handleFilterCustomization(Node aNode)
  {
    String fullName = aNode.getAttributes().getNamedItem("full-name").getNodeValue();

    // register node for this filter full name
    filterElements.put(fullName, aNode);

    NodeList params = aNode.getChildNodes();

    int nb = params.getLength();
    for (int i = 0; i < nb; i++)
    {
      NamedNodeMap attributes = params.item(i).getAttributes();
      String paramName = attributes.getNamedItem("name").getNodeValue();
      String value = attributes.getNamedItem("value").getNodeValue();

      values.put(tuple(fullName, paramName), value);
    }
  }

  /**
   * Handles a default filters node.
   * 
   * @param aNode
   *          the node.
   */
  protected void handleDefaultFilters(Node aNode)
  {
    defaultFiltersElement = (Element) aNode;

    String sFullNames = aNode.getAttributes().getNamedItem("full-names").getNodeValue();

    String[] fullNames = FiltersRepository.getWords(sFullNames, " ");

    FiltersRepository.getInstance().setDefaultFilters(fullNames);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // XML HELPERS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @return <code>null</code> iff the specified named attribute was not found
   */
  protected static String getAttributeValue(NamedNodeMap aAttributes, String aName)
  {
    Node node = aAttributes.getNamedItem(aName);
    if (node == null)
      return null;

    return node.getNodeValue();
  }

  // ///////////////////////////////////////////////////////////////////////////
  // QUICK ACCESS TO ELEMENTS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Builds or returns current <i>settings </i> element.
   * 
   * @return an <code>Element</code> value.
   */
  protected Element getSettingsElement()
  {
    if (rootElement == null)
    {
      rootElement = document.createElement(SETTINGS_ELEMENT);
      document.appendChild(rootElement);
    }

    return rootElement;
  }

  /**
   * Builds or returns current <i>run program dialog </i> element.
   * 
   * @return an <code>Element</code> value.
   */
  protected Element getRunProgramDialogElement()
  {
    if (runProgramDialogElement == null)
    {
      runProgramDialogElement = document.createElement(RUN_PROGRAM_DIALOG_ELEMENT);

      getSettingsElement().appendChild(runProgramDialogElement);
    }

    return runProgramDialogElement;
  }

  /**
   * Builds or returns current default filters element.
   * 
   * @return an <code>Element</code> value.
   */
  protected Element getDefaultFiltersElement()
  {
    if (defaultFiltersElement == null)
    {
      defaultFiltersElement = document.createElement(DEFAULT_FILTERS_ELEMENT);

      getSettingsElement().appendChild(defaultFiltersElement);
    }

    return defaultFiltersElement;
  }

  /**
   * Builds or returns <i>filter customization </i> element for given filter.
   * 
   * @param aFilterName
   *          fully-qualified filter name.
   * @return an <code>Element</code> value.
   */
  protected Element getFilterCustomizationElement(String aFilterName)
  {
    Element result = (Element) filterElements.get(aFilterName);
    if (result == null)
    {
      result = document.createElement(FILTER_CUSTOMIZATION_ELEMENT);
      result.setAttribute(FULL_NAME_ATTR, aFilterName);
      filterElements.put(aFilterName, result);

      getSettingsElement().appendChild(result);
    }

    return result;
  }

  // ///////////////////////////////////////////////////////////////////////////
  //
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Writes current document to default file.
   * 
   * @return <code>true</code> iff there was no error.
   */
  protected boolean writeDocumentToDefaultFile()
  {
    CustomLogger.INSTANCE.info("Writing settings to file " + defaultFile + "...");

    try
    {
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();

      DOMSource source = new DOMSource(document);

      Writer out = new FileWriter(defaultFile);
      SAXResult result = new SAXResult(new IndenterContentHandler(out));

      // do actual transformation
      transformer.transform(source, result);

      out.close();
    }
    catch (Exception e)
    {
      CustomLogger.INSTANCE.warning("Unable to save to XML file - " + e);
      return false;
    }

    return true;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // NESTED CLASS - INDENTER CONTENT HANDLER
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This class writes out a document to an indented XML file.
   */
  protected class IndenterContentHandler extends DefaultHandler
  {
    /**
     * Target output writer.
     */
    protected final Writer output;

    /**
     * Current indentation level.
     */
    protected int indent = 0;

    /**
     * If <code>true</code>, latest element was just opened.
     */
    protected boolean justOpened = false;

    /**
     * Creates a new <code>IndenterContentHandler</code> instance.
     * 
     * @param aOutput
     *          target writer output.
     * @exception IOException
     *              if an error occurs.
     */
    public IndenterContentHandler(Writer aOutput) throws IOException
    {
      output = aOutput;

      println("<!-- This file is automatically generated. -->");
      println("<!-- Any additions might be overwritten without notice. -->");
      skipln();

      println("<!DOCTYPE settings SYSTEM \"settings.dtd\">");
      skipln();
    }

    // /////////////////////////////////////////////////////////////////////////
    // I/O MEMBERS
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Writes out a given string to target file using current indentation.
     * 
     * @param aMsg
     *          a <code>String</code> value.
     * @exception IOException
     *              if an error occurs.
     */
    protected void println(String aMsg) throws IOException
    {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < indent; i++)
        buffer.append("  ");
      buffer.append(aMsg);
      buffer.append('\n');
      output.write(buffer.toString());
    }

    /**
     * Skips a line in target file.
     * 
     * @exception IOException
     *              if an error occurs.
     */
    protected void skipln() throws IOException
    {
      output.write('\n');
    }

    // /////////////////////////////////////////////////////////////////////////
    // OVERRIDINGS OF DEFAULT HANDLER
    // /////////////////////////////////////////////////////////////////////////

    public void startElement(String aNameSpaceURI, String aLocalName, String aQName,
        Attributes aAttrs) throws SAXException
    {
      try
      {
        if (justOpened)
        {
          // finish previous start
          println(">");
          skipln();
        }

        println("<" + aLocalName);
        ++indent;

        // print attributes
        int nb = aAttrs.getLength();
        for (int i = 0; i < nb; i++)
          println(aAttrs.getLocalName(i) + "=\"" + aAttrs.getValue(i) + "\"");

        justOpened = true;
      }
      catch (IOException ioe)
      {
        throw new SAXException(ioe);
      }
    }

    public void endElement(String aNameSpaceURI, String aLocalName, String aQName)
        throws SAXException
    {
      try
      {
        if (justOpened)
        {
          // finish previous start
          println("/>");
          skipln();
          --indent;
        }
        else
        {
          --indent;
          println("</" + aLocalName + ">");
          skipln();
        }

        justOpened = false;
      }
      catch (IOException ioe)
      {
        throw new SAXException(ioe);
      }
    }
  }
}
