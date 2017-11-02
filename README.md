# zookeeper-atomics

[![Codacy Badge](https://api.codacy.com/project/badge/grade/b29214ce1ff64ba88326cb9011ffbc54)](https://www.codacy.com/app/Sammers21/zookeeper-atomics)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/fd054abb2dc94d9eb0993014e051ac58)](https://www.codacy.com/app/Sammers21/zookeeper-atomics?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Sammers21/zookeeper-atomics&amp;utm_campaign=Badge_Coverage)
[![Build Status](https://travis-ci.org/Sammers21/zookeeper-atomics.svg?branch=master)](https://travis-ci.org/Sammers21/zookeeper-atomics)
[![](https://jitpack.io/v/Sammers21/zookeeper-atomics.svg)](https://jitpack.io/#Sammers21/zookeeper-atomics)

# How to use it?

### With Gradle

_Step 1._ Add the JitPack repository to your build file

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

_Step 2_. Add the dependency:

```groovy
dependencies {
    compile 'com.github.Sammers21:zookeeper-atomics:1.0.1'
}
```

### With Maven

_Step 1._ Add the JitPack repository to your build file

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

_Step 2_. Add the dependency:

```xml
<dependency>
    <groupId>com.github.Sammers21</groupId>
    <artifactId>zookeeper-atomics</artifactId>
    <version>1.0.1</version>
</dependency>
```

### Usage

Here is an example:

```java
ZkVariables zkVariables = new ZkVariables(
            "/my_variables",
            "localhost:2181" // ZooKeeper server address
    );
```

_ZkVariables_ is a important object. It allow you to create and change variable in zNodes.

Let's create a variable if such not exist! 

```java
// if variable is not exist then it will be created with "how are you?" value
 ZkAtomicVar hello = zkVariables.getOrCreate("hello", "how are you?");
// otherwise the result would be the same as code above produce
ZkAtomicVar existed = zkVariables.get("hello");
```

You also are welcome to change variables value:

```java
existed.changeValueTo("hello one more time");
```

And even to getting value as a string or a byte array:

```java
String string = existed.getAsByteArr(); 
byte[] bArr = existed.getAsString();
```