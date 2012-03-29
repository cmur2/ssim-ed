package de.mycrobase.ssim.ed;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class CLI {
    
    private static final Logger logger = Logger.getLogger(CLI.class);
    
    private static final String SettingsPrefix = "D";
    
    private String[] args;
    private Properties cmdLineProperties;
    
    public CLI(String[] args) {
        this.args = args;
    }
    
    public boolean parse() {
        logger.info("Start parsing command line parameters");
        
        CommandLineParser parser = new GnuParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(getOptions(), args, false);
        } catch(ParseException ex) {
            logger.info("Exception occured during command line parsing follows...", ex);
            return false;
        }
        
        // interpret values from parser
        cmdLineProperties = cmd.getOptionProperties(SettingsPrefix);
        
        return true;
    }
    
    public Properties getCmdLineProperties() {
        return cmdLineProperties;
    }
    
    private Options getOptions() {
        Options opts = new Options();
        {
            // make parser aware that there might be Java style options
            // with -Dfoo=bar:
            Option minusD = new Option(SettingsPrefix,
                "Settings for SED overwriting all other settings");
            minusD.setArgName("property=value"); // unnecessary, might be empty
            minusD.setArgs(2);
            minusD.setValueSeparator('=');
            opts.addOption(minusD);
        }
        return opts;
    }
}
