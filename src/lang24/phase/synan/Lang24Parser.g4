parser grammar Lang24Parser;

@header {
    package lang24.phase.synan;

	import java.util.*;
	import lang24.common.report.*;
	import lang24.data.ast.tree.*;
	import lang24.data.ast.tree.type.*;
	import lang24.data.ast.tree.stmt.*;
	import lang24.data.ast.tree.expr.*;
	import lang24.data.ast.tree.defn.*;
	import lang24.data.token.*;
	import lang24.phase.lexan.*;
	import lang24.phase.abstr.*;
}

@members {
	private Location loc(Token tok) { return new Location((LocLogToken)tok); }
	private Location loc(Token     tok1, Token     tok2) { return new Location((LocLogToken)tok1, (LocLogToken)tok2); }
	private Location loc(Token     tok1, Locatable loc2) { return new Location((LocLogToken)tok1, loc2); }
	private Location loc(Locatable loc1, Token     tok2) { return new Location(loc1, (LocLogToken)tok2); }
	private Location loc(Locatable loc1, Locatable loc2) { return new Location(loc1, loc2); }
}

options{
    tokenVocab=Lang24Lexer;
}


source returns [AstNodes ast]
: definitions EOF {$ast = $definitions.ast; } ;

definitions returns [AstNodes ast]
@init {
    var nodes = new ArrayList<AstNode>();
}
: (typeDefinition {nodes.add($typeDefinition.value);}
|  variableDefinition {nodes.add($variableDefinition.value);}
|  functionDefinition { nodes.add($functionDefinition.value);}
)+ { $ast = new AstNodes(loc($start, getCurrentToken()), nodes); };

typeDefinition returns [AstTypDefn value]
: name EQUALS type
{$value = new AstTypDefn(loc($start, $type.value), $name.value.name, $type.value); };

variableDefinition returns [AstVarDefn value]
: name COLON type
{$value = new AstVarDefn(loc($start, $type.value), $name.value.name, $type.value); };

functionDefinition returns [AstFunDefn value]
@init {
    boolean isStmt = false;
    boolean isDefns = false;
    boolean arePars = false;
}
: name LPARENTHESIS ({arePars = true;} pars=parameters )? RPARENTHESIS COLON type
    ( {isStmt = true;} EQUALS stmt=statement
        ({isDefns = true;}LBRACE defns=definitions RBRACE )? )?
{
    var locValue = isDefns ? loc($start, $RBRACE)
        : loc($start, isStmt ? $stmt.value
        : $type.value);

    $value = new AstFunDefn(
        locValue,
        $name.value.name,
        arePars ? $pars.value : null,
        $type.value,
        isStmt ? $stmt.value: null,
        isDefns ? $defns.ast : null);
};

parameters returns [AstNodes value]
: param (COMMA param)*
{
    $value = new AstNodes((LocLogToken) getCurrentToken() , $ctx.param()
        .stream()
        .map(p -> p.value)
        .toList());
};

param returns [AstFunDefn.AstParDefn value]
: (CARET)? name COLON type
{
    var locValue = $CARET == null ? loc($name.value, $type.value) : loc($CARET, $type.value);

    $value = $CARET != null
        ? new AstFunDefn.AstRefParDefn(locValue, $name.value.name, $type.value)
        : new AstFunDefn.AstValParDefn(locValue, $name.value.name, $type.value);
};

statement returns [AstStmt value]
@init {
    boolean isElseStmt = false;
}
:expr=expression SEMICOLON {$value = new AstExprStmt(loc($start), $expr.value);}
| dst=expression EQUALS src=expression SEMICOLON{ $value = new AstAssignStmt(loc($start, $SEMICOLON), $dst.value, $src.value);}
| IF cond=expression THEN thenStmt=statement ( {isElseStmt = true;} ELSE elseStmt=statement )? {$value = new AstIfStmt(loc($start, isElseStmt ? $elseStmt.value : $thenStmt.value), $cond.value, $thenStmt.value, isElseStmt ? $elseStmt.value : null);}
| WHILE cond=expression COLON body=statement{ $value = new AstWhileStmt(loc($start, $body.value), $cond.value, $body.value); }
| RETURN expr=expression SEMICOLON{$value = new AstReturnStmt(loc($RETURN, $SEMICOLON), $expr.value); }
| LBRACE (statement)+ RBRACE
{
    $value = new AstBlockStmt(loc($LBRACE, $RBRACE), $ctx.statement()
        .stream()
        .map(s -> s.value)
        .toList());
};


