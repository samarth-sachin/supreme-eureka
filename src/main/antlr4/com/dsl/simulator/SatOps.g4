
grammar SatOps;

program
    : statement+ EOF
    ;

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
    | predictPassStatement            #predictPassStatementAlt
    | maneuverStatement               #maneuverStatementAlt
    ;

maneuverStatement: 'maneuver' ID 'burn' NUMBER 'in' ID 'direction' ';';

// ---------------- Commands ----------------
// In your SatOps.g4 file

deployStatement:
    'deploy' ID 'with' 'id' NUMBER ';'
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

predictPassStatement
    : 'predictPass' ID 'over' ID ';'
    ;

// ---------------- Lexer ----------------
ID      : [a-zA-Z_][a-zA-Z0-9_]* ;
NUMBER  : [0-9]+ ('.' [0-9]+)? ;
STRING  : '"' (~["\\] | '\\' .)* '"' ;
WS      : [ \t\r\n]+ -> skip ;
COMMENT : '//' ~[\r\n]* -> skip ;
