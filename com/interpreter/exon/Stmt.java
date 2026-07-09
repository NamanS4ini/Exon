package com.interpreter.exon;

import java.util.List;

abstract class Stmt {
    interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitOutStmt(Out stmt);
    R visitSetStmt(Set stmt);
    }
 static class Expression extends Stmt{
    Expression(Expr expression) {
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
    }
 static class Out extends Stmt{
    Out(Expr expression) {
    this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitOutStmt(this);
    }

    final Expr expression;
    }
 static class Set extends Stmt{
    Set(Token name, Expr initializer) {
    this.name = name;
    this.initializer = initializer;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
    return visitor.visitSetStmt(this);
    }

    final Token name;
    final Expr initializer;
    }

    abstract <R> R accept(Visitor<R> visitor);
}
