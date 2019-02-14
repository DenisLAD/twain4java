## Twain Java Library
Java library for Window x86 и x64. 

## Tested on
- [x] Kyocera Exosys M2040dn (NET, USB)
- [x] HP MFP 1102 (NET)
- [x] Xerox USB
- [x] Cannon USB
- [ ] ...

## Usage
**Compile and install**
Clone project and install in local repository.

```shell
git clone https://github.com/DenisLAD/twain4java.git
cd twain4java
mvn install
```

**Dependency in your project**
```xml
<dependency>
    <groupId>free.lucifer</groupId>
    <artifactId>twain4java</artifactId>
    <version>0.3</version>
</dependency>

```
**Code**
```java
Collection<Source> sources = SourceManager.instance().getSources();
Source source = sources.get(0);
source.setDpi(200);
source.setColor(Source.ColorMode.COLOR);
List<File> files = source.scan();
```
