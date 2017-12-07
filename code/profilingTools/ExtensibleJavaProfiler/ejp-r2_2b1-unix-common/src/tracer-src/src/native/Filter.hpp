/*
  $Id: Filter.hpp,v 1.2 2005/02/22 13:08:55 vauclair Exp $

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

#ifndef __Filter_h__
#define __Filter_h__

#include "Globals.hpp"

#include <list>

class Filter
{
public:
  void parseRules(JNIEnv* envId_, char const * filename_);
  bool isFilteredOut(JNIEnv* envId_, String const & className_) const;

private:
  typedef std::pair<String, bool> Rule;
  typedef std::list<Rule, STL_ALLOCATOR(Rule) > RuleList;

  RuleList m_rules;
};

#endif // __Filter_h__
