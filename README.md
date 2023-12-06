Panama Experiments
==================

Some experiments using Panama. Used as a playground to learn about Panama,
jextract and the like.

Prerequisites
-------------

- Linux x86_64 (or in a VM)
- Java 21 (exactly this version, as we need [Project Panama](https://openjdk.org/projects/panama/) with is a preview
  feature)
- Python 3.8 (or newer)
- clang (for jextract)
- Maven 3.6.3 (or newer, to build the project)

Build
-----
To build the project, make sure you have all prerequisites installed and run:

```shell
mvn clean package
```

Running the example
--------------------
Be sure to run the following in a shell with root privileges that uses JDK 21:

```shell
java --enable-preview -cp target/panama.jar --enable-native-access=ALL-UNNAMED me.bechberger.panama.ErrnoExample
# or to build and run
./run.sh ErrnoExample
```

License
-------
Apache 2.0