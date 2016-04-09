set SBT_OPTS=-Xmx2048m

bin\switch-team.bat

bin\activator.bat windows:packageBin
copy target\universal\databaseflow.msi build\databaseflow.msi
