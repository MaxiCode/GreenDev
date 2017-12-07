/*
 $Id: LoadedMethod.java,v 1.6 2005/02/14 12:06:24 vauclair Exp $

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

import java.io.StringReader;
import java.text.ParseException;
import java.util.StringTokenizer;

/**
 * Holder for information about a method of a Java class loaded during the
 * execution of the profiled program.
 * 
 * @author Sebastien Vauclair
 * @version <code>$Revision: 1.6 $<br>$Date: 2005/02/14 12:06:24 $</code>
 */
public class LoadedMethod
{
  // ///////////////////////////////////////////////////////////////////////////
  // CONSTANTS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Specifies whether names of classes in <code>java.lang</code> package
   * should be abbreviated.
   * 
   * If <code>true</code>, the types of parameters will be shortened if they
   * belong to such classes.
   */
  public static final boolean ABBREVIATE_JAVA_LANG = true;

  // ///////////////////////////////////////////////////////////////////////////
  // PUBLIC FIELDS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Description of class owning this method.
   */
  public final LoadedClass ownerClass;

  /**
   * The method's name.
   */
  public final String name;

  /**
   * Line number of the method's definition in the class' source file.
   * 
   * This might be <code>-1</code> if the line number is unknown.
   */
  public final int sourceLine;

  /**
   * Types of the method's parameters, encoded as a text value.
   */
  public final String parametersTypes;

  /**
   * Return type of the method, encoded as a text value.
   */
  public final String returnType;

  /**
   * Creates a new <code>LoadedMethod</code> instance.
   * 
   * @param aOwnerClass
   *          the description of the class owning this method.
   * @param aName
   *          the method's name.
   * @param aSignature
   *          the method's signature, encoded as a text value.
   * @param aSourceLine
   *          line of the method's definition in the class' source file (might
   *          be <code>-1</code> if unknown).
   */
  public LoadedMethod(LoadedClass aOwnerClass, String aName, String aSignature, int aSourceLine)
      throws NullPointerException
  {
    if (aOwnerClass == null)
      throw new NullPointerException("owner class is null");

    ownerClass = aOwnerClass;
    name = aName;
    sourceLine = aSourceLine;

    // parse signature

    StringTokenizer st = new StringTokenizer(aSignature, "()");

    String parameters = "";
    if (st.countTokens() > 1)
    {
      StringReader sr = new StringReader(st.nextToken());
      boolean first = true;
      String fd;
      while ((fd = parseFieldDescriptor(sr)) != null)
      {
        if (!first)
          parameters += ", ";
        parameters += fd;
        first = false;
      }
    }
    parametersTypes = parameters;

    returnType = parseFieldDescriptor(new StringReader(st.nextToken()));
  }

  /**
   * Parses a field descriptor into a Java type name.
   * 
   * @param aIn
   *          a <code>StringReader</code> to the field descriptor.
   * @return an explicit type name.
   */
  protected static String parseFieldDescriptor(StringReader aIn)
  {
    String brackets = "";

    try
    {
      String type = "";

      char c;
      while ((c = (char) aIn.read()) == '[')
        brackets += "[]";

      switch (c)
      {
      case 'Z':
        type = "boolean";
        break;

      case 'B':
        type = "byte";
        break;

      case 'C':
        type = "char";
        break;

      case 'S':
        type = "short";
        break;

      case 'I':
        type = "int";
        break;

      case 'J':
        type = "long";
        break;

      case 'F':
        type = "float";
        break;

      case 'D':
        type = "double";
        break;

      case 'V':
        type = "void";
        break;

      case 'L':
        while ((c = (char) aIn.read()) != ';')
          type += c;
        if (ABBREVIATE_JAVA_LANG && type.startsWith("java/lang/"))
          type = type.substring(10);
        type = type.replace('/', '.');

        break;

      case (char) -1:
        return null;

      default:
        throw new ParseException("invalid field type", 0);
      }
      return type + brackets;
    }
    catch (Exception e)
    {
      return "<unknown type>" + brackets;
    }
  }

  /**
   * Returns a textual description of the method.
   */
  public String toString()
  {
    return "method " + ownerClass.name + "." + name + "(" + parametersTypes + ") : " + returnType
        + " @ " + ownerClass.sourceFile + ":" + sourceLine;
  }

  /**
   * Returns a short caption that represents the method.
   * 
   * @return a <code>String</code> value.
   */
  public String toNodeLabel()
  {
    return ownerClass.name + "." + name + "(...)";
  }

  // ///////////////////////////////////////////////////////////////////////////
  // OBJECT OVERRIDINGS
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Returns a hash code of the method.
   * 
   * The hash code is built up from the owner class and the method's name and
   * signature.
   */
  public int hashCode()
  {
    // sourceline is intentionnaly not used
    return ownerClass.hashCode() * name.hashCode() * parametersTypes.hashCode()
        * returnType.hashCode();
  }

  /**
   * Compares this method description with another one for equality.
   * 
   * @param aLoadedMethod
   *          a <code>LoadedMethod</code> value.
   * @return a <code>boolean</code> value.
   */
  public boolean equals(LoadedMethod aLoadedMethod)
  {
    // sourceline is intentionnaly not used
    return ownerClass.equals(aLoadedMethod.ownerClass) && name.equals(aLoadedMethod.name)
        && parametersTypes.equals(aLoadedMethod.parametersTypes)
        && returnType.equals(aLoadedMethod.returnType); // note: not reqd by
    // Java
  }

  public boolean equals(Object aObject)
  {
    return aObject != null && aObject instanceof LoadedMethod && equals((LoadedMethod) aObject);
  }
}
