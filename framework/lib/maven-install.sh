#!/bin/sh

###############################
# Copyright (C) 2012 Jon Schang
# 
# This file is part of jSchangLib, released under the LGPLv3
# 
# jSchangLib is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# jSchangLib is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public License
# along with jSchangLib.  If not, see <http://www.gnu.org/licenses/>.
###############################

mvn install:install-file -Dfile=./jms-1.1/jms.jar -DgroupId=javax.jms \
    -DartifactId=jms -Dversion=1.1 -Dpackaging=jar

mvn install:install-file -Dfile=./jmx-1.2.1/jmxri.jar -DgroupId=com.sun.jmx \
    -DartifactId=jmxri -Dversion=1.2.1 -Dpackaging=jar

mvn install:install-file -Dfile=./jmx-1.2.1/jmxtools.jar -DgroupId=com.sun.jdmk \
    -DartifactId=jmxtools -Dversion=1.2.1 -Dpackaging=jar
