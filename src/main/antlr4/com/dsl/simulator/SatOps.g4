grammar SatOps;

program: statement+ ;

statement: PRINT STRING ';' ;

PRINT: 'print';
STRING: '"' (~["\r\n])* '"' ;
WS: [ \t\r\n]+ -> skip ;
