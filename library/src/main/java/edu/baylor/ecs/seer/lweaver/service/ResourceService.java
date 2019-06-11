package edu.baylor.ecs.seer.lweaver.service;

import edu.baylor.ecs.seer.common.context.SeerContext;
import edu.baylor.ecs.seer.common.context.SeerMsContext;
import edu.baylor.ecs.seer.common.context.SeerRequestContext;

import javassist.ClassPool;
import javassist.CtClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javassist.bytecode.*;

@Service
public class ResourceService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private Environment env;

    /**
     * Paths to all compiled jar packages
     * @param folderPath
     * @return
     */
    public List<String> getResourcePaths(String folderPath){
        String directory = new File(folderPath).getAbsolutePath();
        Path start = Paths.get(directory);
        int maxDepth = 15;
        List<String> fileNames = new ArrayList<>();
        try {
            Stream<Path> stream = Files.find(start, maxDepth,
                    (path, attr) ->
                            String.valueOf(path).toLowerCase().endsWith(".jar") ||
                                    String.valueOf(path).toLowerCase().endsWith(".war"));
            fileNames = stream
                    .sorted()
                    .map(String::valueOf)
                    .filter((path) -> {
                        return (String.valueOf(path).toLowerCase().endsWith(".jar") ||
                                String.valueOf(path).toLowerCase().endsWith(".war")) &&
                                !String.valueOf(path).toLowerCase().contains("/.mvn/") &&
                                !String.valueOf(path).toLowerCase().startsWith("/usr/lib/jvm/") &&
                                !String.valueOf(path).toLowerCase().contains("/target/dependency/") &&
                                !String.valueOf(path).toLowerCase().contains("/gradle") &&
                                !String.valueOf(path).toLowerCase().contains("\\.mvn\\") &&
                                !String.valueOf(path).toLowerCase().contains("\\target\\dependency") &&
                                !String.valueOf(path).toLowerCase().contains("\\gradle");
                    })
                    .collect(Collectors.toList());
        } catch(Exception e){
            e.printStackTrace();
        }
        return fileNames;
    }

    /**
     * Ct classes of particular file
     * @param file
     * @return
     */
    public List<CtClass> getCtClasses(String file, String organizationPath){
        ClassPool cp = ClassPool.getDefault();
        List<CtClass> ctClasses = new ArrayList<>();
        // 1. Get resource
        Resource resource = getResource(file);
        // 2. Get class files
        Set<ClassFile> classFiles = getClassFileSet(resource, organizationPath);

        // Class file to ct class
        for (ClassFile classFile : classFiles) {

            CtClass clazz = null;
            try {
                clazz = cp.makeClass(classFile);
                ctClasses.add(clazz);
            } catch (Exception e) {
                /* LOG */
                System.out.println("Failed to make class:" + e.toString());
                break;
            }
        }
        //return ct classes
        return ctClasses;
    }

    /**
     * 1. Get Resource
     * @param file
     * @return
     */
    private Resource getResource(String file){
        boolean isWindows = env.getProperty("platform.isWindows").equals("true");
        if(isWindows) {
            return resourceLoader.getResource("file:/" + file);
        } else {
            return resourceLoader.getResource("file:" + file);
        }
    }

    /**
     * 2. Get Class File Set
     * @param resource
     * @return
     */
    private Set<ClassFile> getClassFileSet(Resource resource, String organizationPath){
        Set<ClassFile> classFiles = new HashSet<>();
        // 2.1
        String uriString = getUriStringFromResource(resource);
        // 2.2
        URI u = getUri(uriString);
        Path path = Paths.get(u);
        try (JarFile jar = new JarFile(path.toFile())) {
            List<JarEntry> entries = Collections.list(jar.entries());
            for (JarEntry je: entries
            ) {
                //2.3
                if (isClassFile(je)){
                    //ToDo: Check organization path on modules layer
                    if (je.getName().contains(organizationPath)) {
                        //2.4
                        ClassFile classFile = getClassFileFromJar(jar, je);
                        if (classFile != null) {
                            classFiles.add(classFile);
                        }
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classFiles;
    }

    /**
     * 2.1.
     * @param resource
     * @return
     */
    private String getUriStringFromResource(Resource resource){
        try {
            return resource.getURI().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 2.2
     * @param uri
     * @return
     */
    private URI getUri(String uri){
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 2.3
     * @param entry
     * @return
     */
    private boolean isClassFile(JarEntry entry) {
        return entry.getName().endsWith(".class");
    }

    /**
     * 2.4
     * @param jar
     * @param entry
     * @return
     * ToDo: Do not process jars for libraries, just code!
     */
    private ClassFile getClassFileFromJar(JarFile jar, JarEntry entry) {
        try (InputStream in = jar.getInputStream(entry)) {
            try (DataInputStream data = new DataInputStream(in)) {
                return new ClassFile(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
