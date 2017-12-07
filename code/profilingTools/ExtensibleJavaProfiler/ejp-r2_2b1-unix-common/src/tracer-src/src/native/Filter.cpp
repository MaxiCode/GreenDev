/*
  $Id: Filter.cpp,v 1.8 2005/02/22 13:08:55 vauclair Exp $

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

#include "Filter.hpp"

#include <algorithm>
#include <iostream>

#include "EjpException.hpp"
#include "InputFile.hpp"

#define COMMENT_TAG '#'
#define INCLUDE_TAG 'i'
#define EXCLUDE_TAG 'e'

bool Filter::isFilteredOut(JNIEnv* envId_, String const & className_) const
{
  TRACE_ENTER(envId_, "Filter::isFilteredOut");

  LOG_DEBUG(envId_, "checking: " << className_);

  bool result = false;

  RuleList::const_iterator it = m_rules.begin();
  while (it != m_rules.end())
  {
    Rule const & rule = *it;
    String const & prefix = rule.first;
    LOG_DEBUG(envId_, "  rule: " << prefix << " - " << rule.second);

    if (Globals::startsWith(envId_, className_.c_str(), prefix.c_str()))
    {
      LOG_DEBUG(envId_, "  match!");
      result = rule.second;
      break;
    }

    ++it;
  }

  LOG_DEBUG(envId_, "  result=" << result);

  TRACE_EXIT(envId_, "Filter::isFilteredOut");

  return result;
}

void Filter::parseRules(JNIEnv* envId_, char const * filename_)
{
  TRACE_ENTER(envId_, "Filter::parseRule");

  LOG_INFO(envId_, "Reading filter definition from file " << filename_);

  InputFile file(filename_);

  int lineNb = 0;
  bool done = false;
  while (!file.isEof())
  {
    ++lineNb;
    String line = file.readLine();

    // remove all whitespaces (incl. tabs)
    Globals::remove(line, ' ');
    Globals::remove(line, '\t');

    if (line.size() == 0)
    {
      continue;
    }

    char ch = line[0];
    switch (ch)
    {
    case COMMENT_TAG:
      // nop
      break;

    case INCLUDE_TAG:
    case EXCLUDE_TAG:
    {
      String prefix = line.substr(1);
      bool exclude = (ch == EXCLUDE_TAG);
      bool all = (prefix.size() == 0);

      String ruleText = String(exclude ? "EXCLUDE" : "INCLUDE") + " "
        + (all ? "ALL" : prefix);

      if (done)
      {
        LOG_WARNING(envId_, "Ignoring additional rule " << ruleText);
      }
      else
      {
        Rule rule(prefix, exclude);
        m_rules.push_back(rule);

        LOG_INFO(envId_, "Rule #" << m_rules.size() << ": " << ruleText);

        if (all)
        {
          done = true;
        }
      }

      break;
    }

    default:
      OStringStream oss;
      oss << "Invalid rule (starts with '" << ch << "') at "
        << filename_ << ":" << lineNb;
      throw EjpException(__FILE__, __LINE__, envId_, oss.str());
    }
  }

  TRACE_EXIT(envId_, "Filter::parseRule");
}
