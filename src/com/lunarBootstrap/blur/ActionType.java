/*
 * Decompiled with CFR 0_101.
 */
package com.lunarBootstrap.blur;

import java.io.File;

public interface ActionType {
    public boolean run(File var1);

    public boolean isPathValid(File var1);

    public String getFileError(File var1);

    public String getSuccessMessage();

    public String getSponsorMessage();
}

