# Stellar Sepulchre

A traditional-roguelike written in [libgdx](https://libgdx.com/) using the Scala programming language

```
  ./gradlew desktop:clean
  ./gradlew desktop:dist
  # remove the signature or it won't run
  zip ./desktop/build/libs/desktop-x.y.jar -d META-INF/SIGNINGC.SF
  # windows/linux
  java -jar ./desktop/builds/libs/desktop-x.y.jar
  # macOS
  java -XstartOnFirstThread -jar ./desktop/build/libs/desktop-x.y.jar
```
