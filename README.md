# Lang24 Compiler

A project for university course "Compilers"

## Lang24
See [language specification](lang24_specs.md).

## Running

~~Gradle config is to-be-done. Currently only runnable on UNIX systems.~~
To build the jar file, run
```bash
./gradlew build
```
The output will show up in `build/libs` folder.

Testing task is still to-be-done. Rn you can build the jar and run it manually via
```bash
java -jar build/libs/lang24compiler.jar --src-file-name=prg/test.lang24 --xsl=../lib/xsl/ --target-phase=lexan --logged-phase=all
```
This will generate the xml files.


### Compiling with make

1. Download [antlr](https://www.antlr.org/download/antlr-4.13.1-complete.jar) to project `lib/` folder.
2. Execute following command in project root directory:
```bash
make all
```

### Run

1. Write a file `filename.lang24` in `prg/` directory.
2. Execute following command in `prg/` directory:
```bash
make TARGETPHASE=lexan filename
```
where `filename` is the name of the the file, excluding file extension.
