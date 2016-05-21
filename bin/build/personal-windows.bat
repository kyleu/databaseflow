set SBT_OPTS=-Xmx2048m

bin\build\switch-personal.bat

bin\activator.bat windows:packageBin
copy target\universal\databaseflow.msi build\databaseflow-personal.msi

bin\build\switch-team.bat
