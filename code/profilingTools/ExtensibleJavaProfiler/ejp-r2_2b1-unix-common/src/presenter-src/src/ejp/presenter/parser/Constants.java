/*
 $Id: Constants.java,v 1.4 2005/02/22 12:50:09 vauclair Exp $
 
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

package ejp.presenter.parser;

public abstract class Constants
{
  public static final short ID_VERSION = 'v';

  public static final short ID_CLASS_LOAD = 'C';

  public static final short ID_CLASS_UNLOAD = 'c';

  public static final short ID_METHOD_ENTRY = 'M';

  public static final short ID_METHOD_EXIT = 'm';

  public static final short ID_ENDIAN = 'e';

  public static final String GZIP_FILE_EXTENSION = "gz";

  public static final int ITEM_SIZE = 1 + 4 + 8 + 4;
}
