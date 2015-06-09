/*
 * Decompiled with CFR 0_101.
 */
package com.lunarBootstrap.blur;

import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.jdom.JsonRootNode;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class VersionInfo {
    public static final VersionInfo INSTANCE = new VersionInfo();
    public final JsonRootNode versionData;

    public VersionInfo() {
        InputStream installProfile = this.getClass().getResourceAsStream("/install_profile.json");
        JdomParser parser = new JdomParser();
        try {
            this.versionData = parser.parse(new InputStreamReader(installProfile, Charsets.UTF_8));
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static String getProfileName() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "profileName");
    }

    public static String getVersionTarget() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "target");
    }

    public static File getLibraryPath(File root) {
        String path = VersionInfo.INSTANCE.versionData.getStringValue("install", "path");
        String[] split = Iterables.toArray(Splitter.on(':').omitEmptyStrings().split((CharSequence)path), String.class);
        File dest = root;
        Iterable<String> subSplit = Splitter.on('.').omitEmptyStrings().split((CharSequence)split[0]);
        for (String part : subSplit) {
            dest = new File(dest, part);
        }
        dest = new File(new File(dest, split[1]), split[2]);
        String fileName = split[1] + "-" + split[2] + ".jar";
        return new File(dest, fileName);
    }

    public static String getVersion() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "version");
    }

    public static String getWelcomeMessage() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "welcome");
    }

    public static String getLogoFileName() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "logo");
    }

    public static boolean getStripMetaInf() {
        try {
            return VersionInfo.INSTANCE.versionData.getBooleanValue("install", "stripMeta");
        }
        catch (Exception e) {
            return false;
        }
    }

    public static JsonNode getVersionInfo() {
        return VersionInfo.INSTANCE.versionData.getNode("versionInfo");
    }

    public static File getMinecraftFile(File path) {
        return new File(new File(path, VersionInfo.getMinecraftVersion()), VersionInfo.getMinecraftVersion() + ".jar");
    }

    public static String getContainedFile() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "filePath");
    }

    public static void extractFile(File path) throws IOException {
        //INSTANCE.doFileExtract(path);
    }

    private void doFileExtract(File path){
    	try{
    		InputStream inputStream = this.getClass().getResourceAsStream("/" + VersionInfo.getContainedFile());
    		OutputSupplier<FileOutputStream> outputSupplier = Files.newOutputStreamSupplier(path);
    		ByteStreams.copy(inputStream, outputSupplier);
    	}catch(IOException ex){
    		ex.printStackTrace();
    	}
    }

    public static String getMinecraftVersion() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "minecraft");
    }

    public static String getMirrorListURL() {
        return VersionInfo.INSTANCE.versionData.getStringValue("install", "mirrorList");
    }

    public static boolean hasMirrors() {
        return VersionInfo.INSTANCE.versionData.isStringValue("install", "mirrorList");
    }

    public static boolean hideClient() {
        return VersionInfo.INSTANCE.versionData.isBooleanValue("install", "hideClient") && VersionInfo.INSTANCE.versionData.getBooleanValue("install", "hideClient") != false;
    }

    public static boolean hideServer() {
        return VersionInfo.INSTANCE.versionData.isBooleanValue("install", "hideServer") && VersionInfo.INSTANCE.versionData.getBooleanValue("install", "hideServer") != false;
    }
}

