@echo off

rem ###############################
rem # Copyright (C) 2012 Jon Schang
rem # 
rem # This file is part of jSchangLib, released under the LGPLv3
rem # 
rem # jSchangLib is free software: you can redistribute it and/or modify
rem # it under the terms of the GNU Lesser General Public License as published by
rem # the Free Software Foundation, either version 3 of the License, or
rem # (at your option) any later version.
rem # 
rem # jSchangLib is distributed in the hope that it will be useful,
rem # but WITHOUT ANY WARRANTY; without even the implied warranty of
rem # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem # GNU Lesser General Public License for more details.
rem # 
rem # You should have received a copy of the GNU Lesser General Public License
rem # along with jSchangLib.  If not, see <http://www.gnu.org/licenses/>.
rem ###############################

cmd /C mvn install:install-file -Dfile=./amazon-ec2-2009-08-15-java-library.jar -DgroupId=com.amazonaws.ec2 -DartifactId=ec2-client -Dversion=1.0.CUSTOM -Dpackaging=jar

cmd /C mvn install:install-file -Dfile=./jogl/gluegen-rt.jar -DgroupId=com.sun.opengl -DartifactId=gluegen-rt -Dversion=1.0.CUSTOM -Dpackaging=jar

rem cmd /C mvn install:install-file -Dfile=./jogl/jogl-natives-linux-i586.jar -DgroupId=com.sun.opengl -DartifactId=jogl-natives-linux-i586 -Dversion=1.0.CUSTOM -Dpackaging=jar
rem cmd /C mvn install:install-file -Dfile=./jogl/jogl-natives-linux-amd64.jar -DgroupId=com.sun.opengl -DartifactId=jogl-natives-linux-amd64 -Dversion=1.0.CUSTOM -Dpackaging=jar
rem cmd /C mvn install:install-file -Dfile=./jogl/jogl-natives-windows-i586.jar -DgroupId=com.sun.opengl -DartifactId=jogl-natives-windows-i586 -Dversion=1.0.CUSTOM -Dpackaging=jar
cmd /C mvn install:install-file -Dfile=./jogl/jogl.jar -DgroupId=com.sun.opengl -DartifactId=jogl -Dversion=1.0.CUSTOM -Dpackaging=jar

cmd /C mvn install:install-file -Dfile=./jocl/JOCL-0.1.4-beta1.jar -DgroupId=org.jocl -DartifactId=JOCL -Dversion=0.1.4-beta1 -Dpackaging=jar

