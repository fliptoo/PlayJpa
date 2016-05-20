package com.fliptoo.playjpa;

import javassist.ClassPool;
import javassist.CtClass;

import javax.persistence.Entity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fliptoo.playjpa.Enhancer.hasAnnotation;

public class PlayJpa {

    private static class Enhancers {

        private List<Enhancer> enhancers = new ArrayList<>();

        Enhancers() {
            enhancers.add(new Enhancer() {

                @Override
                public void enhance(CtClass cc, String Entity, String Model, String JPAQuery, String JPQL) {
                    makeMethod("public static long count() { return " + JPQL + ".count(\"" + Entity + "\"); }", cc);
                    makeMethod("public static long count(String query, Object[] params) { return  " + JPQL + ".count(\"" + Entity + "\", query, params); }", cc);
                    makeMethod("public static java.util.List findAll() { return  " + JPQL + ".findAll(\"" + Entity + "\"); }", cc);
                    makeMethod("public static " + Model + " findById(Object id) { return  " + JPQL + ".findById(\"" + Entity + "\", id); }", cc);
                    makeMethod("public static " + JPAQuery + " find(String query, Object[] params) { return  " + JPQL + ".find(\"" + Entity + "\", query, params); }", cc);
                    makeMethod("public static " + JPAQuery + " find() { return  " + JPQL + ".find(\"" + Entity + "\"); }", cc);
                    makeMethod("public static " + JPAQuery + " all() { return  " + JPQL + ".all(\"" + Entity + "\"); }", cc);
                    makeMethod("public static int delete(String query, Object[] params) { return  " + JPQL + ".delete(\"" + Entity + "\", query, params); }", cc);
                    makeMethod("public static int deleteAll() { return  " + JPQL + ".deleteAll(\"" + Entity + "\"); }", cc);
                    makeMethod("public static " + Model + " findOneBy(String query, Object[] params) { return  " + JPQL + ".findOneBy(\"" + Entity + "\", query, params); }", cc);
                }
            });
        }

        public List<Enhancer> get() {
            return enhancers;
        }
    }

    private static class ClassPathWithFile {

        public final String classPath;
        public final File file;

        public ClassPathWithFile(String classPath, File file) {
            this.classPath = classPath;
            this.file = file;
        }
    }

    private static final String Model = com.fliptoo.playjpa.Model.class.getCanonicalName();
    private static final String JPAQuery = com.fliptoo.playjpa.JPQL.JPAQuery.class.getCanonicalName();
    private static final String JPQL = JPQL.class.getCanonicalName();
    private static final Enhancers enhancers = new Enhancers();
    private static List<ClassPathWithFile> entities = new ArrayList<>();

    public static void enhance() {
        entities.forEach(entity -> {
            try {
                ClassPool cp = new ClassPool();
                cp.appendSystemPath();
                cp.appendPathList(entity.classPath);
                try (FileInputStream is = new FileInputStream(entity.file)) {
                    CtClass cc = cp.makeClass(is);
                    enhancers.get().forEach(enhancer
                            -> enhancer.enhance(cc, cc.getName(), Model, JPAQuery, JPQL));
                    try (FileOutputStream os = new FileOutputStream(entity.file)) {
                        os.write(cc.toBytecode());
                    }
                }
            } catch (Exception e) {e.printStackTrace();}
        });
    }

    public static boolean scan(String classPath, File classFile) {
        try {
            ClassPool cp = new ClassPool();
            cp.appendSystemPath();
            cp.appendPathList(classPath);
            CtClass model = cp.get(Model);
            CtClass enhancer = cp.get(Enhancer.class.getCanonicalName());
            try (FileInputStream is = new FileInputStream(classFile)) {
                CtClass cc = cp.makeClass(is);
                if (cc.subtypeOf(enhancer)) {
                    cc = cp.getAndRename(cc.getName(), cc.getName() + UUID.randomUUID().toString());
                    enhancers.get().add((Enhancer) cc.toClass(Enhancer.class.getClassLoader(), null).newInstance());
                    return true;
                }
                if (!hasAnnotation(cc, Entity.class)) return false;
                if (!cc.subtypeOf(model)) return false;
                entities.add(new ClassPathWithFile(classPath, classFile));
                return true;
            }
        } catch (Exception e) {e.printStackTrace();}
        return true;
    }
}

