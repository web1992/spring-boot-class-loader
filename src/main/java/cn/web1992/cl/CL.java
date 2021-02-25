package cn.web1992.cl;

import org.springframework.boot.loader.LaunchedURLClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @author web1992
 * @date 2021/2/18  3:43 下午
 */
public class CL extends URLClassLoader {

    private static final int BUFFER_SIZE = 4096;

    private Map<String, Class> classMap = new HashMap<>();
    private Map<String, byte[]> bytesMap = new HashMap<>();
    private final HashMap<String, Package> packages = new HashMap<>();

    private final ClassLoader parent;

    public CL(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
    }

    private native Class<?> makeClass(String name, byte[] bytes);

    public final static native byte[] encByte(byte[] bytes);

    static {
        System.loadLibrary("CL");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        if (parent instanceof LaunchedURLClassLoader) {
            Class<?> aClass = null;
            try {
                aClass = loadClassInLaunchedClassLoader(name);
            } catch (Exception e) {
                // ignore
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
        if (name.startsWith("sun")) {
            return null;
        }

        Class aClass = classMap.get(name);
        if (null != aClass) {
            return aClass;
        }
        LaunchedURLClassLoader cl = (LaunchedURLClassLoader) parent;

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
                    bytesMap.put(name, bytes);
                    definePackageIfNecessary(name);
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

    /**
     * Define a package before a {@code findClass} call is made. This is necessary to
     * ensure that the appropriate manifest for nested JARs is associated with the
     * package.
     *
     * @param className the class name being found
     */
    private void definePackageIfNecessary(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot >= 0) {
            String packageName = className.substring(0, lastDot);
            if (getPackage(packageName) == null) {
                try {
                    definePackage(className, packageName);
                } catch (IllegalArgumentException ex) {
                    // Tolerate race condition due to being parallel capable
                    if (getPackage(packageName) == null) {
                        // This should never happen as the IllegalArgumentException
                        // indicates that the package has already been defined and,
                        // therefore, getPackage(name) should not have returned null.
                        throw new AssertionError(
                                "Package " + packageName + " has already been defined but it could not be found");
                    }
                }
            }
        }
    }

    private void definePackage(String className, String packageName) {

        String packageEntryName = packageName.replace('.', '/') + "/";
        String classEntryName = className.replace('.', '/') + ".class";
        for (URL url : getURLs()) {
            try {
                URLConnection connection = url.openConnection();
                if (connection instanceof JarURLConnection) {
                    JarFile jarFile = ((JarURLConnection) connection).getJarFile();
                    if (jarFile.getEntry(classEntryName) != null && jarFile.getEntry(packageEntryName) != null
                            && jarFile.getManifest() != null) {
                        Package aPackage = super.definePackage(packageName, jarFile.getManifest(), url);
                        if (null != aPackage) {
                            packages.put(packageName, aPackage);
                        }
                    }
                }
            } catch (IOException ex) {
                // Ignore
                ex.printStackTrace();
            }
        }

    }

    @Override
    protected Package getPackage(String name) {
        return packages.get(name);
    }

    @Override
    public URL[] getURLs() {
        LaunchedURLClassLoader cl = (LaunchedURLClassLoader) parent;
        URL[] urLs = cl.getURLs();
        return urLs;
    }
}
