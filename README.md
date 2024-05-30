# VMTranslator

This repository contains a Java implementation of the VMTranslator for the NAND to Tetris course. It fully implements the functionality described in The Elements of Computing Systems by Nisan and Schocken, MIT Press, chapters 7 and 8. The translator passes all unit tests and the online grader.

## Table of Contents
- [Introduction](#introduction)
- [Features](#features)
- [Usage](#usage)
- [Installation](#installation)
- [License](#license)

## Introduction

The VMTranslator translates virtual machine (VM) code into Hack assembly language. This is part of the larger [NAND to Tetris course](https://www.nand2tetris.org/), which covers the construction of a computer from first principles, culminating in the creation of an operating system and a compiler.

_Spoiler alert: This is a fully functioning translator. If you are taking the course, writing the VMTranslator from scratch is recommended._

## Features

- Translates all VM commands into Hack assembly language.
- Implements arithmetic and logical operations, memory access commands, and branching operations.
- Supports function call and return commands, as chapters 7 and 8 describe.

## Usage

To use the VMTranslator, follow these steps:

1. **Compile the Java source code**:
   ```bash
   javac VMTranslator.java
   ```

2. **Run the VMTranslator**:
   ```bash
   java VMTranslator <path-to-vm-file-or-directory>
   ```

   Replace `<path-to-vm-file-or-directory>` with the path to a single `.vm` file or a directory containing `.vm` files. The translator will generate a single `.asm` file.

## Installation

Clone this repository and navigate to the project directory:

```bash
git clone <repository-url>
cd VMTranslator
```

Ensure you have Java installed. You can check your Java installation with:

```bash
java -version
```

Then, compile and run the VMTranslator as described in the [Usage](#usage) section.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

This README.md provides an overview of the VMTranslator project, including how to compile and run the translator. If you encounter any issues or have questions, please feel free to open an issue in the repository.
