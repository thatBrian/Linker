# Two-Pass Linker
### This project shows how to resolve different memory addresses. This was build specifically for a word addressable machine that has a memory of 200 words, each consisting of 4 decimal digits. The first digit is the opcode, the remaining three are the address fields.

## How to compile?
__javac Oslinker.java__

## How does it work?
This program takes in 1 argument:
1. the input file

This program deals with four different addresses:
1. An immediate operand, which the program does not change.
2. An absolute address, which the program does not change.
3. A relative address, which the program relocates.
4. A external address, which the program resolves.


## Example
Use any of the address files in the given input files folder or make your own.

__java Oslinker input-1.txt__
