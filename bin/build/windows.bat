set SBT_OPTS=-Xmx2048m

cd ..\..

call bin\build\switch-gui.bat

call bin\activator\activator.bat windows:packageBin
move target\windows\databaseflow.msi build\windows\databaseflow-gui.msi

call bin\build\switch-service.bat

call bin\activator\activator.bat windows:packageBin
move target\windows\databaseflow.msi build\windows\databaseflow-team.msi

cd bin\build