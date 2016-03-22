/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.runtime.batch.distributed;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The class launches MR container (AM or Task). This class must be purely depends on standard Java library only
 * and free of any library dependencies. The {@link #launch(String, String, String, String[])}} method is
 * expected to be called by classes generated by {@link ContainerLauncherGenerator}.
 */
public class MapReduceContainerLauncher {

  /**
   * Calls the main method of the given main class.
   *
   * 1. Creates a URLClassLoader from the given classPath, with bootstrap ClassLoader as the parent.
   * 2. Creates a new ClassLoader instance by instantiating the given classLoaderName from the URLClassLoader.
   * 3. Set the context ClassLoader to be the ClassLoader created in step 2.
   * 4. Calls the main method of the mainClassName loaded from the ClassLoader created in step 2.
   *
   * The ClassLoader created in step 2 is actually the MapReduceClassLoader. We cannot refer to it directly
   * in here since this class needs to have zero dependency on any library. We can hardcode the name here, but
   * that would make refactoring in future difficult. So the approach we take is to have the classLoader name
   * passed to this class. The name is "hardcode" in the generated class
   * (generated by {@link ContainerLauncherGenerator} which is invoked by MapReduceRuntimeService in
   * the client side and can uses MapReduceClassLoader.class directly.
   *
   * @see ContainerLauncherGenerator
   */
  public static void launch(String classPath, String classLoaderName,
                            String mainClassName, String[] args) throws Exception {

    System.out.println("Launcher classpath: " + classPath);

    // Expands the classpath
    List<URL> urls = new ArrayList<>();
    for (String path : classPath.split("\\s*,\\s*")) {
      getClassPaths(path, urls);
    }

    ClassLoader baseClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), null);
    Thread.currentThread().setContextClassLoader(baseClassLoader);

    // Creates the MapReduceClassLoader.
    final ClassLoader classLoader = (ClassLoader) baseClassLoader.loadClass(classLoaderName).newInstance();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        if (classLoader instanceof AutoCloseable) {
          try {
            ((AutoCloseable) classLoader).close();
          } catch (Exception e) {
            System.err.println("Failed to close ClassLoader " + classLoader);
            e.printStackTrace();
          }
        }
      }
    });

    Thread.currentThread().setContextClassLoader(classLoader);

    // Invoke MapReduceClassLoader.getTaskContextProvider()
    classLoader.getClass().getDeclaredMethod("getTaskContextProvider").invoke(classLoader);

    System.out.println("printing Mapreduceclassloader classpath");

    URL[] classPaths = ((URLClassLoader) classLoader).getURLs();
    for (URL clsPath: classPaths) {
      System.out.println(clsPath.getFile());
    }

    Class<?> mainClass = classLoader.loadClass(mainClassName);
    Method mainMethod = mainClass.getMethod("main", String[].class);
    mainMethod.setAccessible(true);

    System.out.println("Launch main class " + mainClass + ".main(" + Arrays.toString(args) + ")");
    mainMethod.invoke(null, new Object[]{args});
    System.out.println("Main method returned " + mainClass);
  }

  /**
   * Expands the given path into list of classpath {@link URL}s. If the path ends with "/*", it is treated as
   * classpath wildcard and all jar files under the directory represented by that path will be included.
   */
  private static void getClassPaths(String path, Collection<? super URL> collection) throws MalformedURLException {
    String classpath = expand(path);

    // Non-wildcard
    if (!classpath.endsWith(File.separator + "*")) {
      collection.add(new File(classpath).toURI().toURL());
      return;
    }

    // Wildcard, grab all .jar files
    File dir = new File(classpath.substring(0, classpath.length() - 2));
    File[] files = dir.listFiles();
    if (files == null || files.length == 0) {
      return;
    }

    for (File file : files) {
      if (file.getName().toLowerCase().endsWith(".jar")) {
        collection.add(file.toURI().toURL());
      }
    }
  }

  /**
   * Expands the given value with environment variables.
   * For example, if {@code $JAVA_HOME=/home/java}, then if the given value is {@code $JAVA_HOME/bin/java}, the
   * String returned will be {@code /home/java/bin/java}.
   */
  private static String expand(String value) {
    String result = value;
    for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
      result = result.replace("$" + entry.getKey(), entry.getValue());
      result = result.replace("${" + entry.getKey() + "}", entry.getValue());
    }
    return result;
  }
}
