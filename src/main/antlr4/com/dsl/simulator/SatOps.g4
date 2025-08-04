grammar SatOps;

program: statement+ ;

statement
    : deploySatellite STRING ';'
    | moveSatellite STRING 'to' coordinates ';'
    | print STRING ';'
    ;

print: ' ';

deploySatellite: 'deploy';
moveSatellite: 'move';

coordinates: '(' INT ',' INT ')' ;

PRINT: 'print';
STRING: '"' (~["\r\n])* '"' ;
INT: [0-9]+ ;

WS: [ \t\r\n]+ -> skip ;
