package com.mohistmc.banner.bukkit.nms.proxy;

import com.mohistmc.banner.api.DynamicEnumHelper;
import com.mohistmc.banner.bukkit.nms.model.ClassMapping;
import com.mohistmc.banner.bukkit.nms.utils.RemapUtils;
import net.md_5.specialsource.repo.RuntimeRepo;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 *
 * @author pyz
 * @date 2019/7/1 8:41 PM
 */
public class DelegateURLClassLoder extends URLClassLoader {

    public static final String desc = DelegateURLClassLoder.class.getName().replace('.', '/');
    private final Map<String, Class<?>> classeCache = new HashMap<>();
    private final Set<Package> packageCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public DelegateURLClassLoder(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
    }

    public DelegateURLClassLoder(final URL[] urls) {
        super(urls);
    }

    public DelegateURLClassLoder(final URL[] urls, final ClassLoader parent, final URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        if (RemapUtils.needRemap(name)) {
            ClassMapping remappedClassMapping = RemapUtils.jarMapping.byNMSName.get(name);
            if(remappedClassMapping == null){
                throw new ClassNotFoundException(name.replace('/','.'));
            }
            String remappedClass = remappedClassMapping.getMcpName();
            return Class.forName(remappedClass);
        }
        Class<?> result = this.classeCache.get(name);
        if (result != null) {
            return result;
        }
        synchronized (name.intern()) {
            result = this.remappedFindClass(name);
            if (result != null) {
                return result;
            }
            result = this.remappedFindClass(name);
            if (result == null) {
                try {
                    result = super.findClass(name);
                } catch (ClassNotFoundException e) {
                    result = Class.forName(name);
                }
            }
            if (result == null) {
                throw new ClassNotFoundException(name);
            }
            this.cacheClass(name, result);
        }
        return result;
    }

    protected Class<?> remappedFindClass(String name) throws ClassNotFoundException {
        Class<?> result = null;
        try {
            final String path = name.replace('.', '/').concat(".class");
            final URL url = this.findResource(path);
            if (url != null) {
                final InputStream stream = url.openStream();
                if (stream != null) {
                    byte[] bytecode = RemapUtils.jarRemapper.remapClassFile(stream, RuntimeRepo.getInstance());
                    bytecode = RemapUtils.remapFindClass(bytecode);
                    final JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    final URL jarURL = jarURLConnection.getJarFileURL();

                    final Manifest manifest = jarURLConnection.getManifest();
                    fixPackage(manifest, url, name);

                    final CodeSource codeSource = new CodeSource(jarURL, new CodeSigner[0]);
                    result = this.defineClass(name, bytecode, 0, bytecode.length, codeSource);
                    if (result != null) {
                        this.resolveClass(result);
                    }
                }
            }
        } catch (Throwable t) {
            throw new ClassNotFoundException("Failed to remap class " + name, t);
        }
        return result;
    }

    protected void cacheClass(final String name, final Class<?> clazz) {
        this.classeCache.put(name, clazz);
        if (ConfigurationSerializable.class.isAssignableFrom(clazz)) {
            ConfigurationSerialization.registerClass((Class<? extends ConfigurationSerializable>) clazz);
        }
    }

    private void fixPackage(Manifest manifest, URL url, String name) {
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            String pkgName = name.substring(0, dot);
            Package pkg = getPackage(pkgName);
            if (pkg == null) {
                try {
                    if (manifest != null) {
                        pkg = definePackage(pkgName, manifest, url);
                    } else {
                        pkg = definePackage(pkgName, null, null, null, null, null, null, null);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (pkg != null && manifest != null) {
                if (!packageCache.contains(pkg)) {
                    Attributes attributes = manifest.getMainAttributes();
                    if (attributes != null) {
                        try {
                            try {
                                Object versionInfo = DynamicEnumHelper.getField(pkg, Package.class.getDeclaredField("versionInfo"));
                                if (versionInfo != null) {
                                    Class<?> Package$VersionInfo = Class.forName("java.lang.Package$VersionInfo");
                                    DynamicEnumHelper.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE), Package$VersionInfo.getDeclaredField("implTitle"));
                                    DynamicEnumHelper.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION), Package$VersionInfo.getDeclaredField("implVersion"));
                                    DynamicEnumHelper.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR), Package$VersionInfo.getDeclaredField("implVendor"));
                                    DynamicEnumHelper.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_TITLE), Package$VersionInfo.getDeclaredField("specTitle"));
                                    DynamicEnumHelper.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_VERSION), Package$VersionInfo.getDeclaredField("specVersion"));
                                    DynamicEnumHelper.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_VENDOR), Package$VersionInfo.getDeclaredField("specVendor"));
                                }
                            } catch (Exception ignored) {
                            }
                        } finally {
                            packageCache.add(pkg);
                        }
                    }
                }
            }
        }
    }
}