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

cmd /C "cd framework\lib && maven-install.bat && cd ..\.."

cmd /C "cd machine-learning\lib && maven-install.bat && cd ..\.."
 
cmd /C "cd parallel\lib && maven-install.bat && cd ..\.."