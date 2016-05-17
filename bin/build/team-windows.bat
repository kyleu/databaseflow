set SBT_OPTS=-Xmx2048m

bin\build\switch-team.bat

bin\activator.bat windows:packageBin
copy target\universal\databaseflow.msi build\databaseflow.msi
