//// This line declares the name of the grammar
//grammar SatOps;
//
//// This directive adds a package declaration to the generated Java files
////@header {
////package com.dsl.simulator;
////}
//// This is the entry point for the parser.
//// A "program" consists of one or more "statements".
//program: statement+ ;
//
//// These are the labeled rules for different types of statements.
//// The labels (e.g., #deployStatement) are used to generate
//// specific methods in the visitor class, making code more readable.
//statement
//    : 'deploy' ID ';'                                        #deployStatement
//    | 'move' ID 'to' '(' INT ',' INT ')' ';'                 #moveStatement
//    | 'print' STRING ';'                                     #printStatement
//    | 'simulate' 'orbit' NUMBER NUMBER NUMBER ';'            #simulateOrbitStatement
//    ;
//
//
//ID      : [a-zA-Z_] [a-zA-Z_0-9]* ;
//INT     : [0-9]+ ;
//NUMBER  : [0-9]+ ('.' [0-9]+)? ;
//STRING  : '"' (~["\r\n])* '"' ;
//
//// This rule tells the lexer to ignore whitespace characters.
//// The "-> skip" command discards the token.
//WS      : [ \t\r\n]+ -> skip ;
//
grammar SatOps;

program: statement+ ;

statement
    : 'deploy' ID ';'                                        #deployStatement
    | 'move' ID 'to' '(' INT ',' INT ')' ';'                 #moveStatement
    | 'print' STRING ';'                                     #printStatement
    | 'simulateOrbit' NUMBER NUMBER NUMBER ';'               #simulateOrbitStatement
    ;

ID      : [a-zA-Z_] [a-zA-Z_0-9]* ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;  // supports int and decimal
INT     : [0-9]+ ;                // used only for move
STRING  : '"' (~["\r\n])* '"' ;
WS      : [ \t\r\n]+ -> skip ;
