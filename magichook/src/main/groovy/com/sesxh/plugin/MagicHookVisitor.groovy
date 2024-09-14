package com.sesxh.plugin


import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter

import java.util.regex.Pattern

/**
 * @author LYH
 **/

class MagicHookVisitor extends ClassVisitor {

    String className = null;
    String owner
    String clickOwner
    String traceIdType="Lcom/sesxh/magicplugin/annotation/TraceId;";
    String nodeIdType="Lcom/sesxh/magicplugin/annotation/NodeId;";
    String valueIdType="Lcom/sesxh/magicplugin/annotation/Value;";
    String methodAnnotation="";
    Map<String,String> parameterAnnotations;
    MagicMethodHookConfig config;
    Project project;


    boolean isIgnoreHookMethod = false

    MagicHookVisitor(ClassVisitor classVisitor, MagicMethodHookConfig config, Project project) {
        super(Opcodes.ASM5, classVisitor);
        this.config = config;
        this.project = project;
        parameterAnnotations=new HashMap<>();
        owner=(config.impl==null||config.impl.isEmpty())?"com/sesxh/magicplugin/tools/MagicHookHandler":config.impl.replace(".", "/")
        clickOwner=(config.clickImpl==null||config.clickImpl.isEmpty())?"com/sesxh/magicplugin/tools/CheckClickJitterUtil":config.clickImpl.replace(".", "/")
    }


    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name.replace("/", ".")
    }

    @Override
    AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        isIgnoreHookMethod = descriptor == "Lcom/sesxh/magicplugin/annotation/IgnoreHookMethod;"
        return super.visitAnnotation(descriptor, visible)
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {

        def isInit = name == "<init>"
        def isStaticInit = name == "<clinit>"
        if (isIgnoreHookMethod||isInit||isStaticInit)
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        def isUnImplMethod = (access & Opcodes.ACC_ABSTRACT) != 0
        if (isUnImplMethod) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }

        Type[] argsTypes = Type.getArgumentTypes(descriptor)
        Type returnType = Type.getReturnType(descriptor)
        def isStatic = (access & Opcodes.ACC_STATIC) != 0


        def mv = cv.visitMethod(access, name, descriptor, signature, exceptions)
        mv = new AdviceAdapter(Opcodes.ASM5, mv, access, name, descriptor) {

            private boolean inject = config.isAll();

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if ("Lcom/sesxh/magicplugin/annotation/HookMethod;" == desc) {
                    inject = true
                } else if ("Lcom/sesxh/magicplugin/annotation/IgnoreHookMethod;" == desc) {
                    inject = false
                }
                methodAnnotation=desc;
                return super.visitAnnotation(desc, visible);
            }

            private boolean isInject() {
                if (!config.isEnable()) {
                    return false;
                }
                if (inject) {
                    return true;
                }
                for (String value : config.getClassRegexs()) {
                    if (Pattern.matches(value, className)) {
                        for (String mname : config.getMethodRegexs()) {
                            if (Pattern.matches(mname, name)) {
                                return true;
                            }
                        }
                    }
                }
                for (String value : config.getMethodRegexs()) {
                    if (Pattern.matches(value, name)) {
                        return true;
                    }
                }

                return false;
            }

            @Override
            AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor2, boolean visible) {
                parameterAnnotations.put(descriptor2,parameter+"");
                return super.visitParameterAnnotation(parameter, descriptor2, visible)
            }


            @Override
            protected synchronized void onMethodEnter() {
                if (!isInject()||!config.isTimeStatistics) return
                getArgs()
                mv.visitMethodInsn(INVOKESTATIC, owner,
                        "enter",
                        "(" + "Ljava/lang/String;" +
                                "Ljava/lang/String;" +
                                "Ljava/lang/String;" +
                                "Ljava/lang/String;" +
                                "Ljava/lang/String;"+
                                "Ljava/lang/String;"+
                                "Ljava/lang/String;"+
                                "[Ljava/lang/Object;" +
                                ")V",
                        false);
                super.onMethodEnter()
            }

            @Override
            void visitCode() {
                super.visitCode()
                if(isInject()&&config.isClickJitter&&Utils.isClick(access,name,descriptor,methodAnnotation)){
                    mv.visitMethodInsn(INVOKESTATIC, clickOwner, "isClickJitter", "()Z", false)
                    Label label = new Label()
                    mv.visitJumpInsn(IFEQ, label)
                    mv.visitInsn(RETURN)
                    mv.visitLabel(label)
                }
            }

            @Override
            protected synchronized void onMethodExit(int opcode) {
                if (!isInject()||!config.isTimeStatistics) return
                //有返回值的装载返回值参数，无返回值的装载null
                if (opcode >= IRETURN && opcode < RETURN) {
                    if (returnType.sort == Type.LONG || returnType.sort == Type.DOUBLE) {
                        mv.visitInsn(DUP2)
                    } else {
                        mv.visitInsn(DUP)
                    }
                    ClassUtils.autoBox(mv, returnType)
                    getArgs()
                    mv.visitMethodInsn(INVOKESTATIC, owner,
                            "exit",
                            "(" +
                                    "Ljava/lang/Object;" + //return
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;"+
                                    "Ljava/lang/String;"+
                                    "Ljava/lang/String;"+
                                    "[Ljava/lang/Object;" +//prams
                                    ")V",
                            false);
                } else if (opcode == RETURN) {
                    mv.visitInsn(ACONST_NULL)
                    getArgs()
                    mv.visitMethodInsn(INVOKESTATIC, owner,
                            "exit",
                            "(" +
                                    "Ljava/lang/Object;" + //return
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;" +
                                    "Ljava/lang/String;"+
                                    "Ljava/lang/String;"+
                                    "Ljava/lang/String;"+
                                    "[Ljava/lang/Object;" +//prams
                                    ")V",
                            false);
                }


            }

            /**
             * 装载5 个参数
             */
            private void getArgs() {
                mv.visitLdcInsn(className);//className
                mv.visitLdcInsn(name);//methodbName
                mv.visitLdcInsn(getArgsType());//argsTypes
                mv.visitLdcInsn(returnType.className);//returntype
                mv.visitLdcInsn(getIndex(0));//traceIdIndex
                mv.visitLdcInsn(getIndex(1));//nodeIdIndex
                mv.visitLdcInsn(getIndex(2));//valueIndex
                getICONST(argsTypes == null ? 0 : argsTypes.length);
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                if (argsTypes != null) {
                    def valLen = 0;
                    for (int i = 0; i < argsTypes.length; i++) {
                        mv.visitInsn(DUP);
                        getICONST(i);
                        getOpCodeLoad(argsTypes[i], isStatic ? (valLen) : (valLen + 1));
                        mv.visitInsn(AASTORE);
                        valLen += getlenByType(argsTypes[i])
                    }
                }
            }

            String getIndex(int flag){
                if(flag==0){
                    return getIndexByType(traceIdType);
                }
                if(flag==1){
                    return getIndexByType(nodeIdType);
                }
                if(flag==2){
                    return getIndexByType(valueIdType);
                }
            }

            String getIndexByType(String type){
                String index=parameterAnnotations.get(type);
                if(index==null||index.equals("")){
                    index="-1";
                }
                return index;
            }

            void getICONST(int i) {
                if (i == 0) {
                    mv.visitInsn(ICONST_0);
                } else if (i == 1) {
                    mv.visitInsn(ICONST_1);
                } else if (i == 2) {
                    mv.visitInsn(ICONST_2);
                } else if (i == 3) {
                    mv.visitInsn(ICONST_3);
                } else if (i == 4) {
                    mv.visitInsn(ICONST_4);
                } else if (i == 5) {
                    mv.visitInsn(ICONST_5);
                } else {
                    mv.visitIntInsn(BIPUSH, i);
                }
            }

            void getOpCodeLoad(Type type, int argIndex) {
                if (type.equals(Type.INT_TYPE)) {
                    mv.visitVarInsn(ILOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
                    return;
                }
                if (type.equals(Type.BOOLEAN_TYPE)) {
                    mv.visitVarInsn(ILOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
                    return;
                }
                if (type.equals(Type.CHAR_TYPE)) {
                    mv.visitVarInsn(ILOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
                    return;
                }
                if (type.equals(Type.SHORT_TYPE)) {
                    mv.visitVarInsn(ILOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
                    return;
                }
                if (type.equals(Type.BYTE_TYPE)) {
                    mv.visitVarInsn(ILOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
                    return;
                }

                if (type.equals(Type.LONG_TYPE)) {
                    mv.visitVarInsn(LLOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
                    return;
                }
                if (type.equals(Type.FLOAT_TYPE)) {
                    mv.visitVarInsn(FLOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
                    return;
                }
                if (type.equals(Type.DOUBLE_TYPE)) {
                    mv.visitVarInsn(DLOAD, argIndex);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
                    return;
                }
                mv.visitVarInsn(ALOAD, argIndex);
            }

            String getArgsType() {
                if (argsTypes == null)
                    return "null";

                int iMax = argsTypes.length - 1;
                if (iMax == -1)
                    return "[]";

                StringBuilder b = new StringBuilder();
                b.append('[');
                for (int i = 0; ; i++) {
                    b.append(String.valueOf(argsTypes[i].className));
                    if (i == iMax)
                        return b.append(']').toString();
                    b.append(", ");
                }
            }

            int getlenByType(Type type) {
                if (type.equals(Type.DOUBLE_TYPE)
                        || type.equals(Type.LONG_TYPE)) {
                    return 2
                }
                return 1
            }
        }
    }
}
