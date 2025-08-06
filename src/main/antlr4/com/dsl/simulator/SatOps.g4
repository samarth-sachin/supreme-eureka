grammar SatOps;

program: statement+ ;

statement
    :deployStmt                              #deployStatement
    | moveStmt                                #moveStatement
    | printStmt                               #printStatement
    ;

deployStmt: 'deploy' ID ';';
moveStmt: 'move' ID 'to' '(' INT ',' INT ')' ';';
printStmt: 'print' STRING ';';

ID: [a-zA-Z_][a-zA-Z_0-9]*;
INT: [0-9]+;
STRING: '"' (~["\r\n])* '"';
WS: [ \t\r\n]+ -> skip;
