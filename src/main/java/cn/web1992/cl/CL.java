package cn.web1992.cl;

import org.springframework.boot.loader.LaunchedURLClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author web1992
 * @date 2021/2/18  3:43 下午
 */
public class CL extends ClassLoader {

    private static final int BUFFER_SIZE = 4096;

    private final ClassLoader parent;

    public CL(ClassLoader parent) {
        super(parent);
        this.parent = parent;
    }

    private native Class<?> makeClass(String name, byte[] bytes);

    static {
        System.loadLibrary("CL");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("EncJarClassLoader$loadClass name " + name);

//        if (parent instanceof LaunchedURLClassLoader) {
//            Class<?> aClass = loadClassInLaunchedClassLoader(name);
//            if (null != aClass) {
//                return aClass;
//            }
//        }

        return super.loadClass(name, resolve);
    }

    private Class<?> loadClassInLaunchedClassLoader(String name) throws ClassNotFoundException {
        LaunchedURLClassLoader cl = (LaunchedURLClassLoader) parent;

        String internalName = name.replace('.', '/') + ".class";
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
                System.out.println("bytes=" + bytes);
                String n = name.replace('.', '/');

                Class<?> definedClass = makeClass(n, bytes);
                return definedClass;
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            throw new ClassNotFoundException("Cannot load resource for class [" + name + "]", ex);
        }
    }
}
