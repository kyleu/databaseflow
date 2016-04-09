set SBT_OPTS=-Xmx2048m

bin\switch-solo.bat

bin\activator.bat windows:packageBin
copy target\universal\databaseflow.msi build\databaseflow.msi

bin\switch-team.bat
