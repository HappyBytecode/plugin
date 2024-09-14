package com.sesxh.plugin


import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

public class MagicMethodHookInjectImpl implements Inject {

    MagicMethodHookConfig config
    Project project
    static String owner

    MagicMethodHookInjectImpl(Project project) {
        this.project = project
    }

    void setConfig(MagicMethodHookConfig config) {
        this.config = config
        owner=(config.impl==null||config.impl.isEmpty())?"com/sesxh/magicplugin/tools/MagicHookHandler":config.impl.replace(".", "/")
    }

    @Override
    public boolean isInject(File classFile) {
        return isInjectImpl(classFile.name)
    }

    private static boolean isInjectImpl(String name) {
        if (name.endsWith(".class") && !name.startsWith("R\$") &&
                !"R.class".equals(name) && !"BuildConfig.class".equals(name)) {
            return true
        }
        return false
    }

    @Override
    byte[] injectClass(byte[] clazzBytes) {

        ClassReader cr = new ClassReader(clazzBytes)
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
        ClassVisitor cv = new MagicHookVisitor(cw, config, project)

        cr.accept(cv, EXPAND_FRAMES)

        byte[] code = cw.toByteArray()

        return code

    }

    @Override
    File injectJar(File jarFile, File tempDir) {
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile)
        /** 设置输出到的jar */
        def hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = file.getInputStream(jarEntry)

            String entryName = jarEntry.getName()

            ZipEntry zipEntry = new ZipEntry(entryName)

            jarOutputStream.putNextEntry(zipEntry)

            byte[] modifiedClassBytes = null
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
            if (isInjectImpl(entryName)) {
                modifiedClassBytes = injectClass(sourceClassBytes)
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes)
            } else {
                jarOutputStream.write(modifiedClassBytes)
            }
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    File injectSelfJar(File jarFile, File tempDir) {
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile)
        /** 设置输出到的jar */
        def hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = file.getInputStream(jarEntry)

            String entryName = jarEntry.getName()

            ZipEntry zipEntry = new ZipEntry(entryName)

            jarOutputStream.putNextEntry(zipEntry)

            byte[] modifiedClassBytes = null
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)

            if (entryName.equals(owner+".class")) {
                modifiedClassBytes = dump(config.impl)
            }
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes)
            } else {
                jarOutputStream.write(modifiedClassBytes)
            }
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    /**
     * dump MagicHookHandler
     * @param impl
     * @return
     * @throws Exception
     */
    public static byte[] dump(String impl) throws Exception {
        impl = impl.replace(".", "/")
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;

        cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, owner, null, "java/lang/Object", null);


        fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, "HANDLER", "L"+owner+";", null, null);
        fv.visitEnd();

//        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
//        mv.visitCode();
//        mv.visitVarInsn(Opcodes.ALOAD, 0);
//        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
//        mv.visitInsn(Opcodes.RETURN);
//        mv.visitMaxs(1, 1);
//        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_VARARGS, "enter", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, owner, "HANDLER", "L"+owner+";");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitVarInsn(Opcodes.ALOAD, 5);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, "onMethodEnter", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", true);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(7, 6);
        mv.visitEnd();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_VARARGS, "exit", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(Opcodes.GETSTATIC, owner, "HANDLER", "L"+owner+";");
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitVarInsn(Opcodes.ALOAD, 5);
        mv.visitVarInsn(Opcodes.ALOAD, 6);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, owner, "onMethodReturn", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V", true);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(8, 7);
        mv.visitEnd();

//        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
//        mv.visitCode();
//        mv.visitTypeInsn(Opcodes.NEW, impl);
//        mv.visitInsn(Opcodes.DUP);
//        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, impl, "<init>", "()V", false);
//        mv.visitFieldInsn(Opcodes.PUTSTATIC, "com/sesxh/pluginlib/tools/MagicHookHandler", "M_PRINT", "Lcom/sesxh/pluginlib/tools/IMagicHookHandler;");
//        mv.visitInsn(Opcodes.RETURN);
//        mv.visitMaxs(2, 0);
//        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }
}