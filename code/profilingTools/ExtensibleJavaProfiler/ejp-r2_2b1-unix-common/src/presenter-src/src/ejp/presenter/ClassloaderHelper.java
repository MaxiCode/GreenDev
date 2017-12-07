/*
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

package ejp.presenter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * TODOC
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.1 $<br>$Date: 2005/02/18 15:08:29 $</code>
 */
public abstract class ClassloaderHelper
{
  /**
   * Return the version of the JAR file containing the given class
   * 
   * @param c
   *          the class whose version is required.
   * @return String
   */
  public static String getClassVersion(Class c)
  {
    String classResource = c.getName().replace('.', '/') + ".class";
    return getResourceVersion(c.getClassLoader(), classResource);
  }

  /**
   * Find the directory or jar a give resource has been loaded from.
   * 
   * @param c
   *          the classloader to be consulted for the version
   * @param resource
   *          the resource whose location is required.
   * 
   * @return the pathname of the file with the resource source or null if we
   *         cannot determine the location.
   * 
   * @since Ant 1.6
   */
  public static String getResourceVersion(ClassLoader c, String resource)
  {
    URL url = c.getResource(resource);
    if (url != null)
    {
      String u = url.toString();
      if (u.startsWith("jar:file:"))
      {
        int pling = u.indexOf("!");
        String jarName = fromURI(u.substring(4, pling));
        try
        {
          JarFile file = new JarFile(jarName);
          Manifest manifest = file.getManifest();
          String version = manifest.getMainAttributes().getValue("Specification-Version");
          if (version == null)
          {
            version = "(unspecified)";
          }
          return version;
        }
        catch (IOException e)
        {
          return "unknown";
        }
      }
    }
    return "alpha";
  }

  /**
   * Constructs a file path from a <code>file:</code> URI.
   * 
   * <p>
   * Will be an absolute path if the given URI is absolute.
   * </p>
   * 
   * <p>
   * Swallows '%' that are not followed by two characters, doesn't deal with
   * non-ASCII characters.
   * </p>
   * 
   * @param uri
   *          the URI designating a file in the local filesystem.
   * @return the local file system path for the file.
   * @since Ant 1.6
   */
  public static String fromURI(String uri)
  {
    if (!uri.startsWith("file:"))
    {
      throw new IllegalArgumentException("Can only handle file: URIs");
    }
    if (uri.startsWith("file://"))
    {
      uri = uri.substring(7);
    }
    else
    {
      uri = uri.substring(5);
    }

    uri = uri.replace('/', File.separatorChar);
    if (File.pathSeparatorChar == ';' && uri.startsWith("\\") && uri.length() > 2
        && Character.isLetter(uri.charAt(1)) && uri.lastIndexOf(':') > -1)
    {
      uri = uri.substring(1);
    }

    StringBuffer sb = new StringBuffer();
    int i = 0;
    while (i < uri.length())
    {
      char c = uri.charAt(i++);
      if (c == '%')
      {
        if (i < uri.length())
        {
          char c1 = uri.charAt(i++);
          int i1 = Character.digit(c1, 16);
          if (i < uri.length())
          {
            char c2 = uri.charAt(i++);
            int i2 = Character.digit(c2, 16);
            sb.append((char) ((i1 << 4) + i2));
          }
        }
      }
      else
      {
        sb.append(c);
      }
    }

    return sb.toString();
  }
}
