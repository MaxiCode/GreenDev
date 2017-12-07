;; Copyright (C) 2002, 2003 Sebastien Vauclair
;;
;; This file is part of Extensible Java Profiler.
;;
;; Extensible Java Profiler is free software; you can redistribute it and/or
;; modify it under the terms of the GNU General Public License as published by
;; the Free Software Foundation; either version 2 of the License, or
;; (at your option) any later version.
;;
;; Extensible Java Profiler is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Extensible Java Profiler; if not, write to the Free Software
;; Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

;; JDE project file for EJP Tracer.
;;
;; Author: Sebastien Vauclair
;; Version: $Revision: 1.2 $ $Date: 2003/07/17 13:19:28 $

(jde-project-file-version "1.0")
(jde-set-variables
 '(jde-javadoc-gen-destination-directory "./javadoc")
 '(jde-package-default-package-comment "")
 '(jde-global-classpath (quote ("./src/java" "./classes")))
 '(jde-compile-option-classpath (quote ("./src/java")))
 '(jde-compile-option-directory "./classes")
 '(jde-db-source-directories (quote ("./src/java" "c:/prog/jdk/src")))
 '(jde-package-package-comment "")
 '(jde-compile-option-debug (quote ("all" (t nil nil)))))
