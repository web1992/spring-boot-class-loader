package cn.web1992.cl;

import org.springframework.boot.loader.LaunchedURLClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * @author web1992
 * @date 2021/2/18  3:43 下午
 */
public class CL2 extends URLClassLoader {

    private static final int BUFFER_SIZE = 4096;

    private Map<String, Class> classMap = new HashMap<>();
    private Map<String, Package> packageNameMap = new HashMap<>();

    private final ClassLoader parent;

    private final URL[] urls;

    public CL2(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
        this.parent = parent;
    }

    private native Class<?> makeClass(String name, byte[] bytes);

    static {
        System.loadLibrary("CL");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        System.out.println("EncJarClassLoader$loadClass name " + name);

        Class<?> aClass1 = super.findClass(name);
//        if (null != aClass1) {
//            return aClass1;
//        }
        if (parent instanceof LaunchedURLClassLoader) {
            Class<?> aClass = null;
            try {
                aClass = loadClassInLaunchedClassLoader(name);

            } catch (Exception e) {

            }
            if (null != aClass) {
                return aClass;
            }
        }

        return super.loadClass(name, resolve);
    }

    private Class<?> loadClassInLaunchedClassLoader(String name) throws ClassNotFoundException {

        if (name.startsWith("java")) {
            return null;
        }

        Class aClass = classMap.get(name);
        if (null != aClass) {
            return aClass;
        }
        LaunchedURLClassLoader cl = (LaunchedURLClassLoader) parent;

        URL resource = cl.getResource(name);
        String internalName = name.replace('.', '/').concat(".class");
        InputStream inputStream = cl.getResourceAsStream(internalName);
        if (inputStream == null) {
            throw new ClassNotFoundException(name);
        }
        try {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                byte[] bytes = outputStream.toByteArray();
                String n = name.replace('.', '/');

                Class<?> definedClass = makeClass(n, bytes);
                if (definedClass != null) {
                    classMap.put(name, definedClass);
                }
                return definedClass;
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
        }
    }

    @Override
    protected Package definePackage(String name, Manifest man, URL url) throws IllegalArgumentException {
        return super.definePackage(name, man, url);
    }
}
