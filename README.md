# PlayJpa
Play1 JPA Model for Play2

Java developer who familiar with JPA Model (Play1) will like PlayJpa very much.
Please refer [Play!](https://www.playframework.com/documentation/1.3.x/jpa#anamefindingFindingobjectsa) for more detail about the Model. Unfortunately [Explicit Save](https://www.playframework.com/documentation/1.3.x/jpa#anamesaveExplicitsavea) is not implemented yet.

## Installation
plugins.sbt
```
addSbtPlugin("com.fliptoo" % "sbt-playjpa" % "1.0.3")
```
build.sbt
```
"com.fliptoo" % "playjpa" % "1.0.3"
```
conf/application.conf
```
play.modules.enabled += "com.fliptoo.playjpa.Module"
```
## Improvement
Whenever declare a method with single parameter, SBT will throw compilation error during PlayReload. At this moment i can't find any solution, so i have to declare Model.findById(Object id, Object... ignored) instead of Model.findById(Object id)

## Quick Start

Extend your JPA entity with the Model class

```
@Entity
public class User extends Model {

    @Id
    @GeneratedValue
    public Long id;

    public String name;

}
```

Do whatever as you did as Play1
```
public class Application extends Controller {

    @Transactional
    public Result index() {
        User user = User.find("byName", "fliptoo").first();
        return ok(index.render("I am " + user.name));
    }

}
```

## Extra Enhancer

Simply create a class with suffix `_Enhancer` and extend `com.fliptoo.playjpa.Enhancer` to include extra enhancer for your custom Model.
```
public class CustomModel extends Model {

    public static void searchAll() {
        throw new UnsupportedOperationException("Please annotate your JPA model with @javax.persistence.Entity annotation.");
    }
}

public class Custom_Enhancer extends Enhancer {

    @Override
    public void enhance(CtClass cc, String Entity, String Model, String JPAQuery, String JPQL) {
        makeMethod("public static void searchAll() { Logger.info(\"Search All...\"); }", cc);
    }
}
```
