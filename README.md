# Exon

Exon is a tree-walk interpreter for the Exon programming language, written in Java.
The project has progressed well past the scanner stage: it now includes a full
expression grammar, a generated Abstract Syntax Tree (AST), the Visitor pattern for
tree traversal, and an AST pretty-printer that can render expressions in
S-expression form.

## Current Status

| Stage | Status |
|---|---|
| Lexical scanning | ✅ Complete |
| Token types & keyword map | ✅ Complete |
| AST node definitions (`Expr`) | ✅ Complete |
| Visitor pattern & `AstPrinter` | ✅ Complete |
| AST code-generation tool | ✅ Complete |
| Parser (tokens → AST) | 🚧 In progress |
| Interpreter / evaluator | 🚧 Planned |
| Statements & declarations | 🚧 Planned |
| Functions, classes, closures | 🚧 Planned |

---

## Project Layout

```
com/
├── interpreter/
│   ├── exon/                   # Core interpreter package
│   │   ├── Exon.java           # Entry point – file runner & REPL
│   │   ├── Scanner.java        # Lexer: source text → token stream
│   │   ├── Token.java          # Token value object
│   │   ├── TokenType.java      # Enum of every token type
│   │   ├── Expr.java           # AST node hierarchy (auto-generated)
│   │   └── AstPrinter.java     # Visitor that pretty-prints an AST
│   └── tool/
│       └── generateAst.java    # Code-gen tool that writes Expr.java
```

---

## Features

### 1 · Lexical Scanner

`Scanner.java` walks the source one character at a time and produces a flat
list of `Token` objects, each carrying its `TokenType`, raw lexeme, optional
literal value, and line number.

Recognised token categories:

| Category | Examples |
|---|---|
| Single-character symbols | `(` `)` `{` `}` `,` `.` `-` `+` `;` `*` `/` |
| One/two-character operators | `!` `!=` `=` `==` `<` `<=` `>` `>=` |
| String literals | `"hello, world"` (multi-line strings supported) |
| Number literals | `42`, `3.14` (integer & decimal) |
| Identifiers | `myVar`, `_count` |
| Keywords | see table below |
| Line comments | `// anything to end of line` |

**Keywords recognised by the scanner:**

`and` · `class` · `else` · `false` · `for` · `fxn` · `if` · `nil` · `or` ·
`out` · `put` · `return` · `super` · `this` · `true` · `until`

> **Note on comment syntax:** the scanner treats `//` (two forward slashes) as
> the start of a comment, consuming the rest of that line. This is a slight
> difference from the original README which described `//*`.

Errors (unexpected characters, unterminated strings) are reported with a line
number through `Exon.error(line, message)`.

---

### 2 · Token & TokenType

`Token.java` is a plain value object:

```java
class Token {
    final TokenType type;    // e.g. NUMBER, IDENTIFIER, PLUS …
    final String    lexeme;  // raw source text
    final Object    literal; // parsed value for strings & numbers
    final int       line;    // for error messages
}
```

`TokenType.java` is an enum grouping tokens into four sections:
single-character, one-or-two-character, literals, and keywords.

---

### 3 · Expression AST (`Expr.java`)

The `Expr` hierarchy represents every kind of expression the parser will
eventually produce. All node types live as `static` inner classes and share a
single generic `Visitor<R>` interface so that any pass over the tree (printing,
interpreting, resolving, …) only needs to implement one method per node.

| Node | Fields | Meaning |
|---|---|---|
| `Expr.Binary` | `left`, `operator`, `right` | Infix operations: `+`, `-`, `*`, `/`, `==`, `!=`, `<`, `<=`, `>`, `>=` |
| `Expr.Grouping` | `expression` | Parenthesised sub-expression `(expr)` |
| `Expr.Literal` | `value` | Number, string, `true`, `false`, or `nil` |
| `Expr.Unary` | `operator`, `right` | Prefix operations: `-expr` or `!expr` |

Each node implements `accept(Visitor<R> visitor)`, enabling the classic double-dispatch Visitor pattern.

---

### 4 · AST Pretty-Printer (`AstPrinter.java`)

`AstPrinter` is the first concrete `Expr.Visitor<String>`. It walks an
expression tree and returns a fully parenthesised S-expression string, which is
useful for debugging and verifying parser output.

Example (built manually in `AstPrinter.main`):

```
(-123 * (group 45.67))
→  (* (- 123) (group 45.67))
```

Run it directly:

```bash
javac com/interpreter/exon/*.java
java  com.interpreter.exon.AstPrinter
```

---

### 5 · AST Code-Generation Tool (`generateAst.java`)

Because adding new node types to a hand-written `Expr.java` is tedious and
error-prone, the project ships a small meta-programming tool in
`com/interpreter/tool/generateAst.java`.

The tool accepts an output directory and writes a fully formed `Expr.java` that
includes the `Visitor` interface, all node inner classes (constructor + fields +
`accept`), and the abstract base `accept` method.

Compile and run:

```bash
javac com/interpreter/tool/generateAst.java
java  com.interpreter.tool.generateAst com/interpreter/exon
```

The current grammar passed to the tool:

```
Binary   : Expr left, Token operator, Expr right
Grouping : Expr expression
Literal  : Object value
Unary    : Token operator, Expr right
```

---

## Running Exon

### Compile everything

```bash
javac com/interpreter/exon/*.java
```

### Run a source file

```bash
java com.interpreter.exon.Exon path\to\script.exon
```

### Start the interactive REPL

```bash
java com.interpreter.exon.Exon
```

The prompt displays `/>`. Each line you enter is scanned and all tokens are
printed. Press **Ctrl-D** (Unix) or **Ctrl-Z** (Windows) to exit.

---

## Error Handling

- Errors do **not** crash the REPL; `hadError` is reset after each line so you
  can keep typing.
- Running a file that contains errors exits with code `65`.
- All errors include the line number: `[line 3] Error: Unexpected character.`

---

## Roadmap

- [ ] **Parser** — consume the token stream and build an `Expr` tree
- [ ] **Statements** — `put` (variable declaration), `out` (print), `if`, `for`, `until` blocks
- [ ] **Interpreter / evaluator** — walk the AST and compute values
- [ ] **Functions** — `fxn` declarations, `return`, closures
- [ ] **Classes** — `class`, `this`, `super`, inheritance
- [ ] **Standard library** — built-in utilities for I/O, strings, math
