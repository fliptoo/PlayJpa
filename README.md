# PlayJPA
Play1 JPA Model for Play2

Jave developer who familiar with JPA Model (Play 1) will like PlayJPA very much.
Please refer [Play!](https://www.playframework.com/documentation/1.3.x/jpa#anamefindingFindingobjectsa) for more detail about the Model. Unfortunely [Explicit Save](https://www.playframework.com/documentation/1.3.x/jpa#anamesaveExplicitsavea) is not implemented as i am still thinking is it a good practice of hacking the normal JPA behaviour.

## Installation
Add plugin declarations into your plugins.sbt file:
```
addSbtPlugin("com.fliptoo" % "sbt-playjpa" % "1.0.0")
```
Add dependency declarations into your build.sbt file:
```
"com.fliptoo" % "playjpa" % "1.0.0"
```
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

Do whatever as you did as Play 1
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