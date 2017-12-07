/*
 $Id: LoadedClass.java,v 1.5 2005/02/14 12:06:24 vauclair Exp $

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

package ejp.presenter.api.model;

import java.util.Vector;

/**
 * Holder for information about a Java class loaded during the execution of the
 * profiled program.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.5 $<br>$Date: 2005/02/14 12:06:24 $</code>
 */
public class LoadedClass
{
  /**
   * Fully-qualified name of the class.
   */
  public final String name;

  /**
   * Source file defining the class.
   */
  public final String sourceFile;

  /**
   * List the of methods the class has.
   * 
   * Each element is an instance of the <code>LoadedMethod</code> class.
   */
  public final Vector methods = new Vector();

  /**
   * Creates a new <code>LoadedClass</code> instance.
   * 
   * @param aName
   *          the class' fully-qualified name.
   * @param aSourceFile
   *          source file definning the class.
   */
  public LoadedClass(String aName, String aSourceFile)
  {
    name = aName;
    sourceFile = aSourceFile;
  }

  /**
   * Convenience method to add a method description to this class.
   * 
   * Calls the full constructor of <code>LoadedMethod</code> to build an new
   * instance, then adds to <code>methods</code> and returns it.
   * 
   * @param aName
   *          method's name.
   * @param aSignature
   *          method's signature (parameters + return types).
   * @param aSourceLine
   *          line in source file definning the method.
   * @return the new <code>LoadedMethod</code> value.
   */
  public LoadedMethod addMethod(String aName, String aSignature, int aSourceLine)
  {
    LoadedMethod result = new LoadedMethod(this, aName, aSignature, aSourceLine);
    methods.add(result);
    return result;
  }

  /**
   * Returns a textual description of the class.
   * 
   * The result is built up from the class' name and its source file.
   */
  public String toString()
  {
    return "class " + name + " (" + sourceFile + ")";
  }

  /**
   * Returns a hash code of the class.
   * 
   * The hash code is built up from the class name.
   */
  public int hashCode()
  {
    return name.hashCode();
  }

  /**
   * Compares this class description with another one for equality.
   * 
   * @param aLoadedClass
   *          a <code>LoadedClass</code> value.
   * @return a <code>boolean</code> value.
   */
  public boolean equals(LoadedClass aLoadedClass)
  {
    return name.equals(aLoadedClass.name);
  }

  public boolean equals(Object aObject)
  {
    return aObject != null && aObject instanceof LoadedClass && equals((LoadedClass) aObject);
  }

  /**
   * Extracts package name from class name.
   * 
   * @return the part before the last dot (<code>"."</code>), or
   *         <code>""</code> if the class has no package.
   */
  public String getPackage()
  {
    int pos = name.lastIndexOf(".");
    return (pos >= 0 ? name.substring(0, pos) : "");
  }
}
