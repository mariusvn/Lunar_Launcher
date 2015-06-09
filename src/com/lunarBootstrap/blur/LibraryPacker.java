/*
 * Decompiled with CFR 0_101.
 */
package com.lunarBootstrap.blur;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.ZipEntry;
import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class LibraryPacker {
    private static final ArrayList<String> CHECKSUMS = new ArrayList();
    private static final OutputStream NULL_OUT = new OutputStream(){

        @Override
        public void write(int b) throws IOException {
        }
    };

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java cpw.mods.fml.installer.LibraryPacker <path1> [path2...]");
            System.out.println("This program will walk the supplied paths recursivly and create compressed versions of any .jar file they find.");
            return;
        }
        for (String path : args) {
            LibraryPacker.walk(new File(path));
        }
    }

    private static void walk(File path) throws IOException {
        if (path.isDirectory()) {
            for (File child : path.listFiles()) {
                LibraryPacker.walk(child);
            }
        } else if (path.getName().endsWith(".jar")) {
            byte[] xz = LibraryPacker.compress(path);
            LibraryPacker.decompress(path, xz, CHECKSUMS);
        }
    }

    private static byte[] compress(File path) throws IOException {
        File lzma = new File(path.getAbsolutePath() + ".pack.lzma");
        if (lzma.exists()) {
            System.out.println("Skipping \"" + path.getAbsolutePath() + "\" lzma already exists");
            return null;
        }
        System.out.println("Processing: " + path.getAbsolutePath());
        byte[] raw = Files.toByteArray(path);
        System.out.println("  Raw:        " + raw.length);
        System.out.println("  SHA1:       " + Hashing.sha1().hashBytes(raw).toString());
        byte[] packed = LibraryPacker.pack(raw, path);
        System.out.println("  Packed:     " + packed.length);
        byte[] unpacked = LibraryPacker.unpack(packed, path);
        System.out.println("  Unpacked:   " + unpacked.length);
        byte[] checksums = LibraryPacker.checksum(unpacked, path);
        System.out.println("  SHA1:       " + Hashing.sha1().hashBytes(checksums).toString());
        CHECKSUMS.add(Hashing.sha1().hashBytes(checksums).toString());
        byte[] xzed = LibraryPacker.xz(packed, checksums, path);
        System.out.println("  XZed:       " + xzed.length);
        System.out.println("");
        return xzed;
    }

    private static byte[] checksum(byte[] raw, File path) throws IOException {
        JarInputStream in = new JarInputStream(new ByteArrayInputStream(raw));
        StringBuffer checksums = new StringBuffer();
        JarEntry entry = in.getNextJarEntry();
        while (entry != null) {
            if (!entry.isDirectory()) {
                checksums.append(Hashing.sha1().hashBytes(DownloadUtils.readFully(in)).toString()).append(' ').append(entry.getName()).append('\n');
            }
            entry = in.getNextJarEntry();
        }
        in.close();
        return checksums.toString().getBytes(Charset.forName("UTF-8"));
    }

    private static byte[] pack(byte[] data, File path) throws IOException {
        JarInputStream in = new JarInputStream(new ByteArrayInputStream(data)){

            @Override
            public ZipEntry getNextEntry() throws IOException {
                ZipEntry ret = super.getNextEntry();
                while (ret != null && ret.getName().startsWith("META-INF")) {
                    ret = super.getNextEntry();
                }
                return ret;
            }
        };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Pack200.Packer packer = Pack200.newPacker();
        SortedMap<String, String> props = packer.properties();
        props.put("pack.effort", "9");
        props.put("pack.keep.file.order", "true");
        props.put("pack.unknown.attribute", "pass");
        PrintStream err = new PrintStream(System.err);
        System.setErr(new PrintStream(NULL_OUT));
        packer.pack(in, (OutputStream)out);
        System.setErr(err);
        in.close();
        out.close();
        byte[] packed = out.toByteArray();
        return packed;
    }

    private static byte[] unpack(byte[] data, File path) throws IOException {
        File output = new File(path.getAbsolutePath() + ".unpacked");
        if (output.exists()) {
            output.delete();
        }
        FileOutputStream jar = new FileOutputStream(output);
        JarOutputStream jos = new JarOutputStream(jar);
        Pack200.newUnpacker().unpack(new ByteArrayInputStream(data), jos);
        jos.close();
        jar.close();
        byte[] unpacked = Files.toByteArray(output);
        output.delete();
        return unpacked;
    }

    private static byte[] xz(byte[] data, byte[] checksums, File path) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        LZMA2Options options = new LZMA2Options();
        options.setPreset(8);
        XZOutputStream xz = new XZOutputStream(out, options);
        int x = checksums.length;
        xz.write(data);
        xz.write(checksums);
        xz.write(new byte[]{(byte)(x & 255), (byte)((x & 65280) >> 8), (byte)((x & 16711680) >> 16), (byte)((x & -16777216) >> 24)});
        xz.write("SIGN".getBytes());
        xz.close();
        byte[] xzed = out.toByteArray();
        Files.write(xzed, new File(path.getAbsolutePath() + ".pack.xz"));
        return xzed;
    }

    private static void decompress(File path, byte[] data, List<String> checksum) throws IOException {
        Stopwatch t = new Stopwatch();
        t.start();
        File output = new File(path.getAbsolutePath() + ".unpacked.test");
        DownloadUtils.unpackLibrary(new File(path.getAbsolutePath() + ".unpacked.test"), data);
        DownloadUtils.validateJar(output, Files.toByteArray(output), CHECKSUMS);
        t.stop();
        System.out.println("  Decompress: " + t.toString());
    }

    private static class Stopwatch {
        private long fStart;
        private long fStop;
        private boolean fIsRunning;

        private Stopwatch() {
        }

        public void start() {
            if (this.fIsRunning) {
                throw new IllegalStateException("Must stop before calling start again.");
            }
            this.fStart = System.currentTimeMillis();
            this.fStop = 0;
            this.fIsRunning = true;
        }

        public void stop() {
            if (!this.fIsRunning) {
                throw new IllegalStateException("Cannot stop if not currently running.");
            }
            this.fStop = System.currentTimeMillis();
            this.fIsRunning = false;
        }

        public String toString() {
            return "" + this.length() + " ms";
        }

        public long length() {
            return this.fStop - this.fStart;
        }
    }

}

