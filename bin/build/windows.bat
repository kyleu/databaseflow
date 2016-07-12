set SBT_OPTS=-Xmx2048m

cd ..\..

call bin\build\switch-service.bat

call bin\activator\activator.bat windows:packageBin
move target\windows\databaseflow.msi build\windows\databaseflow.msi

call bin\activator\activator.bat universal:packageBin
move target\universal\databaseflow-1.0.0.zip build\windows\databaseflow.zip

cd bin\build
