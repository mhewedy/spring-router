# Spring Router

Allow write HTTP mapping in external text file (rails/play like approach).

The file looks like (any file name and extension):

```
# this is my router file

GET		/users					UserController#list
GET		/users/{id}				UserController#get(Long)
PUT		/users/{id}				UserController#update(Long, User)

```

You can need to define one Bean (BPP):

```
@Bean
public RouterBeanPostProcessor routerBeanPostProcessor() {
    return new RouterBeanPostProcessor(new ClassPathResource("route1.txt"),
        "controllers");
}
```

see the test cases for complete example. 