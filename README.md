<div align="center">

# ⚡ Exon

**A tree-walk interpreter crafted in Java**

[![Language](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Status](https://img.shields.io/badge/Status-Active%20Development-22c55e?style=for-the-badge)]()
[![Stage](https://img.shields.io/badge/Stage-Parser%20Complete-6366f1?style=for-the-badge)]()
[![License](https://img.shields.io/badge/License-MIT-0ea5e9?style=for-the-badge)]()

<br/>

*Exon is a hand-built, tree-walk interpreter that compiles source text through*
*scanning → parsing → AST construction → evaluation. The scanner, AST, and*
*recursive-descent parser are complete; the tree-walk evaluator is next.*

</div>

---

## 📊 Build Progress

| Phase | Component | Status |
|:---:|:---|:---:|
| 1 | Lexical Scanner | ✅ **Complete** |
| 1 | Token types & keyword map | ✅ **Complete** |
| 2 | AST node definitions (`Expr`) | ✅ **Complete** |
| 2 | Visitor pattern & `AstPrinter` | ✅ **Complete** |
| 2 | AST code-generation tool | ✅ **Complete** |
| 3 | Recursive-descent Parser | ✅ **Complete** |
| 3 | Token-level error reporting | ✅ **Complete** |
| 4 | Interpreter / evaluator | 🚧 **In Progress** |
| 4 | Statements & declarations | 🔷 **Planned** |
| 5 | Functions, classes, closures | 🔷 **Planned** |

---

## 🗂️ Project Layout

```
Exon/
└── com/
    └── interpreter/
        ├── exon/                    ← Core interpreter package
        │   ├── Exon.java            ← Entry point - file runner & REPL
        │   ├── Scanner.java         ← Lexer: source text → token stream
        │   ├── Token.java           ← Token value object
        │   ├── TokenType.java       ← Enum of every token type
        │   ├── Expr.java            ← AST node hierarchy (auto-generated)
        │   └── AstPrinter.java      ← Visitor: pretty-prints the AST
        └── tool/
            └── generateAst.java     ← Code-gen tool that writes Expr.java
```

---

## ✨ Features

### 🔍 1 - Lexical Scanner

`Scanner.java` walks the source one character at a time and produces a flat list of `Token` objects - each carrying its type, raw lexeme, optional literal value, and line number.

<details>
<summary><b>Token categories recognised</b></summary>

| Category | Tokens |
|:---|:---|
| Single-character symbols | `(` `)` `{` `}` `,` `.` `-` `+` `;` `*` `/` |
| One/two-character operators | `!` `!=` `=` `==` `<` `<=` `>` `>=` |
| String literals | `"hello, world"` - supports multi-line strings |
| Number literals | `42` · `3.14` - integers and decimals |
| Identifiers | `myVar` · `_count` |
| Line comments | `// anything to end of line` |
| Keywords | `and` `class` `else` `false` `for` `fxn` `if` `nil` `or` `out` `put` `return` `super` `this` `true` `until` |

</details>

> [!NOTE]
> Keywords are resolved *after* a full identifier is consumed - the scanner checks the final string against a `HashMap<String, TokenType>`. Unknown characters and unterminated strings both emit a line-aware error: `[line N] Error: <message>`.

---

### 🏷️ 2 - Token & TokenType

`Token.java` is a minimal, immutable value object:

```java
class Token {
    final TokenType type;    // e.g. NUMBER, IDENTIFIER, PLUS …
    final String    lexeme;  // raw source text
    final Object    literal; // parsed value for strings & numbers
    final int       line;    // source line - used in error messages
}
```

`TokenType.java` organises every variant into four logical groups via an enum:

```
Single-char   →  LEFT_PAREN  RIGHT_PAREN  LEFT_BRACE  RIGHT_BRACE  …
Two-char      →  BANG  BANG_EQUAL  EQUAL  EQUAL_EQUAL  LESS  LESS_EQUAL  …
Literals      →  IDENTIFIER  STRING  NUMBER
Keywords      →  AND  CLASS  ELSE  FALSE  FOR  FXN  IF  NIL  OR  …
```

---

### 🌲 3 - Expression AST (`Expr.java`)

The `Expr` hierarchy models every expression the parser will eventually produce. Each node is a `static` inner class of the base `Expr` type, and all share a single generic `Visitor<R>` interface - meaning any tree pass (printing, evaluation, resolution…) only needs one method per node.

| Node | Fields | Represents |
|:---|:---|:---|
| `Expr.Binary` | `left` · `operator` · `right` | Infix ops: `+` `-` `*` `/` `==` `!=` `<` `<=` `>` `>=` |
| `Expr.Grouping` | `expression` | Parenthesised sub-expression `(expr)` |
| `Expr.Literal` | `value` | A number, string, `true`, `false`, or `nil` |
| `Expr.Unary` | `operator` · `right` | Prefix ops: `-expr` or `!expr` |

Each node implements `accept(Visitor<R>)` enabling **double-dispatch** - the canonical solution to the Expression Problem in statically-typed OOP.

---

### 🖨️ 4 - AST Pretty-Printer (`AstPrinter.java`)

`AstPrinter` is the first concrete `Expr.Visitor<String>`. It walks any expression tree and returns a fully-parenthesised **S-expression** string - invaluable for verifying parser output.

```
Expression built:  (-123)  *  (group 45.67)
AstPrinter output: (* (- 123) (group 45.67))
```

Run it standalone:

```bash
javac com/interpreter/exon/*.java
java  com.interpreter.exon.AstPrinter
```

---

### 🧩 6 — Recursive-Descent Parser (`Parser.java`)

`Parser.java` is a hand-written **recursive-descent parser** that consumes the flat token stream produced by the scanner and builds a structured `Expr` tree.

The grammar is stratified by operator precedence — lowest precedence at the top, highest at the bottom — and each rule is implemented as its own private method:

```
expression  →  equality
equality    →  comparison  ( ( "!=" | "==" )  comparison )*
comparison  →  term        ( ( ">" | ">=" | "<" | "<=" )  term )*
term        →  factor      ( ( "+" | "-" )  factor )*
factor      →  unary       ( ( "*" | "/" )  unary )*
unary       →  ( "!" | "-" )  unary  |  primary
primary     →  NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")"
```

**Key implementation details:**

| Feature | Detail |
|:---|:---|
| Entry point | `Parser.parse()` — returns the root `Expr` or `null` on error |
| Error type | Private `ParseError extends RuntimeException` for controlled unwinding |
| Panic-mode recovery | `synchronize()` discards tokens until a statement boundary (`CLASS` `FOR` `FXN` `IF` `OUT` `RETURN` `PUT` `UNTIL` or `;`) |
| Token-level errors | `Exon.error(Token, message)` reports the exact lexeme and position |

**Token-level error reporting** was also added to `Exon.java` — errors now pinpoint the exact token:

```
[line 3] Error at 'pii': Expect expression.
[line 7] Error at end: Expect ')' after expression.
```

---

### ⚙️ 5 - AST Code-Generation Tool (`generateAst.java`)

Rather than hand-editing `Expr.java` every time a new node is needed, the project ships a **meta-programming tool** at `com/interpreter/tool/generateAst.java`.

It reads a simple grammar spec and writes a complete, correctly-structured `Expr.java` including the `Visitor` interface, all inner node classes, and the abstract `accept` method.

```bash
# Compile the tool
javac com/interpreter/tool/generateAst.java

# Regenerate Expr.java into the exon package
java  com.interpreter.tool.generateAst com/interpreter/exon
```

Current grammar spec passed to the tool:

```
Binary   : Expr left, Token operator, Expr right
Grouping : Expr expression
Literal  : Object value
Unary    : Token operator, Expr right
```

---

## 🚀 Running Exon

### Step 1 - Compile

```bash
javac com/interpreter/exon/*.java
```

### Step 2 - Run a script file

```bash
java com.interpreter.exon.Exon path\to\script.exon
```

### Step 3 - Or launch the interactive REPL

```bash
java com.interpreter.exon.Exon
```

```
/> put x = 42;
/> out x + 1;
```

> The REPL prompt is `/>`. Press **Ctrl-Z** *(Windows)* or **Ctrl-D** *(Unix)* to exit.

---

## 🛡️ Error Handling

| Scenario | Behaviour |
|:---|:---|
| Unexpected character in source | `[line N] Error: Unexpected character.` |
| Unterminated string literal | `[line N] Error: Unterminated string.` |
| Error in REPL | Error printed; **session continues** - `hadError` is reset each line |
| Error in script file | Interpreter exits with code **65** |

---

## 🗺️ Roadmap

```
Phase 3 — Parser  ✅ Done
```
- [x] Recursive descent parser consuming the token stream
- [x] Full expression grammar with correct operator precedence
- [x] Panic-mode error recovery via `synchronize()`
- [x] Token-level error messages (lexeme + position)

```
Phase 4 — Interpreter
```
- [ ] Tree-walk evaluator implementing `Expr.Visitor<Object>`
- [ ] `put` variable declarations and `out` print statements
- [ ] `if` / `else` branching
- [ ] `for` and `until` loop constructs

```
Phase 5 — Functions & Classes
```
- [ ] `fxn` declarations, first-class functions, `return`
- [ ] Closures and lexical scoping
- [ ] `class` definitions, `this`, `super`, single inheritance

```
Phase 6 — Standard Library
```
- [ ] Built-in utilities for I/O, strings, and math

---

<div align="center">

**Built with ❤️ - following the tree-walk interpreter tradition**

</div>
