[![pipeline status](http://gitlab.dev.smart-consulting.ru/utils/JavaTWAIN/badges/master/pipeline.svg)](http://gitlab.dev.smart-consulting.ru/utils/JavaTWAIN/commits/master)

## Twain Java Library
Библиотека для Java под Window x86 и x64. 

## Протестировано
- [x] Kyocera Exosys M2040dn (NET, USB)
- [x] HP MFP 1102 (NET)
- [ ] Xerox
- [ ] Cannon
- [ ] ...

## Использование

**Репозиторий**
```xml
<repository>
    <id>atcs-repo</id>
    <name>ATS Releases</name>
    <url>http://nexus.dev.smart-consulting.ru/nexus/content/repositories/releases/</url>
</repository>
```
**Зависимость**
```xml
<dependency>
    <groupId>com.ats</groupId>
    <artifactId>jtwain</artifactId>
    <version>0.3</version>
</dependency>

```
**Код**
```java
Collection<Source> sources = SourceManager.instance().getSources();
Source source = sources.get(0);
source.setDpi(200);
source.setColor(Source.ColorMode.COLOR);
List<File> files = source.scan();
```

## ССылки

* Library: [jtwain-0.3.jar][library]
* Sources: [jtwain-0.3-sources.jar][sources]
* JavaDoc: [jtwain-0.3-javadoc.jar][javadoc]

[library]: http://nexus.dev.smart-consulting.ru//nexus/content/repositories/releases/com/ats/jtwain/0.3/jtwain-0.3.jar
[sources]: http://nexus.dev.smart-consulting.ru//nexus/content/repositories/releases/com/ats/jtwain/0.3/jtwain-0.3-sources.jar
[javadoc]: http://nexus.dev.smart-consulting.ru//nexus/content/repositories/releases/com/ats/jtwain/0.3/jtwain-0.3-javadoc.jar