/*
 $Id: DomUtilsTest.java,v 1.3 2005/02/14 12:12:22 vauclair Exp $

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

import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * TODOC
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.3 $<br>$Date: 2005/02/14 12:12:22 $</code>
 */
public class DomUtilsTest
{
  public static void main(String[] args) throws TransformerException, IOException
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder;
    try
    {
      builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e)
    {
      throw new RuntimeException("DOM configuration error", e);
    }
    Document doc = builder.newDocument();

    Element root = doc.createElement("ejp-export");
    doc.appendChild(root);

    Element child = doc.createElement("node");
    child.setAttribute("name", "label");
    child.setAttribute("time0", "213");

    root.appendChild(child);

    DomUtils.exportDocument(doc, null, null, new FileWriter("c:/test.xml"));
  }
}
