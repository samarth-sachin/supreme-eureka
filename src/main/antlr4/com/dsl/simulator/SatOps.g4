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
//grammar SatOps;
//
//program: statement+ ;
//
////statement
////    : 'deploy' ID ';'                                        #DeployStatement
////    | 'move' ID 'to' '(' NUMBER ',' NUMBER ')' ';'           #MoveStatement
////    | 'print' STRING ';'                                     #PrintStatement
////    | 'simulateOrbit' NUMBER NUMBER NUMBER ';'               #SimulateOrbitStatement
////    ;
//statement
//    : 'deploy' ID ';'                                        #deployStatement
//    | 'move' ID 'to' '(' NUMBER ',' NUMBER ')' ';'           #moveStatement
//    | 'print' STRING ';'                                     #printStatement
//    | 'simulateOrbit' NUMBER NUMBER NUMBER ';'               #simulateOrbitStatement
//    | 'deployGroundStation' ID 'at' '(' NUMBER ',' NUMBER ')' ';' #deployGroundStationStatement
//    | 'link' ID 'to' ID ';'                                  #linkStatement
//    | 'unlink' ID 'from' ID ';'                              #unlinkStatement
//    | 'send' ID 'to' ID STRING ';'                           #sendStatement
//    | 'receive' ID 'from' ID ';'                             #receiveStatement
//    ;
//
//
//ID      : [a-zA-Z_] [a-zA-Z_0-9]* ;
//NUMBER  : [0-9]+ ('.' [0-9]+)? ;
//STRING  : '"' (~["\r\n])* '"' ;
//WS      : [ \t\r\n]+ -> skip ;
grammar SatOps;

// Entry
program
    : statement+ EOF
    ;

// Labeled alternatives for statements
statement
    : deployStatement                 #deployStatementAlt
    | moveStatement                   #moveStatementAlt
    | printStatement                  #printStatementAlt
    | simulateOrbitStatement          #simulateOrbitStatementAlt
    | deployGroundStationStatement    #deployGroundStationStatementAlt
    | linkStatement                   #linkStatementAlt
    | unlinkStatement                 #unlinkStatementAlt
    | sendStatement                   #sendStatementAlt
    | receiveStatement                #receiveStatementAlt
    ;

// ---- Commands ----
deployStatement
    : 'deploy' ID ';'
    ;

moveStatement
    : 'move' ID 'to' '(' NUMBER ',' NUMBER ')' ';'
    ;

printStatement
    : 'print' STRING ';'
    ;

simulateOrbitStatement
    : 'simulateOrbit' NUMBER NUMBER NUMBER ';'
    ;

deployGroundStationStatement
    : 'deployGroundStation' ID 'at' '(' NUMBER ',' NUMBER ')' ';'
    ;

linkStatement
    : 'link' ID 'to' ID ';'
    ;

unlinkStatement
    : 'unlink' ID 'from' ID ';'
    ;

sendStatement
    : 'send' ID 'to' ID STRING ';'
    ;

receiveStatement
    : 'receive' ID 'from' ID ';'
    ;

//  lexer like conditons
ID      : [a-zA-Z_][a-zA-Z0-9_]* ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;
STRING  : '"' (~["\\] | '\\' .)* '"' ;
WS      : [ \t\r\n]+ -> skip ;
COMMENT : '//' ~[\r\n]* -> skip ;
