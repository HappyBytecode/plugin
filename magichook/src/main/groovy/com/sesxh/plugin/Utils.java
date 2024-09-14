package com.sesxh.plugin;

import org.objectweb.asm.Opcodes;

/**
 * @author LYH
 * @date 2021/7/13
 * @time 15:51
 * @desc
 **/
public class Utils implements Opcodes {

    private Utils() {
        throw new AssertionError("no instance");
    }

    static boolean isPrivate(int access) {
        return (access & ACC_PRIVATE) != 0;
    }

    static boolean isPublic(int access) {
        return (access & ACC_PUBLIC) != 0;
    }

    static boolean isStatic(int access) {
        return (access & ACC_STATIC) != 0;
    }

    static boolean isAbstract(int access) {
        return (access & ACC_ABSTRACT) != 0;
    }


    static boolean isViewOnClick(int access, String name, String desc) {
        return (isPublic(access) && !isStatic(access) && !isAbstract(access)) //
                && name.equals("onClick") //
                && desc.equals("(Landroid/view/View;)V");
    }

    static boolean isListViewItemClickMethod(int access, String name, String desc) {
        return (isPublic(access) && !isStatic(access) && !isAbstract(access)) && //
                name.equals("onItemClick") && //
                desc.equals("(Landroid/widget/AdapterView;Landroid/view/View;IJ)V");
    }

    static boolean isBaseQuickAdapterItemClickMethod(int access, String name, String desc) {
        return (isPublic(access) && !isStatic(access) && !isAbstract(access)) && //
                name.equals("onItemClick") && //
                desc.equals("(Lcom/chad/library/adapter/base/BaseQuickAdapter;Landroid/view/View;I)V");
    }

    static boolean isBaseQuickAdapterItemChildClickMethod(int access, String name, String desc) {
        return (isPublic(access) && !isStatic(access) && !isAbstract(access)) && //
                name.equals("onItemChildClick") && //
                desc.equals("(Lcom/chad/library/adapter/base/BaseQuickAdapter;Landroid/view/View;I)V");
    }

    static boolean isButterKnifeClick(String desc,String annotation){
        return annotation.equals("Lbutterknife/OnClick;") && //
                desc.equals("(Landroid/view/View;)V");
    }

    static boolean isAnnotation(String annotation){
        return "Lcom/sesxh/magicplugin/annotation/ClickJitterClick;".equals(annotation);
    }

    static boolean isClick(int access, String name, String desc,String annotation){
        return isViewOnClick(access,name,desc)||
                isListViewItemClickMethod(access,name,desc)||
                isButterKnifeClick(desc,annotation)||
                isAnnotation(annotation)||
                isBaseQuickAdapterItemClickMethod(access, name, desc)||
                isBaseQuickAdapterItemChildClickMethod(access, name, desc);


    }

}
