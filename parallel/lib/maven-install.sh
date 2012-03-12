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

mvn install:install-file -Dfile=./amazon-ec2-2009-08-15-java-library.jar \
  -DgroupId=com.amazonaws.ec2 -DartifactId=ec2-client -Dversion=1.0.CUSTOM -Dpackaging=jar
mvn install:install-file -Dfile=./jogl/gluegen-rt.jar \
  -DgroupId=com.sun.opengl -DartifactId=gluegen-rt -Dversion=1.0.CUSTOM -Dpackaging=jar
mvn install:install-file -Dfile=./jogl/jogl.jar \
  -DgroupId=com.sun.opengl -DartifactId=jogl -Dversion=1.0.CUSTOM -Dpackaging=jar
mvn install:install-file -Dfile=./jocl/JOCL-0.1.4-beta1.jar -DgroupId=org.jocl \
	-DartifactId=JOCL -Dversion=0.1.4-beta1 -Dpackaging=jar
