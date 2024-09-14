package com.sesxh.plugin


import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author LYH
 **/

class MagicMethodHookPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("magichook", MagicMethodHookConfig)
        def android = project.extensions.android
        boolean islib = true
        if (android instanceof AppExtension) {
            islib = false
        }
        android.registerTransform(new MagicHookTransform(project, islib))
    }



}
