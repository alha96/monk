package com.monk.utils;

import com.monk.main.Main;
import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by damarten on 13.07.2017.
 */
public class ClassLoaderHelper {

	static final private FileFilter dirsFilter = new FileFilter() {

		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}

	};
	private static String JAR_SUFFIX = ".JAR";
	static final private FileFilter jarsFilter = new FileFilter() {

		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().toUpperCase().endsWith(JAR_SUFFIX);
		}

	};

	public ClassLoaderHelper() {
	}

	public static ClassLoader buildClassLoader(List<File> directories, boolean includeSubDirectiories) {

		return buildClassLoader(directories, includeSubDirectiories, Main.class.getClassLoader());

	}

	public static ClassLoader buildClassLoader(List<File> directories, boolean includeSubDirectiories, ClassLoader loader) {

		List<URL> jars = new ArrayList<URL>();
		// Find all Jars in each directory
		for (File dir : directories) {
			addJarsToList(jars, dir, includeSubDirectiories);
		}
		return new URLClassLoader(jars.toArray(new URL[jars.size()]), loader);

	}

	private static void addJarsToList(List<URL> jars, File dir, boolean includeSubDirectiories) {

		try {
			for (File jar : dir.listFiles(jarsFilter)) {
				jars.add(jar.toURI().toURL());
			}

			if (includeSubDirectiories) {
				for (File subdir : dir.listFiles(dirsFilter)) {
					addJarsToList(jars, subdir, true);
				}
			}
		} catch (Exception e) {
			Logger.error("Error in ClassLoaderHelper");
		}

	}

}
