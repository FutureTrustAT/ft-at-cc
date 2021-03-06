@REM
@REM Copyright (C) 2014-2019 Philip Helger (www.helger.com)
@REM philip[at]helger[dot]com
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off

@rem copy first
copy ..\target\*-SNAPSHOT.jar .
copy ..\src\test\resources\example*.xml .

@rem copy Maven dependencies
set SRC=%~dp0
set SRC=%SRC%jars
mkdir %SRC%
cd ..
call mvn dependency:copy-dependencies -U "-DoutputDirectory=%SRC%" %*
