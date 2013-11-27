# Swagger / Spring MVC Integration

[![Build Status](https://travis-ci.org/adrianbk/swagger-springmvc.png?branch=swagger-spec-1.2.0-upgrade)](https://travis-ci.org/adrianbk/swagger-springmvc)


This project provides integration between [Swagger](https://github.com/wordnik/swagger-core) and Spring MVC.


## Rules

## Swagger Annotations
### ApiOperation
- value: (summary) if present and not blank otherwise the java method name is used
- notes: if present and not blank otherwise the java method name is used
- position: used if present otherwise a logical position is provided
- httpMethod: overrides the spring's  @RequestMapping(method = ..) if springs method is not defined an operation appears on the ui for all possible http methods
- consumes/produces: media types are derived from springs @RequestMapping. if @APiOperation.consumes/produces is present these values override


Produces and consumes driven off spring annotation arguments only - swagger ignored

##Development

- Running tests including coverage and checkstyle

-reports: \target\site\jacoco-ut
```
mvn test
mvn test jacoco:check

mvn jacoco:check

mvn test site

local nexus >  mvn deploy
```

-Coverage Help
```
mvn org.jacoco:jacoco-maven-plugin:0.6.3.201306030806:check

-Djacoco.skip=true
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

