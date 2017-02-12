# Play Framework with Scala.js, Binding.scala, reactivemongo, silhouette

WORK IN PROGRESS

This is a simple example application serving as a starting point to developers who would like to use Play with Binding.scala, Silhouette and ReactiveMongo.

Frontend communicates with backend via JSON. Project aims to be a simple modern starting point. It covers following points
* MongoDB database using [reactivemongo](http://reactivemongo.org/)
* User authentication using [silhouette](https://www.silhouette.rocks/)
* [Binding.scala](https://github.com/ThoughtWorksInc/Binding.scala) for JS-frontend

The application contains three directories:
* `server` Play application (server side)
* `client` Scala.js, Binding.scala application (client side)
* `shared` Scala code that you want to share between the server and the client

## Run the application
```shell
$ sbt
> run
$ open http://localhost:9000
```

## Features

The application uses the [sbt-web-scalajs](https://github.com/vmunier/sbt-web-scalajs) sbt plugin and the [scalajs-scripts](https://github.com/vmunier/scalajs-scripts) library.

- Run your application like a regular Play app
  - `compile` triggers the Scala.js fastOptJS command
  - `run` triggers the Scala.js fastOptJS command on page refresh
  - `~compile`, `~run`, continuous compilation is also available
- Compilation errors from the Scala.js projects are also displayed in the browser
- Production archives (e.g. using `stage`, `dist`) contain the optimised javascript
- Source maps
  - Open your browser dev tool to set breakpoints or to see the guilty line of code when an exception is thrown
  - Source Maps is _disabled in production_ by default to prevent your users from seeing the source files. But it can easily be enabled in production too by setting `emitSourceMaps in fullOptJS := true` in the Scala.js projects.


## IDE integration

### IntelliJ

In IntelliJ, open Project wizard, select `Import Project`, choose the root folder and click `OK`.
Select `Import project from external model` option, choose `SBT project` and click `Next`. Select additional import options and click `Finish`.
Make sure you use the IntelliJ Scala Plugin v1.3.3 or higher. There are known issues with prior versions of the plugin.

This is based on starter project provided by [Algomancer](https://github.com/Algomancer/Full-Stack-Scala-Starter/). So cheers to him.
