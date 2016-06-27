package com.fliptoo.playjpa;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JPQL {

    public static long count(String entity) {
        return Long.parseLong(PlayJpa.jpaApi.em().createQuery("select count(*) from " + entity + " e").getSingleResult().toString());
    }

    public static long count(String entity, String query, Object[] params) {
        return Long.parseLong(
                bindParameters(PlayJpa.jpaApi.em().createQuery(
                        createCountQuery(entity, entity, query, params)), params).getSingleResult().toString());
    }

    public static List findAll(String entity) {
        return PlayJpa.jpaApi.em().createQuery("select e from " + entity + " e").getResultList();
    }

    public static <T extends Model> T findById(String entity, Object id) throws Exception {
        return (T) PlayJpa.jpaApi.em().find(PlayJpa.app.classloader().loadClass(entity), id);
    }

    public static <T extends Model> T findOneBy(String entity, String query, Object... params) {
        Query q = PlayJpa.jpaApi.em().createQuery(
                createFindByQuery(entity, entity, query, params));
        List results = bindParameters(q, params).getResultList();
        if (results.size() == 0) {
            return null;
        }
        return (T) results.get(0);
    }

    public static List findBy(String entity, String query, Object[] params) {
        Query q = PlayJpa.jpaApi.em().createQuery(
                createFindByQuery(entity, entity, query, params));
        return bindParameters(q, params).getResultList();
    }

    public static JPAQuery find(String entity, String query, Object[] params) {
        String jpql = createFindByQuery(entity, entity, query, params);
        Query q = PlayJpa.jpaApi.em().createQuery(jpql);
        return new JPAQuery(jpql, bindParameters(q, params));
    }

    public static JPAQuery find(String entity) {
        String jpql = createFindByQuery(entity, entity, null);
        Query q = PlayJpa.jpaApi.em().createQuery(jpql);
        return new JPAQuery(jpql, bindParameters(q));
    }

    public static JPAQuery all(String entity) {
        String jpql = createFindByQuery(entity, entity, null);
        Query q = PlayJpa.jpaApi.em().createQuery(jpql);
        return new JPAQuery(jpql, bindParameters(q));
    }

    public static int delete(String entity, String query, Object[] params) {
        Query q = PlayJpa.jpaApi.em().createQuery(
                createDeleteQuery(entity, entity, query, params));
        return bindParameters(q, params).executeUpdate();
    }

    public static int deleteAll(String entity) {
        Query q = PlayJpa.jpaApi.em().createQuery(
                createDeleteQuery(entity, entity, null));
        return bindParameters(q).executeUpdate();
    }

    public static String createFindByQuery(String entityName, String entityClass, String query, Object... params) {
        if (query == null || query.trim().length() == 0) {
            return "from " + entityName;
        }
        if (query.matches("^by[A-Z].*$")) {
            return "from " + entityName + " where " + findByToJPQL(query);
        }
        if (query.trim().toLowerCase().startsWith("select ")) {
            return query;
        }
        if (query.trim().toLowerCase().startsWith("from ")) {
            return query;
        }
        if (query.trim().toLowerCase().startsWith("order by ")) {
            return "from " + entityName + " " + query;
        }
        if (query.trim().indexOf(' ') == -1 && query.trim().indexOf('=') == -1 && params != null && params.length == 1) {
            query += " = ?1";
        }
        if (query.trim().indexOf(' ') == -1 && query.trim().indexOf('=') == -1 && params == null) {
            query += " = null";
        }
        return "from " + entityName + " where " + query;
    }

    public static String createDeleteQuery(String entityName, String entityClass, String query, Object... params) {
        if (query == null) {
            return "delete from " + entityName;
        }
        if (query.trim().toLowerCase().startsWith("delete ")) {
            return query;
        }
        if (query.trim().toLowerCase().startsWith("from ")) {
            return "delete " + query;
        }
        if (query.trim().indexOf(' ') == -1 && query.trim().indexOf('=') == -1 && params != null && params.length == 1) {
            query += " = ?1";
        }
        if (query.trim().indexOf(' ') == -1 && query.trim().indexOf('=') == -1 && params == null) {
            query += " = null";
        }
        return "delete from " + entityName + " where " + query;
    }

    public static String createCountQuery(String entityName, String entityClass, String query, Object... params) {
        if (query.trim().toLowerCase().startsWith("select ")) {
            return query;
        }
        if (query.matches("^by[A-Z].*$")) {
            return "select count(*) from " + entityName + " where " + findByToJPQL(query);
        }
        if (query.trim().toLowerCase().startsWith("from ")) {
            return "select count(*) " + query;
        }
        if (query.trim().toLowerCase().startsWith("order by ")) {
            return "select count(*) from " + entityName;
        }
        if (query.trim().indexOf(' ') == -1 && query.trim().indexOf('=') == -1 && params != null && params.length == 1) {
            query += " = ?1";
        }
        if (query.trim().indexOf(' ') == -1 && query.trim().indexOf('=') == -1 && params == null) {
            query += " = null";
        }
        if (query.trim().length() == 0) {
            return "select count(*) from " + entityName;
        }
        return "select count(*) from " + entityName + " e where " + query;
    }

    public static Query bindParameters(Query q, Object... params) {
        if (params == null) {
            return q;
        }
        if (params.length == 1 && params[0] instanceof Map) {
            return bindParameters(q, (Map<String, Object>) params[0]);
        }
        for (int i = 0; i < params.length; i++) {
            q.setParameter(i + 1, params[i]);
        }
        return q;
    }

    public static Query bindParameters(Query q, Map<String, Object> params) {
        if (params == null) {
            return q;
        }
        for (String key : params.keySet()) {
            q.setParameter(key, params.get(key));
        }
        return q;
    }

    public static String findByToJPQL(String findBy) {
        findBy = findBy.substring(2);
        StringBuilder jpql = new StringBuilder();
        String subRequest;
        if (findBy.contains("OrderBy"))
            subRequest = findBy.split("OrderBy")[0];
        else subRequest = findBy;

        String[] requests = subRequest.split("[\\(||//)]");
        String op = null;
        int index = 1;
        for (String request : requests) {
            if (request.endsWith("Or")) {
                op = "OR";
                request = request.substring(0, request.length()-2);
            } else if (request.endsWith("And")) {
                op = "AND";
                request = request.substring(0, request.length()-3);
            } else if (op != null && op.length() > 0) {
                jpql.append(" ").append(op).append("(");
                op = "CLOSED";
            }

            String[] parts = request.split("And");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (part.length() > 0) {
                    if (part.contains("Or")) {
                        String[] orParts = part.split("Or");
                        jpql.append("(");
                        for (int j = 0; j < orParts.length; j++) {
                            String orPart = orParts[j];
                            if (j > 0) jpql.append(" OR ");
                            index = translate(jpql, index, orPart);
                        }
                        jpql.append(")");
                    } else index = translate(jpql, index, part);
                    if (i < parts.length - 1) {
                        jpql.append(" AND ");
                    }
                } else jpql.append(" AND ");
            }

            if (op != null && op.equalsIgnoreCase("CLOSED")) {
                jpql.append(")");
                op = null;
            }
        }

        // ORDER BY clause
        if (findBy.contains("OrderBy")) {
            jpql.append(" ORDER BY ");
            String orderQuery = findBy.split("OrderBy")[1];
            String[] parts = orderQuery.split("And");
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                String orderProp;
                if (part.endsWith("Desc"))
                    orderProp = extractProp(part, "Desc") + " DESC";
                else orderProp = part.toLowerCase();
                if (i > 0)
                    jpql.append(", ");
                jpql.append(orderProp);
            }
        }
        return jpql.toString();
    }

    private static int translate(StringBuilder jpql, int index, String part) {
        if (part.endsWith("NotEqual")) {
            String prop = extractProp(part, "NotEqual");
            jpql.append(prop).append(" <> ?").append(index++);
        } else if (part.endsWith("Equal")) {
            String prop = extractProp(part, "Equal");
            jpql.append(prop).append(" = ?").append(index++);
        } else if (part.endsWith("IsNotNull")) {
            String prop = extractProp(part, "IsNotNull");
            jpql.append(prop).append(" is not null");
        } else if (part.endsWith("IsNull")) {
            String prop = extractProp(part, "IsNull");
            jpql.append(prop).append(" is null");
        } else if (part.endsWith("LessThan")) {
            String prop = extractProp(part, "LessThan");
            jpql.append(prop).append(" < ?").append(index++);
        } else if (part.endsWith("LessThanEquals")) {
            String prop = extractProp(part, "LessThanEquals");
            jpql.append(prop).append(" <= ?").append(index++);
        } else if (part.endsWith("GreaterThan")) {
            String prop = extractProp(part, "GreaterThan");
            jpql.append(prop).append(" > ?").append(index++);
        } else if (part.endsWith("GreaterThanEquals")) {
            String prop = extractProp(part, "GreaterThanEquals");
            jpql.append(prop).append(" >= ?").append(index++);
        } else if (part.endsWith("Contains")) {
            String prop = extractProp(part, "Contains");
            jpql.append("?").append(index++).append(" in elements(").append(prop).append(")");
        } else if (part.endsWith("In")) {
            String prop = extractProp(part, "In");
            jpql.append(prop).append(" in ?").append(index++);
        } else if (part.endsWith("Between")) {
            String prop = extractProp(part, "Between");
            jpql.append(prop).append(" < ?").append(index++).append(" AND ").append(prop).append(" > ?").append(index++);
        } else if (part.endsWith("Like")) {
            String prop = extractProp(part, "Like");
            if (isHSQL()) {
                jpql.append("LCASE(").append(prop).append(") like ?").append(index++);
            } else {
                jpql.append("LOWER(").append(prop).append(") like ?").append(index++);
            }
        } else if (part.endsWith("Ilike")) {
            String prop = extractProp(part, "Ilike");
            if (isHSQL()) {
                jpql.append("LCASE(").append(prop).append(") like LCASE(?").append(index++).append(")");
            } else {
                jpql.append("LOWER(").append(prop).append(") like LOWER(?").append(index++).append(")");
            }
        } else if (part.endsWith("Elike")) {
            String prop = extractProp(part, "Elike");
            jpql.append(prop).append(" like ?").append(index++);
        } else {
            String prop = extractProp(part, "");
            jpql.append(prop).append(" = ?").append(index++);
        }
        return index;
    }

    private static boolean isHSQL() {
//        String db = Play.configuration.getProperty("db");
//        return ("mem".equals(db) || "fs".equals(db) || "org.hsqldb.jdbcDriver".equals(Play.configuration.getProperty("db.driver")));
        return false;
    }

    protected static String extractProp(String part, String end) {
        String prop = part.substring(0, part.length() - end.length());
        prop = (prop.charAt(0) + "").toLowerCase() + prop.substring(1);
        return prop;
    }

    public static class JPAQuery {

        public Query query;
        public String sq;

        public JPAQuery(String sq, Query query) {
            this.query = query;
            this.sq = sq;
        }

        public JPAQuery(Query query) {
            this.query = query;
            this.sq = query.toString();
        }

        public <T> T first() {
            try {
                List<T> results = query.setMaxResults(1).getResultList();
                if (results.isEmpty()) {
                    return null;
                }
                return results.get(0);
            } catch (Exception e) {
                throw new JPAQueryException("Error while executing query <strong>" + sq + "</strong>", JPAQueryException.findBestCause(e));
            }
        }

        /**
         * Bind a JPQL named parameter to the current query.
         * Careful, this will also bind count results. This means that Integer get transformed into long
         *  so hibernate can do the right thing. Use the setParameter if you just want to set parameters.
         */
        public JPAQuery bind(String name, Object param) {
            if (param.getClass().isArray()) {
                param = Arrays.asList((Object[]) param);
            }
            if (param instanceof Integer) {
                param = ((Integer) param).longValue();
            }
            query.setParameter(name, param);
            return this;
        }

        /**
         * Set a named parameter for this query.
         **/
        public JPAQuery setParameter(String name, Object param) {
            query.setParameter(name, param);
            return this;
        }

        /**
         * Retrieve all results of the query
         * @return A list of entities
         */
        public <T> List<T> fetch() {
            try {
                return query.getResultList();
            } catch (Exception e) {
                throw new JPAQueryException("Error while executing query <strong>" + sq + "</strong>", JPAQueryException.findBestCause(e));
            }
        }

        /**
         * Retrieve results of the query
         * @param max Max results to fetch
         * @return A list of entities
         */
        public <T> List<T> fetch(int max) {
            try {
                query.setMaxResults(max);
                return query.getResultList();
            } catch (Exception e) {
                throw new JPAQueryException("Error while executing query <strong>" + sq + "</strong>", JPAQueryException.findBestCause(e));
            }
        }

        /**
         * Set the position to start
         * @param position Position of the first element
         * @return A new query
         */
        public <T> JPAQuery from(int position) {
            query.setFirstResult(position);
            return this;
        }

        /**
         * Retrieve a page of result
         * @param page Page number (start at 1)
         * @param length (page length)
         * @return a list of entities
         */
        public <T> List fetch(int page, int length) {
            if (page < 1) {
                page = 1;
            }
            query.setFirstResult((page - 1) * length);
            query.setMaxResults(length);
            try {
                return query.getResultList();
            } catch (Exception e) {
                throw new JPAQueryException("Error while executing query <strong>" + sq + "</strong>", JPAQueryException.findBestCause(e));
            }
        }
    }

    public static class JPAQueryException extends RuntimeException {

        public JPAQueryException(String message, Throwable e) {
            super(message + ": " + e.getMessage(), e);
        }

        public static Throwable findBestCause(Throwable e) {
            Throwable best = e;
            Throwable cause = e;
            int it = 0;
            while ((cause = cause.getCause()) != null && it++ < 10) {
                if (cause instanceof ClassCastException) {
                    best = cause;
                    break;
                }
                if (cause instanceof SQLException) {
                    best = cause;
                    break;
                }
            }
            return best;
		}
	}
}