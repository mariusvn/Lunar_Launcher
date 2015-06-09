/*
 * Decompiled with CFR 0_101.
 */
package com.lunarBootstrap.blur;

import argo.jdom.JsonNode;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.ZipEntry;

import javax.swing.ProgressMonitor;

import org.tukaani.xz.XZInputStream;

public class DownloadUtils {
    public static int downloadInstalledLibraries(String jsonMarker, File librariesDir, IMonitor monitor, List<JsonNode> libraries, int progress, List<String> grabbed, List<String> bad) {
        for (JsonNode library : libraries) {
            String libName = library.getStringValue("name");
            ArrayList<String> checksums = null;
            if (library.isArrayNode("checksums")) {
                checksums = Lists.newArrayList(Lists.transform(library.getArrayNode("checksums"), new Function<JsonNode, String>(){

                    @Override
                    public String apply(JsonNode node) {
                        return node.getText();
                    }
                }));
            }
            monitor.setNote(String.format("Considering library %s", libName));
            if (library.isBooleanValue(jsonMarker) && library.getBooleanValue(jsonMarker).booleanValue()) {
                String[] nameparts = Iterables.toArray(Splitter.on(':').split((CharSequence)libName), String.class);
                nameparts[0] = nameparts[0].replace('.', '/');
                String jarName = nameparts[1] + '-' + nameparts[2] + ".jar";
                String pathName = nameparts[0] + '/' + nameparts[1] + '/' + nameparts[2] + '/' + jarName;
                File libPath = new File(librariesDir, pathName.replace('/', File.separatorChar));
                String libURL = "https://libraries.minecraft.net/";
                if (MirrorData.INSTANCE.hasMirrors() && library.isStringValue("url")) {
                    libURL = MirrorData.INSTANCE.getMirrorURL();
                } else if (library.isStringValue("url")) {
                    libURL = library.getStringValue("url") + "/";
                }
                if (libPath.exists() && DownloadUtils.checksumValid(libPath, checksums)) {
                    monitor.setProgress(progress++);
                    continue;
                }
                libPath.getParentFile().mkdirs();
                monitor.setNote(String.format("Downloading library %s", libName));
                libURL = libURL + pathName;
                File packFile = new File(libPath.getParentFile(), libPath.getName() + ".pack.xz");
                if (!DownloadUtils.downloadFile(libName, packFile, libURL + ".pack.xz", null)) {
                    if (library.isStringValue("url")) {
                        monitor.setNote(String.format("Trying unpacked library %s", libName));
                    }
                    if (!DownloadUtils.downloadFile(libName, libPath, libURL, checksums)) {
                        if (!(libURL.startsWith("https://libraries.minecraft.net/") && jsonMarker.equals("clientreq"))) {
                            bad.add(libName);
                        } else {
                            monitor.setNote("Unmrriored file failed, Mojang launcher should download at next run, non fatal");
                        }
                    } else {
                        grabbed.add(libName);
                    }
                } else {
                    try {
                        monitor.setNote(String.format("Unpacking packed file %s", packFile.getName()));
                        DownloadUtils.unpackLibrary(libPath, Files.toByteArray(packFile));
                        monitor.setNote(String.format("Successfully unpacked packed file %s", packFile.getName()));
                        packFile.delete();
                        if (DownloadUtils.checksumValid(libPath, checksums)) {
                            grabbed.add(libName);
                        } else {
                            bad.add(libName);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        bad.add(libName);
                    }
                }
            }
            monitor.setProgress(progress++);
        }
        return progress;
    }

    private static boolean checksumValid(File libPath, List<String> checksums) {
        try {
            boolean valid;
            byte[] fileData = Files.toByteArray(libPath);
            boolean bl = valid = checksums == null || checksums.isEmpty() || checksums.contains(Hashing.sha1().hashBytes(fileData).toString());
            if (!valid && libPath.getName().endsWith(".jar")) {
                valid = DownloadUtils.validateJar(libPath, fileData, checksums);
            }
            return valid;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void unpackLibrary(File output, byte[] data) throws IOException {
        String end;
        byte[] decompressed;
        if (output.exists()) {
            output.delete();
        }
        if (!(end = new String(decompressed = DownloadUtils.readFully(new XZInputStream(new ByteArrayInputStream(data))), decompressed.length - 4, 4)).equals("SIGN")) {
            System.out.println("Unpacking failed, signature missing " + end);
            return;
        }
        int x = decompressed.length;
        int len = decompressed[x - 8] & 255 | (decompressed[x - 7] & 255) << 8 | (decompressed[x - 6] & 255) << 16 | (decompressed[x - 5] & 255) << 24;
        byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);
        FileOutputStream jarBytes = new FileOutputStream(output);
        JarOutputStream jos = new JarOutputStream(jarBytes);
        Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);
        jos.putNextEntry(new JarEntry("checksums.sha1"));
        jos.write(checksums);
        jos.closeEntry();
        jos.close();
        jarBytes.close();
    }

    public static boolean validateJar(File libPath, byte[] data, List<String> checksums) throws IOException {
        System.out.println("Checking \"" + libPath.getAbsolutePath() + "\" internal checksums");
        HashMap<String, String> files = new HashMap<String, String>();
        String[] hashes = null;
        JarInputStream jar = new JarInputStream(new ByteArrayInputStream(data));
        JarEntry entry = jar.getNextJarEntry();
        while (entry != null) {
            byte[] eData = DownloadUtils.readFully(jar);
            if (entry.getName().equals("checksums.sha1")) {
                hashes = new String(eData, Charset.forName("UTF-8")).split("\n");
            }
            if (!entry.isDirectory()) {
                files.put(entry.getName(), Hashing.sha1().hashBytes(eData).toString());
            }
            entry = jar.getNextJarEntry();
        }
        jar.close();
        if (hashes != null) {
            boolean failed;
            boolean bl = failed = !checksums.contains(files.get("checksums.sha1"));
            if (failed) {
                System.out.println("    checksums.sha1 failed validation");
            } else {
                System.out.println("    checksums.sha1 validated successfully");
                for (String hash : hashes) {
                    if (hash.trim().equals("")) continue;
                    if (!hash.contains((CharSequence)" ")) continue;
                    String[] e = hash.split(" ");
                    String validChecksum = e[0];
                    String target = e[1];
                    String checksum = (String)files.get(target);
                    if (!(files.containsKey(target) && checksum != null)) {
                        System.out.println("    " + target + " : missing");
                        failed = true;
                        continue;
                    }
                    if (checksum.equals(validChecksum)) continue;
                    System.out.println("    " + target + " : failed (" + checksum + ", " + validChecksum + ")");
                    failed = true;
                }
            }
            if (!failed) {
                System.out.println("    Jar contents validated successfully");
            }
            return !failed;
        }
        System.out.println("    checksums.sha1 was not found, validation failed");
        return false;
    }

    public static List<String> downloadList(String libURL) {
        try {
            URL url = new URL(libURL);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            URLISSupplier urlSupplier = new URLISSupplier(connection);
            
            return CharStreams.readLines(CharStreams.newReaderSupplier(urlSupplier, Charsets.UTF_8));
        }
        catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static boolean downloadFile(String libName, File libPath, String libURL, List<String> checksums) {
        try {
            URL url = new URL(libURL);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            URLISSupplier urlSupplier = new URLISSupplier(connection);
            Files.copy(urlSupplier, libPath);
            if (DownloadUtils.checksumValid(libPath, checksums)) {
                return true;
            }
            return false;
        }
        catch (FileNotFoundException fnf) {
            if (!libURL.endsWith(".pack.xz")) {
                fnf.printStackTrace();
            }
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] readFully(InputStream stream) throws IOException {
        int len;
        byte[] data = new byte[4096];
        ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
        do {
            if ((len = stream.read(data)) <= 0) continue;
            entryBuffer.write(data, 0, len);
        } while (len != -1);
        return entryBuffer.toByteArray();
    }

    public static IMonitor buildMonitor() {
        if (ServerInstall.headless) {
            return new IMonitor(){

                @Override
                public void setMaximum(int max) {
                }

                @Override
                public void setNote(String note) {
                    System.out.println("MESSAGE: " + note);
                }

                @Override
                public void setProgress(int progress) {
                }

                @Override
                public void close() {
                }
            };
        }
        return new IMonitor(){
            private ProgressMonitor monitor = new ProgressMonitor(null, "Downloading libraries", "Libraries are being analyzed", 0, 1);

            @Override
            public void setMaximum(int max) {
                this.monitor.setMaximum(max);
            }

            @Override
            public void setNote(String note) {
                System.out.println(note);
                this.monitor.setNote(note);
            }

            @Override
            public void setProgress(int progress) {
                this.monitor.setProgress(progress);
            }

            @Override
            public void close() {
                this.monitor.close();
            }
        };
    }

    static class URLISSupplier
    implements InputSupplier<InputStream> {
        private final URLConnection connection;

        private URLISSupplier(URLConnection connection) {
            this.connection = connection;
        }

        @Override
        public InputStream getInput() throws IOException {
            return this.connection.getInputStream();
        }
    }

}

