# Lang24 Compiler

This repository contains the implementation of a custom compiler for the programming language Lang24, developed according to the principles outlined in the book "Modern Compiler Implementation in Java" by Jens Palsberg and Andrew Appel.

The project demonstrates the complete compilation process, divided into various phases, each designed to handle a specific aspect of compilation. Below is a detailed description of the implemented phases:

## Lang24
See code example of bubblesort:

```
putint(i:int):void
putchar(c:char):void

seed:int
random():int={seed=(seed*281474976710656)%44485709377909;return seed;}

abs(i:int):int= if i<0 then return -i; else return i;

tab:[100]int

bubblesort(nums:^[100]int,^cmps:int):void=
  {
    i=100-1;
    while i>=0:{
      j=0;
      while j<i:{
        if nums^[j]>nums^[j+1] then{
          t=nums^[j];nums^[j]=nums^[j+1];nums^[j+1]=t;
        }
        cmps=cmps+1;
        j=j+1;
      }
      i=i-1;
    }
    return none;
  }
  {i:int j:int t:int}

main():int=
  {
    seed=2024;
    i=0;
    while i<100:{
        tab[i]=abs(random())%50;
        i=i+1;
    }
    i=0;while i<100:{putint(tab[i]);if i<99 then putchar(',');i=i+1;}putchar('\n');
    cmps=0;bubblesort(^tab,cmps);
    i=0;while i<100:{putint(tab[i]);if i<99 then putchar(',');i=i+1;}putchar('\n');
    return cmps;
  }
  {i:int cmps:int}
```

## Phases of the Lang24 Compiler

1. **Lexical Analysis (Lexan)**
   - Converts the source code into a stream of tokens, which are the smallest units of meaning (e.g., keywords, identifiers, symbols).
   - Identifies and classifies tokens for use in subsequent phases.

2. **Syntax Analysis (Synan)**
   - Constructs a syntax tree (parse tree) from the stream of tokens.
   - Ensures that the code adheres to the grammatical rules of Lang24.

3. **Abstract Syntax Tree Construction (Abstr)**
   - Constructs an abstract syntax tree (AST) that provides a simplified, structured view of the source code.
   - Used as a basis for later phases like semantic analysis and intermediate code generation.

4. **Semantic Analysis (Seman)**
   - Analyzes the syntax tree to enforce semantic rules, such as type checking, scope resolution, and function declarations.
   - Detects errors that cannot be caught during syntax analysis.

5. **Intermediate Code Generation (IMCGen)**
   - Translates the syntax tree into an intermediate representation (IR), which is a simplified, language-neutral version of the program.

6. **Intermediate Code Linearization (IMCLin)**
   - Converts the IR into a linear sequence of instructions, making it easier to analyze and optimize.

7. **Liveness Analysis (LiveAn)**
   - Analyzes variable usage to determine which variables are "live" (in use) at each point in the program.
   - Essential for optimizing memory and register allocation.

8. **Register Allocation (RegAll)**
   - Maps variables to machine registers to minimize memory access and improve performance.

9. **Memory Management (Memory)**
   - Handles memory allocation for variables, arrays, and other data structures during runtime.
   - Ensures efficient usage of memory resources.

10. **Assembly Code Generation (ASMGen)**
    - Converts the intermediate representation into low-level assembly code for the target machine.
    - Optimizes the generated code for performance.

11. **Finalization Phase (End)**
    - Marks the completion of the compilation process.
    - Handles any necessary clean-up and ensures the compiler is ready for execution or deployment.

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
