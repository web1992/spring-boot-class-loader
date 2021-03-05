#!/bin/bash

mvn clean package

./encjar ./target/class-loader-0.0.1-SNAPSHOT.jar demo.jar
rm demo.jar
jar -cfm0 demo.jar tempDir/META-INF/MANIFEST.MF  -C  tempDir/  .

java -Dloader.classLoader=cn.web1992.cl.CL  -jar demo.jar