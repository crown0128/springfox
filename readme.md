# Swagger / Spring MVC Integration

[![Build Status](https://travis-ci.org/adrianbk/swagger-springmvc.png?branch=swagger-spec-1.2.0-upgrade)](https://travis-ci.org/adrianbk/swagger-springmvc)
This project provides integration between [Swagger](https://github.com/wordnik/swagger-core) and Spring MVC.

Spring beans annotated with `@Controller` are detected and parsed for documentation.

## Getting started
The project is available from maven:
```xml
	<dependency>
  		<groupId>com.mangofactory</groupId>
  		<artifactId>swagger-springmvc</artifactId>
  		<version>0.6.5</version>
	</dependency>
```	

## Supported annotations
Currently, a subset of Swagger annotations are supported.  Support will improve over the coming releases.

All `@Controller` classes are parsed, and methods annotated with `@RequestMapping` are generated.
  
Additionally, `@Api` at the class level, and `@ApiOperation` at the method level are both supported.
			
## Getting started

### For XML config

To wire up support, add the following into your spring context file (Configuration based on the [example project](https://github.com/martypitt/swagger-springmvc-example)):

```xml
    <!-- to specify swagger properties
    	documentation.services.basePath=http://localhost:8080/swagger-springmvc-test
	documentation.services.version=1.0
    --> 
    <context:property-placeholder location="classpath:swagger.properties" /> 
    <!-- pulls in the Controllers to document -->
    <context:component-scan base-package="com.mangofactory.swagger.springmvc.example" />
    <!-- Configuration Bean -->
    <bean id="documentationConfig" class="com.mangofactory.swagger.configuration.DocumentationConfig"/>
    <!-- Custom extensibility module (bean) Has override methods to customize the document generation-->
    <bean id="extensibilityModule" class="com.mangofactory.swagger.springmvc.example.config.ExampleExtensibilityModule" />

```

### For Java config
Create if required a @Configuration Bean to import the DocumentationConfig bean defined in this library it [like here](https://github.com/martypitt/swagger-springmvc-example/blob/master/src/main/java/com/mangofactory/swagger/springmvc/example/config/CustomDocumentationConfig.java).

Secondly to pull in the properties, define a bean in yr custom config (note that it needs to implement ServletContextAware to get access to servlet properties)

```java
    @Bean
    public PropertyPlaceholderConfigurer swaggerProperties() throws UnknownHostException {

        // Swagger expects these to property values to be replaced. We don't want to propagate these to consumers of
        // this configuration, so we derive reasonable defaults here and configure the properties programmatically.
        Properties properties = new Properties();
        properties.setProperty("documentation.services.basePath", servletContext.getContextPath());
        // this property will be overridden at runtime, so the value here doesn't matter
        properties.setProperty("documentation.services.version", "REPLACE-ME");

        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(properties);
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }
```
**Importantly** everything that swagger needs is defined in the DocumentationConfig bean, *you just need to import it*. And it will guide (scream actually) through satisfying the unsatisfied dependencies when the application starts.

The `basePath` property is external-facing url the maps to your SpringMVC dispatcher servlet.

This creates a controller at `/api-docs` from this uri, which serves swagger's raw documentation in JSON format.  (eg
., In the above example,  `http://www.mydomain.com/swagger-springmvc-example/api-docs`)

## Deviations from default Swagger API
Some deviations from the default Swagger API exist.  Wherever possible, these are inteded to be implemented as-well-as the default Swagger implementation, rather than as a replacement.

The overarching goal is to support generation of the Swagger JSON, with minimal intrusion to the code itself.

### Errors
Declaration of errors supports the standard Swagger `@ApiErrors` and `@ApiError` annotations.
In addition, there are `com.mangofactory.swagger` implementations of these that reduce the amount of per-method code (notably, at the cost of some flexibility)

`@ApiError` is now supported at the exception class level, as shown here:

```java
    @ApiError(code=302,reason="Malformed request")
    public class BadRequestException {}
```

This allows errors to be declared as follows:

```java
	@ApiErrors({NotFoundException.class,BadRequestException.class})
	public void someApiMethod() {};
```

or, simply using a `throws` declaration:

	public void someApiMethod() throws NotFoundException, BadRequestException {};

## Example project
An example of Swaggers PetStore in Spring MVC is available [here](https://github.com/martypitt/swagger-springmvc-example)

## TODO:
- Handle the case where RequestMapping might have wildcards

## Credits

A big thank you to all the folks who are using this library and contributing via code/issues/discussions. 

In no partiular order thank you to the following for sending in PRs. Its not sustainable without community support!
- [@mbazos](https://github.com/mbazos)
- [@thomaschristensen](https://github.com/thomaschristensen)
- [@pawelrychlik](https://github.com/pawelrychlik)
- [@frisco82](https://github.com/frisco82)
- [@adrianbk](https://github.com/adrianbk)
- [@mwhig](https://github.com/mwhig)
- [@alexeytokar](https://github.com/alexeytokar)
- [@williamvoor](https://github.com/williamvoor)
- [@obernal](https://github.com/obernal)


## Rules

Produces and consumes driven off spring annotation arguments only - swagger ignored

##Development

- Running tests including coverage and checkstyle

-reports: \target\site\jacoco-ut
```
mvn test
mvn test jacoco:check

mvn jacoco:check

mvn test site

local nexus: mvn deploy
```

-Coverage Help
```
mvn org.jacoco:jacoco-maven-plugin:0.6.3.201306030806:check
```

License
-------

Copyright 2012 Marty Pitt - [@martypitt](https://github.com/martypitt), Dilip Krishnan - [@dilipkrish](https://github.com/dilipkrish)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

