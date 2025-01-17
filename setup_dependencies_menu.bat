@echo off

echo Select CL to Link
echo  1: CLCastles
echo  2: CLVerifone
echo  3: CLNexgo
echo  4: CLIngenico
echo  5: CLSunmi
echo  6: CLMagicCube
echo.

set "input="
set /p input=Type number choice then press ENTER:
IF NOT DEFINED input GOTO :EOF

if "%input%" == "1" set cl=CLCastles
if "%input%" == "2"	set cl=CLVerifone
if "%input%" == "3"	set cl=CLNexgo
if "%input%" == "4"	set cl=CLIngenico
if "%input%" == "5"	set cl=CLSunmi
if "%input%" == "6"	set cl=CLMagicCube

IF NOT DEFINED cl GOTO FILE_NOT_FOUND
IF NOT EXIST ..\%cl% GOTO FILE_NOT_FOUND

echo.

call setup_dependencies.bat %cl%
GOTO END

:FILE_NOT_FOUND
echo CL does not exist

:END
pause