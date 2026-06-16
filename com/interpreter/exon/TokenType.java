package com.interpreter.exon;


enum TokenType {
    // Single character token:
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
                    
    // One or two char tokens:
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
                    
    // Litrals:
    IDENTIFIER, STRING, NUMBER,
                    
    // Keywords:
    AND, CLASS, ELSE, FALSE, FXN, FOR, IF, NIL, OR, RETURN, OUT, SUPER, THIS, TRUE, PUT, UNTIL,
    
    EOF
}