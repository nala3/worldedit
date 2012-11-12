// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.scripting;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

import com.sk89q.worldedit.WorldEdit;

public class CraftScriptEngines {

    private final ServiceLoader<CraftScriptEngine> loader;

    public CraftScriptEngines(File loadDirectory) {
        this.loader = ServiceLoader.load(CraftScriptEngine.class,
                classLoader(loadDirectory));
    }

    private ClassLoader classLoader(File loadDirectory) {
        URLClassLoader cl = (URLClassLoader) WorldEdit.class.getClassLoader();

        List<URL> urls = new ArrayList<URL>();
        for (File file : loadDirectory.listFiles(new CraftScriptExtFilenameFilter())) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
            }
        }

        return URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), cl);
    }

    public boolean hasEngineForFilename(String filename) {
        return forFilename(filename) != null;
    }

    public CraftScriptEngine forFilename(String filename) {
        Iterator<CraftScriptEngine> it = loader.iterator();
        while (it.hasNext()) {
            try {
                CraftScriptEngine engine = it.next();
                if (engine.acceptFilename(filename)) {
                    return engine;
                }
            } catch (ServiceConfigurationError exc) {
                exc.printStackTrace();
            } catch (NoClassDefFoundError exc) {
                exc.printStackTrace();
            }
        }

        return null;
    }

    private static class CraftScriptExtFilenameFilter implements FilenameFilter {

        private static final String PATTERN = "craftscript-.+.jar$";

        private final Pattern pattern;

        public CraftScriptExtFilenameFilter() {
            pattern = Pattern.compile(PATTERN);
        }

        @Override
        public boolean accept(File dir, String name) {
            return pattern.matcher(name).matches();
        }
    }
}
