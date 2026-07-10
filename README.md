<div align="center">

# ⚡ Exon

**A tree-walk interpreter crafted in Java**

[![Language](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Status](https://img.shields.io/badge/Status-Active%20Development-22c55e?style=for-the-badge)]()
[![Stage](https://img.shields.io/badge/Stage-Variables%20%26%20Scope%20Complete-6366f1?style=for-the-badge)]()
[![License](https://img.shields.io/badge/License-MIT-0ea5e9?style=for-the-badge)]()

<br/>

_Exon is a hand-built, tree-walk interpreter that compiles source text through_
_scanning → parsing → AST construction → evaluation. Scanner, AST, parser,_
_evaluator, statements, variables, assignment, and block scoping are all complete._
_Control flow (`if`/`else`, loops) is next._

</div>

---

## 📊 Build Progress

| Phase | Component                                  |       Status       |
| :---: | :----------------------------------------- | :----------------: |
|   1   | Lexical Scanner                            |  ✅ **Complete**   |
|   1   | Token types & keyword map                  |  ✅ **Complete**   |
|   2   | AST node definitions (`Expr`)              |  ✅ **Complete**   |
|   2   | Visitor pattern & `AstPrinter`             |  ✅ **Complete**   |
|   2   | AST code-generation tool                   |  ✅ **Complete**   |
|   3   | Recursive-descent Parser                   |  ✅ **Complete**   |
|   3   | Token-level error reporting                |  ✅ **Complete**   |
|   4   | Tree-walk Interpreter (`Interpreter.java`) |  ✅ **Complete**   |
|   4   | Runtime error handling (`RuntimeError`)    |  ✅ **Complete**   |
|   5   | Statement AST (`Stmt.java`)                |  ✅ **Complete**   |
|   5   | `out` print statement                      |  ✅ **Complete**   |
|   5   | Expression statements (`;`)                |  ✅ **Complete**   |
|   6   | `set` variable declarations                | ✅ **Complete**    |
|   6   | Variable assignment expressions            | ✅ **Complete**    |
|   6   | `Environment` (variable storage & lookup)  | ✅ **Complete**    |
|   6   | Block scoping `{ ... }`                    | ✅ **Complete**    |
|   7   | `if` / `else`, `for`, `loop` control flow  |   🔷 **Planned**   |
|   8   | Functions, classes, closures               |   🔷 **Planned**   |

---

## 🗂️ Project Layout

```
Exon/
└── com/
    └── interpreter/
        ├── exon/                    ← Core interpreter package
        │   ├── Exon.java            ← Entry point — file runner & REPL
        │   ├── Scanner.java         ← Lexer: source text → token stream
        │   ├── Token.java           ← Token value object
        │   ├── TokenType.java       ← Enum of every token type
        │   ├── Expr.java            ← AST node hierarchy (auto-generated)
        │   ├── AstPrinter.java      ← Visitor: pretty-prints the AST
        │   ├── Parser.java          ← Recursive-descent parser
        │   ├── Interpreter.java     ← Tree-walk evaluator
        │   ├── Stmt.java            ← Statement AST node hierarchy
        │   ├── Environment.java     ← Variable storage with lexical scoping
        │   └── RuntimeError.java    ← Runtime exception with token context
        └── tool/
            └── generateAst.java     ← Code-gen tool that writes Expr.java
```

---

## ✨ Features

### 🔍 1 - Lexical Scanner

`Scanner.java` walks the source one character at a time and produces a flat list of `Token` objects - each carrying its type, raw lexeme, optional literal value, and line number.

<details>
<summary><b>Token categories recognised</b></summary>

| Category                    | Tokens                                                                                                     |
| :-------------------------- | :--------------------------------------------------------------------------------------------------------- |
| Single-character symbols    | `(` `)` `{` `}` `,` `.` `-` `+` `;` `*` `/`                                                                |
| One/two-character operators | `!` `!=` `=` `==` `<` `<=` `>` `>=`                                                                        |
| String literals             | `"hello, world"` - supports multi-line strings                                                             |
| Number literals             | `42` · `3.14` - integers and decimals                                                                      |
| Identifiers                 | `myVar` · `_count`                                                                                         |
| Line comments               | `// anything to end of line`                                                                               |
| Keywords                    | `and` `class` `else` `false` `for` `fxn` `if` `nil` `or` `out` `set` `return` `super` `this` `true` `loop` |

</details>

> [!NOTE]
> Keywords are resolved _after_ a full identifier is consumed - the scanner checks the final string against a `HashMap<String, TokenType>`. Unknown characters and unterminated strings both emit a line-aware error: `[line N] Error: <message>`.

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
| `Expr.Variable` | `name` | Read the value of a named variable |
| `Expr.Assign` | `name` · `value` | Assign a new value to an existing variable |

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

| Feature             | Detail                                                                                                                   |
| :------------------ | :----------------------------------------------------------------------------------------------------------------------- |
| Entry point         | `Parser.parse()` — returns the root `Expr` or `null` on error                                                            |
| Error type          | Private `ParseError extends RuntimeException` for controlled unwinding                                                   |
| Panic-mode recovery | `synchronize()` discards tokens until a statement boundary (`CLASS` `FOR` `FXN` `IF` `OUT` `RETURN` `SET` `LOOP` or `;`) |
| Token-level errors  | `Exon.error(Token, message)` reports the exact lexeme and position                                                       |

**Token-level error reporting** was also added to `Exon.java` — errors now pinpoint the exact token:

```
[line 3] Error at 'pii': Expect expression.
[line 7] Error at end: Expect ')' after expression.
```

---

### 🖥️ 7 — Tree-Walk Interpreter (`Interpreter.java` + `RuntimeError.java`)

`Interpreter.java` is the execution engine. It implements `Expr.Visitor<Object>` and walks the AST produced by the parser, computing a Java `Object` for every node.

**Supported operations:**

| Operation                  | Behaviour                                                                          |
| :------------------------- | :--------------------------------------------------------------------------------- |
| **Literals**               | Numbers (`Double`), strings (`String`), `true`/`false` (`Boolean`), `nil` (`null`) |
| **Grouping**               | Evaluates the inner expression transparently                                       |
| **Unary `-`**              | Negates a number; throws `RuntimeError` if operand is not a number                 |
| **Unary `!`**              | Logical NOT — `nil` and `false` are falsy, everything else is truthy               |
| **Arithmetic** `+ - * /`   | Operates on two numbers; `+` also concatenates two strings                         |
| **Comparison** `> >= < <=` | Compares two numbers, returns a boolean                                            |
| **Equality** `== !=`       | Works on any two values; `nil` is only equal to `nil`                              |

**Output formatting (`stringify`):** integers are printed without a trailing `.0` (e.g. `5` not `5.0`); `nil` prints as `nil`.

`RuntimeError.java` is a thin `RuntimeException` subclass that carries the offending `Token`, enabling precise line-level runtime error messages:

```
Operand must be a number.
[line 4]
```

`Exon.java` was updated with:

- A static `Interpreter` instance shared across all `run()` calls
- A `hadRuntimeError` flag that causes script files to exit with code **70**
- A `RuntimeError(RuntimeError)` handler method that prints the message + line
- The full pipeline is now **scan → parse → interpret**
- `interpret()` now accepts `List<Stmt>` and iterates with `execute(Stmt)`
- Implements both `Expr.Visitor<Object>` and `Stmt.Visitor<Void>`

---

### 📜 8 — Statements (`Stmt.java`)

`Stmt.java` mirrors `Expr.java` but for _statements_ — side-effecting constructs that don't produce a value. It follows the same auto-generated Visitor pattern.

| Node | Fields | Represents |
|:---|:---|:---|
| `Stmt.Expression` | `expression` | An expression evaluated for its side effects, terminated by `;` |
| `Stmt.Out` | `expression` | The `out` keyword — evaluates the expression and prints the result |
| `Stmt.Set` | `name` · `initializer` | Variable declaration: `set x = <expr>;` (initializer optional) |
| `Stmt.Block` | `statements` | A `{ ... }` block that creates a new nested scope |

**Parser changes:**

- `parse()` now returns `List<Stmt>` (previously returned a single `Expr`)
- New `statement()` dispatch: matches `out` → `printStatement()`, otherwise → `expressionStatement()`
- Both statement parsers require a trailing `;` and report errors if missing

**Interpreter changes:**

- Now implements `Stmt.Visitor<Void>` in addition to `Expr.Visitor<Object>`
- `visitExpressionStmt` — evaluates the expression and discards the result
- `visitOutStmt` — evaluates and calls `System.out.println(stringify(value))`

**Example Exon program now runnable:**

```
out 1 + 2;                // prints: 3
out "hello" + " world";   // prints: hello world
out 5 > 3;                // prints: true
2 * 3;                    // expression statement (no output)
set x = 10;               // variable declaration
out x;                    // prints: 10
x = x + 1;                // assignment
{ set y = x * 2; out y; } // block scope — y is local
```

---

### 🗄️ 9 — Variables, Assignment & Environments

#### `Environment.java`

The `Environment` class provides a **linked-chain of scopes** — each block gets its own environment that holds a reference to the enclosing one. Variable lookup and assignment walk the chain outward.

| Method | Behaviour |
|:---|:---|
| `define(name, value)` | Binds a new name in the **current** scope (always succeeds, allows shadowing) |
| `get(token)` | Searches the current scope, then enclosing scopes; throws `RuntimeError` if not found |
| `assign(token, value)` | Updates an **existing** binding anywhere in the scope chain; throws `RuntimeError` if undeclared |

#### New expression nodes in `Expr.java`

| Node | Parsed as | Evaluated as |
|:---|:---|:---|
| `Expr.Variable` | a bare identifier `x` | looks up `x` in the current environment |
| `Expr.Assign` | `x = <expr>` | evaluates the right-hand side, assigns it to `x` |

Assignment is **right-associative** and parses at the same level as `equality`, using a lookahead: if the left-hand side turns out to be an `Expr.Variable`, it's rewritten into an `Expr.Assign`; otherwise a parse error is reported.

#### New statement nodes in `Stmt.java`

| Node | Syntax | Behaviour |
|:---|:---|:---|
| `Stmt.Set` | `set <name> = <expr>;` | Declares a variable in the current scope; initializer is optional (defaults to `nil`) |
| `Stmt.Block` | `{ <stmts> }` | Creates a child `Environment`, executes all inner statements, then restores the previous scope |

#### `executeBlock` in `Interpreter.java`

```java
void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    try {
        this.environment = environment;  // enter new scope
        for (Stmt s : statements) execute(s);
    } finally {
        this.environment = previous;     // restore on exit (even on error)
    }
}
```

---

### ⚙️ 5 — AST Code-Generation Tool (`generateAst.java`)

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
/> set x = 42;
/> out x + 1;
```

> The REPL prompt is `/>`. Press **Ctrl-Z** _(Windows)_ or **Ctrl-D** _(Unix)_ to exit.

---

## 🛡️ Error Handling

| Scenario                       | Behaviour                                               | Exit code |
| :----------------------------- | :------------------------------------------------------ | :-------: |
| Unexpected character in source | `[line N] Error: Unexpected character.`                 |     —     |
| Unterminated string literal    | `[line N] Error: Unterminated string.`                  |     —     |
| Parse error (bad syntax)       | `[line N] Error at '<token>': <message>`                |     —     |
| Parse error at EOF             | `[line N] Error at end: <message>`                      |     —     |
| Runtime type error             | `<message>\n[line N]`                                   |     —     |
| Scan/parse error in script     | Exits immediately after reporting                       |  **65**   |
| Runtime error in script        | Exits after the failing expression                      |  **70**   |
| REPL scan/parse error          | Error printed; **session continues** (`hadError` reset) |     —     |
| REPL runtime error             | Error printed; **session continues**                    |     —     |

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
Phase 4 — Interpreter  ✅ Done
```

- [x] Tree-walk evaluator implementing `Expr.Visitor<Object>`
- [x] Arithmetic, comparison, equality, logical operators
- [x] String concatenation with `+`
- [x] Truthiness rules (`nil` and `false` are falsy)
- [x] Runtime type checking with `RuntimeError`
- [x] Script exit code **70** on runtime errors

```
Phase 5 — Statements  ✅ Done
```

- [x] `Stmt` AST hierarchy with Visitor pattern
- [x] `out` print statement
- [x] Expression statements (`;` terminated)
- [x] Parser updated to produce `List<Stmt>`
- [x] Interpreter updated to execute statement lists

```
Phase 6 — Variables & Scope  ✅ Done
```
- [x] `Expr.Variable` and `Expr.Assign` nodes
- [x] `set` variable declarations (`Stmt.Set`)
- [x] Block scoping (`Stmt.Block`)
- [x] `Environment` with lexical scope chain
- [x] `assignment()` in parser with l-value validation

```
Phase 7 — Control Flow
```
- [ ] `if` / `else` branching
- [ ] `for` loop construct
- [ ] `loop` (while-style) construct
- [ ] Logical `and` / `or` with short-circuit evaluation

```
Phase 8 — Functions & Classes
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

**Built with ❤️ by Naman Saini - following the tree-walk interpreter tradition**

</div>
