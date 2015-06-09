/*
 * Decompiled with CFR 0_101.
 */
package com.lunarBootstrap.blur;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

public class SimpleInstaller {
    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpecBuilder serverInstallOption = parser.accepts("installServer", "Install a server to the current directory");
        OptionSpecBuilder extractOption = parser.accepts("extract", "Extract the contained jar file");
        OptionSpecBuilder helpOption = parser.acceptsAll(Arrays.asList("h", "help"), "Help with this installer");
        OptionSet optionSet = parser.parse(args);
        if (parser.parse(args).nonOptionArguments().size() > 0) {
            SimpleInstaller.handleOptions(parser, optionSet, serverInstallOption, extractOption, helpOption);
        } else {
            SimpleInstaller.launchGui();
        }
    }

    private static void handleOptions(OptionParser parser, OptionSet optionSet, OptionSpecBuilder serverInstallOption, OptionSpecBuilder extractOption, OptionSpecBuilder helpOption) throws IOException {
        String path = VersionInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.contains((CharSequence)"!/")) {
            System.out.println("Due to java limitation, please do not run this jar in a folder ending with !");
            System.out.println(path);
            return;
        }
        if (optionSet.has(serverInstallOption)) {
            try {
                VersionInfo.getVersionTarget();
                ServerInstall.headless = true;
                System.out.println("Installing server to current directory");
                if (!InstallerAction.SERVER.run(new File("."))) {
                    System.err.println("There was an error during server installation");
                    System.exit(1);
                } else {
                    System.out.println("The server installed successfully, you should now be able to run the file " + VersionInfo.getContainedFile());
                    System.out.println("You can delete this installer file now if you wish");
                }
                System.exit(0);
            }
            catch (Throwable e) {
                System.err.println("A problem installing the server was detected, server install cannot continue");
                System.exit(1);
            }
        } else if (optionSet.has(extractOption)) {
            try {
                VersionInfo.getVersionTarget();
                if (!InstallerAction.EXTRACT.run(new File("."))) {
                    System.err.println("A problem occurred extracting the file to " + VersionInfo.getContainedFile());
                    System.exit(1);
                } else {
                    System.out.println("File extracted successfully to " + VersionInfo.getContainedFile());
                    System.out.println("You can delete this installer file now if you wish");
                }
                System.exit(0);
            }
            catch (Throwable e) {
                System.err.println("A problem extracting the file was detected, extraction failed");
                System.exit(1);
            }
        } else {
            parser.printHelpOn(System.err);
        }
    }

    private static void launchGui() {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        File targetDir = null;
        String mcDir = ".minecraft";
        targetDir = osType.contains((CharSequence)"win") && System.getenv("APPDATA") != null ? new File(System.getenv("APPDATA"), mcDir) : (osType.contains((CharSequence)"mac") ? new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft") : new File(userHomeDir, mcDir));
        String path = VersionInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.contains((CharSequence)"!/")) {
            JOptionPane.showMessageDialog(null, "Due to java limitation, please do not run this jar in a folder ending with ! : \n" + path, "Error", 0);
            return;
        }
        try {
            VersionInfo.getVersionTarget();
        }
        catch (Throwable e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Corrupt download detected, cannot install", "Error", 0);
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            // empty catch block
        }
        InstallerPanel panel = new InstallerPanel(targetDir);
        panel.run();
    }
}

