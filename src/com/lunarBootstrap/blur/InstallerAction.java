package com.lunarBootstrap.blur;

import com.google.common.base.Throwables;
import java.io.File;
import javax.swing.Icon;

public enum InstallerAction {
    CLIENT("Install client", "Install a new profile to the Mojang client launcher", ClientInstall.class),
    SERVER("Install server", "Create a new modded server installation", ServerInstall.class),
    EXTRACT("Extract", "Extract the contained jar file", ExtractAction.class);
    
    private String label;
    private String tooltip;
    private ActionType action;

    private InstallerAction(String label, String tooltip, Class<? extends ActionType> action) {
        this.label = label;
        this.tooltip = tooltip;
        try {
            this.action = action.newInstance();
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public String getButtonLabel() {
        return this.label;
    }

    public String getTooltip() {
        return this.tooltip;
    }

    public boolean run(File path) {
        return this.action.run(path);
    }

    public boolean isPathValid(File targetDir) {
        return this.action.isPathValid(targetDir);
    }

    public String getFileError(File targetDir) {
        return this.action.getFileError(targetDir);
    }

    public String getSuccessMessage() {
        return this.action.getSuccessMessage();
    }

    public String getSponsorMessage() {
        return this.action.getSponsorMessage();
    }

    public Icon getSponsorLogo() {
        return MirrorData.INSTANCE.getImageIcon();
    }

    public String getSponsorURL() {
        return MirrorData.INSTANCE.getSponsorURL();
    }
}

