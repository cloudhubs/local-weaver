package edu.baylor.ecs.cfgg.evaluator.service;

import javassist.bytecode.ClassFile;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Service
public class ClassPathScanner {

    private final Set<ClassFile> classes = new HashSet<>();

    public void scanUri(String uri){
        URI u = null;
        try {
            u = new URI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Path path = Paths.get(u);
        try (JarFile jar = new JarFile(path.toFile())) {
            List<JarEntry> entries = Collections.list(jar.entries());
            for (JarEntry je: entries
            ) {
                if (isClassFile(je)){
                    processClass(jar, je);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean isClassFile(JarEntry entry) {
        return entry.getName().endsWith(".class");
    }

    protected void processClass(JarFile jar, JarEntry entry) {
        try (InputStream in = jar.getInputStream(entry)) {
            try (DataInputStream data = new DataInputStream(in)) {
                classes.add(new ClassFile(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<ClassFile> getClasses() {
        return classes;
    }
}