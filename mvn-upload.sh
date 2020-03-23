#!/bin/sh
mvn deploy:deploy-file -Dfile=target/beicon.jar -DpomFile=pom.xml -DrepositoryId=clojars -Durl=https://clojars.org/repo/
