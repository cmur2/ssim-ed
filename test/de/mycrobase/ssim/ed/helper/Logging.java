package de.mycrobase.ssim.ed.helper;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.altimos.util.logger.JLFBridge;

public class Logging {
    
    private static boolean inited = false;
    
    private Logging() {
    }
    
    public static void require() {
        if(!inited) {
            //org.apache.log4j.BasicConfigurator.configure();
            {
                Logger root = Logger.getRootLogger();
                root.addAppender(new ConsoleAppender(new PatternLayout("%-3r [%t] %-5p %c: %m%n")));
            }
            JLFBridge.installBridge();
            // prevent NPE in VertexBuffer.toString() during NativeObjectManager.deleteUnused()
            java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINER);
            // disable scene graph Node attach/detach logging for performance
            // (an issue mostly for terrain)
            java.util.logging.Logger.getLogger("com.jme3.scene.Node").setLevel(java.util.logging.Level.OFF);
        }
    }
}
