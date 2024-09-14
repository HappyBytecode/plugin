package com.sesxh.plugin;

import java.io.File;

/**
 * @author LYH
 **/
public interface Inject {
    boolean isInject(File file);

    byte[] injectClass(byte[] file);

    File injectJar(File file, File tempDir);
}
