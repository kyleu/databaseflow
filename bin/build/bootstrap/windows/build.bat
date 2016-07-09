cd C:\Users\kyle\Desktop\build
REM rmdir /S /Q databaseflow
xcopy /E /I /R /Y /H /Q Y:\Projects\Personal\databaseflow databaseflow /EXCLUDE:C:\Users\kyle\Desktop\build\exclude.txt
cd databaseflow
mkdir build\windows
REM bin/activator/activator.bat "compile"
cd bin/build
windows.bat
cd C:\Users\kyle\Desktop\build

copy databaseflow\build\windows\*.* Y:\Projects\Personal\databaseflow\build\windows