type returns [AstType value]
: atomType {$value = $atomType.value;}
| LBRACKET size=intconst RBRACKET elemType=type {
    $value = new AstArrType(loc($LBRACKET, $RBRACKET), $elemType.value, $size.value);

    }
| CARET type { $value = new AstPtrType(loc($CARET, $type.value), $type.value); }
| components { $value = $components.value; }
| name {$value = new AstNameType($name.value, $name.value.name); }
;

atomType returns [AstAtomType value]
: VOID {$value = new AstAtomType(loc($VOID), AstAtomType.Type.VOID);}
| BOOL {$value = new AstAtomType(loc($BOOL), AstAtomType.Type.BOOL);}
| CHAR {$value = new AstAtomType(loc($CHAR), AstAtomType.Type.CHAR);}
| INT  {$value = new AstAtomType(loc($INT), AstAtomType.Type.INT);};

components returns [AstRecType value]
: LPARENTHESIS cmps=componentList RPARENTHESIS {$value = new AstStrType(loc($start, $RPARENTHESIS), $componentList.value); }
| LBRACE cmps=componentList RBRACE {$value = new AstUniType(loc($start, $RBRACE), $componentList.value); }
;

componentList returns [AstNodes value]
: component (COMMA component)*
{
    $value = new AstNodes((LocLogToken) getCurrentToken(), $ctx.component()
        .stream()
        .map(x -> x.value)
        .toList());
};

component returns [AstRecType.AstCmpDefn value]
: name COLON type {$value = new AstRecType.AstCmpDefn(loc($start, $type.value), $name.value.name, $type.value); }
;

expression returns [AstExpr value]
@init {
    boolean arePars = false;
}
: constExpression { $value = $constExpression.value; }
| name ( LPARENTHESIS ({arePars = true;} expression (COMMA expression)* )? RPARENTHESIS )?
{

    $value = $LPARENTHESIS == null
            ? $name.value
            : new AstCallExpr(
                $RPARENTHESIS != null
                    ? loc($start, $RPARENTHESIS)
                    : loc($start, $name.value),
                $name.value.name,
                !arePars
                    ? null
                    : new AstNodes($ctx.expression()
                        .stream()
                        .map(e -> e.value)
                        .toList()));
}
| arr=expression LBRACKET idx=expression RBRACKET {$value = new AstArrExpr(loc($start, $RBRACKET), $arr.value, $idx.value ); }
| expr=expression DOT name {$value = new AstCmpExpr(loc($start, $name.value), $expr.value, $name.value.name); }
| expr=expression CARET {$value = new AstSfxExpr(loc($start, $CARET), AstSfxExpr.Oper.PTR, $expr.value);}
| LESS type GREATER expr=expression {$value = new AstCastExpr(loc($LESS, $expr.value), $type.value, $expr.value ); }
| prefixOperator expr=expression
{
    AstPfxExpr.Oper operatorVal = null;
        switch($prefixOperator.text) {
            case "not": operatorVal = AstPfxExpr.Oper.NOT ; break;
            case "+": operatorVal = AstPfxExpr.Oper.ADD ; break;
            case "-": operatorVal = AstPfxExpr.Oper.SUB ; break;
            case "^": operatorVal = AstPfxExpr.Oper.PTR ; break;
        }
    $value = new AstPfxExpr(loc($ctx.prefixOperator.value, $expr.value), operatorVal, $expr.value);
}
| fstExpr=expression multiplicativeOperators sndExpr=expression{$value = new AstBinExpr(loc($start, $sndExpr.value), $multiplicativeOperators.value, $fstExpr.value, $sndExpr.value);}
| fstExpr=expression additiveOperators sndExpr=expression  {$value = new AstBinExpr(loc($start, $sndExpr.value), $additiveOperators.value, $fstExpr.value, $sndExpr.value);}
| fstExpr=expression relationalOperators sndExpr=expression  {$value = new AstBinExpr(loc($start, $sndExpr.value), $relationalOperators.value, $fstExpr.value, $sndExpr.value);}
| fstExpr=expression conjuctiveOperators sndExpr=expression{$value = new AstBinExpr(loc($start, $sndExpr.value), $conjuctiveOperators.value, $fstExpr.value, $sndExpr.value);}
| fstExpr=expression disjunctiveOperators sndExpr=expression{$value = new AstBinExpr(loc($start, $sndExpr.value), $disjunctiveOperators.value, $fstExpr.value, $sndExpr.value);}

