package com.fliptoo.playjpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StringType;
import org.hibernate.usertype.UserType;

import play.Application;

import javax.inject.Inject;

public class Blob implements UserType {

    @Inject
    Application app;

    private String UUID;
    private String type;
    private String folder;
    private File file;

    public Blob() {
        this(null);
    }

    public Blob(String folder) {
        this.folder = folder;
    }

    private Blob(String UUID, String type, String folder) {
        this.UUID = UUID;
        this.type = type;
        this.folder = folder;
    }

    public InputStream get() {
        if (exists()) {
            try {
                return new FileInputStream(getFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public void set(InputStream is, String type) {
        this.UUID = java.util.UUID.randomUUID().toString();
        this.type = type;
        try {
            Files.copy(is, Paths.get(getFile().toURI()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long length() {
        return getFile().length();
    }

    public String type() {
        return type;
    }

    public boolean exists() {
        return UUID != null && getFile().exists();
    }

    public File getFile() {
        if (file == null) {
            file = new File(getStore(folder), UUID);
        }
        return file;
    }

    public String getUUID() {
        return UUID;
    }

    public int[] sqlTypes() {
        return new int[]{Types.VARCHAR};
    }

    public Class returnedClass() {
        return Blob.class;
    }

    private static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public boolean equals(Object o, Object o1) throws HibernateException {
        if (o instanceof Blob && o1 instanceof Blob) {
            return equal(((Blob) o).UUID, ((Blob) o1).UUID)
                    && equal(((Blob) o).type, ((Blob) o1).type);
        }
        return equal(o, o1);
    }

    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor implementor, Object o) throws HibernateException, SQLException {
        String val = (String) StringType.INSTANCE.get(resultSet, names[0], implementor);

        if (val == null || val.length() == 0 || !val.contains("|")) {
            return new Blob(null);
        }

        String vals[] = val.split("[|]");
        String UUID = vals[0];
        String type = vals[1];
        String folder = null;
        if (vals.length > 2 && vals[2].length() > 0)
            folder = vals[2];
        return new Blob(UUID, type, folder);
    }

    @Override
    public void nullSafeSet(PreparedStatement ps, Object o, int i, SessionImplementor sessionImplementor) throws HibernateException, SQLException {
        if (o != null) {
            Blob bin = (Blob) o;
            if (bin.folder != null && bin.folder.length() > 0)
                ps.setString(i, bin.UUID + "|" + bin.type + "|" + bin.folder);
            else
                ps.setString(i, bin.UUID + "|" + bin.type);
        } else {
            ps.setNull(i, Types.VARCHAR);
        }
    }

    public Object deepCopy(Object o) throws HibernateException {
        if (o == null) {
            return null;
        }
        return new Blob(((Blob) o).UUID, ((Blob) o).type, ((Blob) o).folder);
    }

    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object o) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object assemble(Serializable serializable, Object o) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object replace(Object o, Object o1, Object o2) throws HibernateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public File getStore(String folder) {
        String name = app.configuration().getString("playJPA.attachments.path");
        if (StringUtils.isEmpty(name)) name = "data/attachments";
        if (folder != null && folder.length() > 0)
            name += File.separator + folder;
        File store;
        if (new File(name).isAbsolute()) {
            store = new File(name);
        } else {
            store = app.getFile(name);
        }
        if (!store.exists()) {
            store.mkdirs();
        }
		return store;
	}
}
