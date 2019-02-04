# Spring Router

Allow write HTTP mapping in external text file (rails/play like approach).

The file looks like (any file name and extension, by default it is `router.txt` at the root of classpath):

```
# The example from https://guides.rubyonrails.org/routing.html#controller-namespaces-and-routing
# see https://github.com/mhewedy/spring-router


GET             /admin/articles	                    ArticleController#index
POST            /admin/articles	                    ArticleController#create(Article)
GET             /admin/articles/{id}	            ArticleController#show(Long)
PUT             /admin/articles/{id}	            ArticleController#update(Long, Article)
DELETE          /admin/articles/{id}	            ArticleController#destroy(Long)



```

You need to configure `@EnableRouter` annotation

```java
@EnableRouter(controllerPackage = "com.example.demo.controllers")
public class MyConfig {

}
```

See https://github.com/mhewedy/spring-router-example
