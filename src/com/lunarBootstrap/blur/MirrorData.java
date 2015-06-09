/*
 * Decompiled with CFR 0_101.
 */
package com.lunarBootstrap.blur;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public enum MirrorData {
    INSTANCE;
    
    private final List<Mirror> mirrors;
    private int chosenMirror;

    private MirrorData() {
        if (VersionInfo.hasMirrors()) {
            this.mirrors = this.buildMirrorList();
            if (!this.mirrors.isEmpty()) {
                this.chosenMirror = new Random().nextInt(this.getAllMirrors().size());
            }
        } else {
            this.mirrors = Collections.emptyList();
        }
    }

    private List<Mirror> buildMirrorList() {
        String url = VersionInfo.getMirrorListURL();
        ArrayList<Mirror> results = Lists.newArrayList();
        List<String> mirrorList = DownloadUtils.downloadList(url);
        Splitter splitter = Splitter.on('!').trimResults();
        for (String mirror : mirrorList) {
            String[] strings = Iterables.toArray(splitter.split((CharSequence)mirror), String.class);
            Mirror m = new Mirror(strings[0], strings[1], strings[2], strings[3]);
            results.add(m);
        }
        return results;
    }

    public boolean hasMirrors() {
        return VersionInfo.hasMirrors() && this.mirrors != null && !this.mirrors.isEmpty();
    }

    private List<Mirror> getAllMirrors() {
        return this.mirrors;
    }

    private Mirror getChosen() {
        return this.getAllMirrors().get(this.chosenMirror);
    }

    public String getMirrorURL() {
        return this.getChosen().url;
    }

    public String getSponsorName() {
        return this.getChosen().name;
    }

    public String getSponsorURL() {
        return this.getChosen().clickURL;
    }

    public Icon getImageIcon() {
        return this.getChosen().getImage();
    }

    private static class Mirror {
        final String name;
        final String imageURL;
        final String clickURL;
        final String url;
        boolean triedImage;
        Icon image;

        public Mirror(String name, String imageURL, String clickURL, String url) {
            this.name = name;
            this.imageURL = imageURL;
            this.clickURL = clickURL;
            this.url = url;
        }

        Icon getImage() {
            if (!this.triedImage) {
                try {
                    this.image = new ImageIcon(ImageIO.read(new URL(this.imageURL)));
                }
                catch (Exception e) {
                    this.image = null;
                }
                finally {
                    this.triedImage = true;
                }
            }
            return this.image;
        }
    }

}

