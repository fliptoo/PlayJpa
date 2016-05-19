# PlayJPA
Play1 JPA Model for Play2

Jave developer who familiar with JPA Model (Play 1) will like PlayJPA very much.
Please refer [Play!](https://www.playframework.com/documentation/1.3.x/jpa#anamefindingFindingobjectsa) for more detail about the Model. Unfortunely [Explicit Save](https://www.playframework.com/documentation/1.3.x/jpa#anamesaveExplicitsavea) is not implemented as i am still thinking is it a good practice of hacking the normal JPA behaviour.

## Installation
Add dependency declarations into your build.sbt file:
```
"com.fliptoo" %% "playjpa" % "1.0-SNAPSHOT"
```
Enable PlayJPA in application.conf
```
play.modules.enabled += "com.fliptoo.play.jpa.Module"
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