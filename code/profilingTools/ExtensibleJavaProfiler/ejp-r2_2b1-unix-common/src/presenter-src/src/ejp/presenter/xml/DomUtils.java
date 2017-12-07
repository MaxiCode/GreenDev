/*
 $Id: DomUtils.java,v 1.3 2005/02/14 12:06:21 vauclair Exp $

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

import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * @author vauclasn
 * 
 * Instances of this classe are used to ...
 */
public abstract class DomUtils
{

  public static void exportDocument(Document doc, String publicId, String systemId, Writer out)
      throws TransformerException
  {

    Transformer transformer;
    try
    {
      transformer = TransformerFactory.newInstance().newTransformer();
    }
    catch (TransformerConfigurationException e)
    {
      throw new RuntimeException("DOM configuration error", e);
    }
    catch (TransformerFactoryConfigurationError e)
    {
      throw new RuntimeException("DOM configuration error", e);
    }

    // set public and system ids
    if (publicId != null)
    {
      transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
    }
    if (systemId != null)
    {
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
    }

    transformer.setOutputProperty(OutputKeys.INDENT, "yes");

    // write DOM document to a file
    Source source = new DOMSource(doc);
    StreamResult result = new StreamResult(out);
    transformer.transform(source, result);
  }
}