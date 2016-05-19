package com.fliptoo.playjpa;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;

public abstract class Enhancer {

    public abstract void enhance(CtClass cc, String Entity, String Model, String JPAQuery, String JPQL);

    protected static void makeMethod(String method, CtClass cc) {
        try {
            CtMethod cm = CtMethod.make(method, cc);
            cc.addMethod(cm);
        }
        catch (DuplicateMemberException ignored) {}
        catch (Exception e) {e.printStackTrace();}
    }

    protected static void createAnnotation(AnnotationsAttribute attribute, Class<? extends java.lang.annotation.Annotation> annotationType, String memberKey, MemberValue memberValue) {
        Annotation annotation = new Annotation(annotationType.getName(), attribute.getConstPool());
        annotation.addMemberValue(memberKey, memberValue);
        attribute.addAnnotation(annotation);
    }

    protected static boolean hasAnnotation(CtClass ctClass, Class<? extends java.lang.annotation.Annotation> annotationType) throws ClassNotFoundException {
        return getAnnotations(ctClass).getAnnotation(annotationType.getName()) != null;
    }

    protected static AnnotationsAttribute getAnnotations(CtClass ctClass) {
        AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        if (annotationsAttribute == null) {
            annotationsAttribute = new AnnotationsAttribute(ctClass.getClassFile().getConstPool(), AnnotationsAttribute.visibleTag);
            ctClass.getClassFile().addAttribute(annotationsAttribute);
        }
        return annotationsAttribute;
    }
}
