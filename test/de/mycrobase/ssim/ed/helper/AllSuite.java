package de.mycrobase.ssim.ed.helper;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class AllSuite extends Suite {
    
    private static final String BUILD_DIR = "bin";
    
    public AllSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
        super(builder, suiteClass, getAllTestClasses(getOurClassPath()));
    }
    
    private static File getOurClassPath() {
//        System.out.println(System.getProperty("java.class.path"));
//        String ourClassPath =
//            System.getProperty("java.class.path").split(System.getProperty("path.separator"))[0];
        return new File(BUILD_DIR);
    }
    
    private static Class<?>[] getAllTestClasses(File classPath) {
        List<File> allTestClassFiles = getAllFiles(classPath, new FileFilter() {
            @Override
            public boolean accept(File file) {
                if(file.isDirectory()) {
                    return true;
                }
                if(file.isFile()) {
                    return file.getName().endsWith("Test.class");
                }
                return false;
            }
        });
        
        List<Class<?>> allTestClasses = new LinkedList<Class<?>>();
        
        for(File f : allTestClassFiles) {
            String className = f.getAbsolutePath();
            className = className.substring(0, className.lastIndexOf(".class"));
            className = className.substring(classPath.getAbsolutePath().length()+1);
            className = className.replace(File.separatorChar, '.');
            try {
                Class<?> clazz = Class.forName(className);
                allTestClasses.add(clazz);
            } catch(ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        
        return allTestClasses.toArray(new Class<?>[0]);
    }
    
    private static List<File> getAllFiles(File root, FileFilter filter) {
        LinkedList<File> allFiles = new LinkedList<File>();
        for(File f : root.listFiles(filter)) {
            if(f.isFile()) {
                allFiles.add(f);
            } else {
                allFiles.addAll(getAllFiles(f, filter));
            }
        }
        return allFiles;
    }
}
