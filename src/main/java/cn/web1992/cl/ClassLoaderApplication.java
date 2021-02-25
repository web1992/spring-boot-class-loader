package cn.web1992.cl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * mvn clean package && java -Dloader.classLoader=cn.web1992.cl.CL  -jar target/class-loader-0.0.1-SNAPSHOT.jar
 * <p>
 * java -Dloader.classLoader=cn.web1992.cl.CL  -jar target/class-loader-0.0.1-SNAPSHOT.jar
 * <p>
 * unzip  -oq class-loader-0.0.1-SNAPSHOT.jar -d tempDir
 * <p>
 * jar -cfm0 demo.jar tempDir/META-INF/MANIFEST.MF  -C  tempDir/  .
 * <p>
 * ./encjar ./target/class-loader-0.0.1-SNAPSHOT.jar demo.jar
 */
@SpringBootApplication
public class ClassLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassLoaderApplication.class, args);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        System.out.println(cl);
    }

}
