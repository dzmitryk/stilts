/*
 * Copyright 2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License 2.0. 
 * 
 * You should have received a copy of the Apache License 
 * along with this software; if not, please see:
 * http://apache.org/licenses/LICENSE-2.0.txt
 */

package org.projectodd.stilts.clownshoes.weld;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileBeanDeploymentArchive extends SimpleCircusBeanDeploymentArchive {

    public FileBeanDeploymentArchive(File file) throws Exception {
        this( file, null );
    }

    public FileBeanDeploymentArchive(File file, ClassLoader parent) throws Exception {
        super( file.getAbsolutePath(), new URLClassLoader( new URL[] { file.toURI().toURL() }, parent ) );
        this.file = file;
        scanDeployment();
    }

    protected void scanDeployment() throws IOException {
        if (this.file.isDirectory()) {
            scanDirectory();
        } else {
            scanArchive();
        }
    }

    protected void scanDirectory() {
        scanDirectory( null, this.file );
    }

    protected void scanDirectory(String prefix, File dir) {
        if (prefix == null) {
            prefix = "";
        } else {
            prefix = prefix + ".";
        }

        for (File child : dir.listFiles()) {
            if (child.isDirectory()) {
                scanDirectory( prefix + child.getName(), child );
            } else {
                String name = child.getName();
                if (name.endsWith( ".class" )) {
                    name = name.substring( 0, name.length() - 6 );
                    addBeanClass( prefix + name );
                }
            }
        }
    }

    protected void scanArchive() throws IOException {
        JarFile archive = new JarFile( this.file );

        Enumeration<JarEntry> entries = archive.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith( ".class" )) {
                name = name.substring( 0, name.length() - 6 );
                name = name.replaceAll( "/", "." );
                addBeanClass( name );
            }
        }
    }

    private File file;

}
