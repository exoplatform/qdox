%{
import com.thoughtworks.qdox.parser.*;
import com.thoughtworks.qdox.parser.structs.*;
import java.io.IOException;
%}

%token SEMI DOT COMMA STAR EQUALS
%token PACKAGE IMPORT PUBLIC PROTECTED PRIVATE STATIC FINAL ABSTRACT NATIVE STRICTFP SYNCHRONIZED TRANSIENT VOLATILE
%token CLASS INTERFACE THROWS EXTENDS IMPLEMENTS
%token BRACEOPEN BRACECLOSE SQUAREOPEN SQUARECLOSE PARENOPEN PARENCLOSE
%token JAVADOCSTART JAVADOCEND
%token CODEBLOCK STRING

// stringly typed tokens/types
%token <sval> IDENTIFIER JAVADOCTAG JAVADOCTOKEN
%type <sval> fullidentifier modifier
%type <ival> dimensions
%type <type> type arrayidentifier

%%


// ----- TOP LEVEL

// A file consists of 0-n fileparts...
file: | file filepart;

// And a filepart is a package/import statement, javadoc comment, or class declaration.
filepart: package | import | javadoc | class;

// Package statement
package: PACKAGE fullidentifier SEMI { builder.addPackage($2); };

// Import statement
import: IMPORT fullidentifier SEMI { builder.addImport($2); };


// ----- JAVADOC

javadoc: JAVADOCSTART javadocdescription javadoctags JAVADOCEND;

javadocdescription: 
    javadoctokens { 
        builder.addJavaDoc(buffer()); 
    };

javadoctokens: | javadoctokens javadoctoken;

javadoctoken: 
     JAVADOCTOKEN { 
        textBuffer.append($1); textBuffer.append(' '); 
    };

javadoctags: | javadoctags javadoctag;

javadoctag: 
    JAVADOCTAG javadoctokens {
        builder.addJavaDocTag($1.substring(1), buffer(), lexer.getLine()); 
    };


// ----- COMMON TOKENS

// A fullidentifier is "a", "a.b", "a.b.c", "a.b.*", etc...
fullidentifier: 
    IDENTIFIER { $$ = $1; } |
    fullidentifier DOT IDENTIFIER { $$ = $1 + '.' + $3; } |
    fullidentifier DOT STAR { $$ = $1 + ".*"; };

arrayidentifier: 
    IDENTIFIER dimensions {
        $$ = new TypeDef($1,$2); 
    };

type: 
    fullidentifier dimensions { 
        $$ = new TypeDef($1,$2); 
    };

dimensions: 
    /* empty */ { $$ = 0; }
|   dimensions SQUAREOPEN SQUARECLOSE {
        $$ = $1 + 1; 
    };

// Modifiers to methods, fields, classes, interfaces, parameters, etc...
modifier:
    PUBLIC          { $$ = "public"; } |
    PROTECTED       { $$ = "protected"; } |
    PRIVATE         { $$ = "private"; } |
    STATIC          { $$ = "static"; } |
    FINAL           { $$ = "final"; } |
    ABSTRACT        { $$ = "abstract"; } |
    NATIVE          { $$ = "native"; } |
    SYNCHRONIZED    { $$ = "synchronized"; } |
    VOLATILE        { $$ = "volatile"; } |
    TRANSIENT       { $$ = "transient"; } |
    STRICTFP        { $$ = "strictfp"; } ;

modifiers: | modifiers modifier { modifiers.add($2); };


// ----- CLASS

class: 
    classdefinition BRACEOPEN members BRACECLOSE { 
        builder.endClass(); 
    };

classdefinition: 
    modifiers classorinterface IDENTIFIER extends implements { 
        cls.modifiers.addAll(modifiers); modifiers.clear(); 
        cls.name = $3; 
        builder.beginClass(cls); 
        cls = new ClassDef(); 
    };

classorinterface: 
    CLASS | 
    INTERFACE { cls.isInterface = true; };

extends: | EXTENDS extendslist;

extendslist: 
    fullidentifier { cls.extendz.add($1); } | 
    extendslist COMMA fullidentifier { cls.extendz.add($3); };

implements: | IMPLEMENTS implementslist;

implementslist: 
    fullidentifier { cls.implementz.add($1); } | 
    implementslist COMMA fullidentifier { cls.implementz.add($3); };

members: | members member;

member:
    javadoc | 
    fields | 
    method |
    constructor |
    modifiers CODEBLOCK | // static block
    class | 
    SEMI;

memberend: SEMI | CODEBLOCK;


// ----- FIELD

fields:
    modifiers type arrayidentifier {
        fieldType = $2;
        makeField($3);
    }
    extrafields memberend {
        modifiers.clear();
    };
  
extrafields: | 
    extrafields COMMA arrayidentifier {
        makeField($3);
    };


// ----- METHOD

method:
    modifiers type IDENTIFIER methoddef memberend {
        mth.modifiers.addAll(modifiers); modifiers.clear(); 
        mth.returns = $2.name; mth.dimensions = $2.dimensions; 
        mth.name = $3; 
        builder.addMethod(mth);
        mth = new MethodDef(); 
    };

constructor:
    modifiers IDENTIFIER methoddef memberend { 
        mth.modifiers.addAll(modifiers); modifiers.clear(); 
        mth.constructor = true; mth.name = $2; 
        builder.addMethod(mth);
        mth = new MethodDef(); 
    };

methoddef: PARENOPEN params PARENCLOSE exceptions;

exceptions: | THROWS exceptionlist;

exceptionlist: 
    fullidentifier { mth.exceptions.add($1); } | 
    exceptionlist COMMA fullidentifier { mth.exceptions.add($3); };

// formal parameters
params: | param paramlist;
paramlist: | paramlist COMMA param;

param: 
    parammodifiers type arrayidentifier { 
        param.name = $3.name; 
        param.type = $2.name; 
        param.dimensions = $2.dimensions + $3.dimensions; 
        mth.params.add(param); param = new FieldDef(); 
    };

parammodifiers: | 
    parammodifiers modifier { param.modifiers.add($2); };


%%

private Lexer lexer;
private Builder builder;
private StringBuffer textBuffer = new StringBuffer();
private ClassDef cls = new ClassDef();
private MethodDef mth = new MethodDef();
private FieldDef param = new FieldDef();
private java.util.Set modifiers = new java.util.HashSet();
private TypeDef fieldType;

private String buffer() {
    if (textBuffer.length() > 0) textBuffer.deleteCharAt(textBuffer.length() - 1);
    String result = textBuffer.toString();
    textBuffer.setLength(0);
    return result;
}

public Parser(Lexer lexer, Builder builder) {
    this.lexer = lexer;
    this.builder = builder;
}

/**
 * Parse file. Return true if successful.
 */
public boolean parse() {
    return yyparse() == 0;
}

private int yylex() {
    try {
        final int result = lexer.lex();
        yylval = new Value();
        yylval.sval = lexer.text();
        return result;
    }
    catch(IOException e) {
        return 0;
    }
}

private void yyerror(String msg) {
    // TODO: Implement error handling
}

private class Value {
    String sval;
    int ival;
    TypeDef type;
}

private void makeField(TypeDef field) {
    FieldDef fd = new FieldDef();
    fd.modifiers.addAll(modifiers); 
    fd.type = fieldType.name; 
    fd.dimensions = fieldType.dimensions + field.dimensions;
    fd.name = field.name;
    builder.addField(fd);
}
            