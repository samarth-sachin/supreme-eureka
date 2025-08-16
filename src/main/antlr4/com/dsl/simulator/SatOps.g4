grammar SatOps;

@header {
package com.dsl.simulator;
}

// Entry
program: statement+ ;

// Labeled alternatives with tokens inlined
statement
    : 'deploy'  ID ';'                                        #deployStatement
    | 'move'    ID 'to' '(' INT ',' INT ')' ';'               #moveStatement
    | 'print'   STRING ';'                                    #printStatement
    | 'simulate' 'orbit' NUMBER NUMBER NUMBER ';'             #simulateOrbitStatement
    ;

// Lexer rules
ID      : [a-zA-Z_][a-zA-Z_0-9]* ;
INT     : [0-9]+ ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;
STRING  : '"' (~["\r\n])* '"' ;
WS      : [ \t\r\n]+ -> skip ;
//grammar SatOps;
//
//program: statement+ ;
//
//statement
//    :deployStmt                              #deployStatement
//    | moveStmt                                #moveStatement
//    | printStmt                               #printStatement
//    ;
//
//deployStmt: 'deploy' ID ';';
//moveStmt: 'move' ID 'to' '(' INT ',' INT ')' ';';
//printStmt: 'print' STRING ';';
//
//ID: [a-zA-Z_][a-zA-Z_0-9]*;
//INT: [0-9]+;
//STRING: '"' (~["\r\n])* '"';
//WS: [ \t\r\n]+ -> skip;