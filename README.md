# Exon

Exon is an interpreter for the Exon programming language. The project is currently at the lexical scanning stage: source text is tokenized and printed, but parsing, evaluation, and runtime execution are still in progress.

## Current Status

- **Scanner is working** - The scanner reads source input, splits it into tokens, and tracks line numbers for error reporting.
- **CLI entry point is working** - `Exon.main()` can run a source file or start a simple prompt loop.
- **Parser / interpreter are not implemented yet** - The program currently stops after tokenization.

## How The Scanner Works

The scanner walks through the source one character at a time and produces tokens for:

- single-character symbols like `(`, `)`, `{`, `}`, `,`, `.`, `-`, `+`, `;`, `*`, and `/`
- one- and two-character operators like `!`, `!=`, `=`, `==`, `<`, `<=`, `>`, and `>=`
- string literals enclosed in double quotes
- number literals, including decimals
- identifiers and keywords
- line comments that start with `//*`

Keyword recognition is handled through a keyword map. That means the scanner first reads a full identifier, then checks whether the text matches a reserved word such as `and`, `class`, `fxn`, `or`, `put`, or `until`.

If the scanner finds an unexpected character or an unterminated string, it reports a line-aware error through `Exon.error(...)`.

## Running Exon

From the project root, compile the sources and run the interpreter:

```bash
javac com/interpreter/exon/*.java
java com.interpreter.exon.Exon
```

To scan a file instead of using the prompt:

```bash
java com.interpreter.exon.Exon path\to\script.exon
```

The prompt currently prints `/>` and echoes the tokens for each line you enter.

## In Progress

- **Parsing** - Build an AST from the token stream.
- **Semantic analysis** - Add validation for language rules.
- **Execution** - Evaluate statements and expressions at runtime.
- **Language features** - Expand the grammar as the interpreter grows.