| SIZEOF LPARENTHESIS type RPARENTHESIS {$value = new AstSizeofExpr(loc($SIZEOF, $RPARENTHESIS), $type.value); }
| LPARENTHESIS expr=expression RPARENTHESIS {$value = $expr.value; }
;


constExpression returns [AstAtomExpr value]
: voidconst {$value = $voidconst.value; }
| boolconst {$value = $boolconst.value; }
| charconst {$value = $charconst.value; }
| intconst {$value = $intconst.value; }
| strconst {$value = $strconst.value; }
| ptrconst {$value = $ptrconst.value; }
;

postfixOperator returns [AstSfxExpr.Oper value]
: /*LBRACKET
| RBRACKET
|*/ CARET {$value = AstSfxExpr.Oper.PTR; } /*
| DOT*/
;

prefixOperator returns [Token value]
: NOT {$value = (LocLogToken)$NOT; }
| PLUS {$value = (LocLogToken)$PLUS; }
| MINUS {$value = (LocLogToken)$MINUS; }
| CARET {$value = (LocLogToken)$CARET; }
//| LESS {$value = (LocLogToken)$LESS; }
//| GREATER {$value = (LocLogToken)$GREATER; }
;

multiplicativeOperators returns [AstBinExpr.Oper value]
: STAR {$value = AstBinExpr.Oper.MUL; }
| DIVIDE {$value = AstBinExpr.Oper.DIV; }
| PERCENT {$value = AstBinExpr.Oper.MOD; }
;

additiveOperators returns [AstBinExpr.Oper value]
: PLUS  {$value = AstBinExpr.Oper.ADD; }
| MINUS {$value = AstBinExpr.Oper.SUB; }
;

relationalOperators returns [AstBinExpr.Oper value]
: EQUALS_R  {$value = AstBinExpr.Oper.EQU; }
| NOTEQUALS_R {$value = AstBinExpr.Oper.NEQ; }
| LESS_OR_EQUALS_R {$value = AstBinExpr.Oper.LEQ; }
| GREATER_OR_EQUALS_R {$value = AstBinExpr.Oper.GEQ; }
| LESS {$value = AstBinExpr.Oper.LTH; }
| GREATER{$value = AstBinExpr.Oper.GTH; }
;

conjuctiveOperators returns [AstBinExpr.Oper value]
: AND {$value = AstBinExpr.Oper.AND; };

disjunctiveOperators returns [AstBinExpr.Oper value]
: OR {$value = AstBinExpr.Oper.OR; };

ptrconst returns [AstAtomExpr value]
: NIL {$value = new AstAtomExpr(loc($NIL), AstAtomExpr.Type.PTR, $NIL.text); };

voidconst returns [AstAtomExpr value]
: NONE {$value = new AstAtomExpr(loc($NONE), AstAtomExpr.Type.VOID, $NONE.text); };

boolconst returns [AstAtomExpr value]
: BOOLVAL {$value = new AstAtomExpr(loc($BOOLVAL), AstAtomExpr.Type.BOOL, $BOOLVAL.text); };

charconst returns [AstAtomExpr value]
: CHARACTER{$value = new AstAtomExpr(loc($CHARACTER), AstAtomExpr.Type.CHAR, $CHARACTER.text); };

intconst returns [AstAtomExpr value]
: NUMBER{$value = new AstAtomExpr(loc($NUMBER), AstAtomExpr.Type.INT, $NUMBER.text); };

strconst returns [AstAtomExpr value]
: STRING{$value = new AstAtomExpr(loc($STRING), AstAtomExpr.Type.STR, $STRING.text);};

name returns  [AstNameExpr value]
: IDENTIFIER {$value = new AstNameExpr(loc($IDENTIFIER), $IDENTIFIER.text); };