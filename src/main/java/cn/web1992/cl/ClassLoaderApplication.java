package cn.web1992.cl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * java -Dloader.classLoader=cn.web1992.cl.CL  -jar target/class-loader-0.0.1-SNAPSHOT.jar
 */
@SpringBootApplication
public class ClassLoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassLoaderApplication.class, args);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        System.out.println(cl);
    }

}
