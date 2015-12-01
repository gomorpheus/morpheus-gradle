# Morpheus Gradle Plugin

The Morpheus Gradle plugin provides tasks for deployment automation to the Morpheus Cloud Management platform. Deploy any of your applications to a morpheus on or off-premise setup.

## Configuration

Simply add the plugin to your project and configure it using the `morpheus` configure block

```groovy
//Example build.gradle file
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
    mavenCentral()
  }
  dependencies {
    classpath "com.bertramlabs.plugins:morpheus-gradle:0.1.0"
  }
}
apply plugin: 'com.bertramlabs.morpheus'

morpheus {
    morpheusUser = 'blah'
    morpheusPassword = 'dafsadfsdf'
    applianceUrl = 'https://v2.gomorpheus.com'
    instance = 'My Instance Name'

    from "build/libs/*"
}
```

A `from` command can be either a path to files to upload to the final deployment archive or they can be a Closure with more advanced options:

```groovy
morpheus {
    from {
        resolverPath = 'build/libs/*'
        includePatterns = ['**/*.war']
        excludePatterns = ['*.jar']
        //destinationPath = '' Useful for nesting into a subfolder of the deploy archive
    }
}
```


## Usage

Simply execute the task:

```shell
./gradlew morpheusDeploy
```

## Resources

* [Java APIDoc](http://gomorpheus.github.io/morpheus-java-sdk)
* [Web APIDoc](http://bertramdev.github.io/morpheus-apidoc/)
* [Morpheus Website](https://www.gomorpheus.com)
