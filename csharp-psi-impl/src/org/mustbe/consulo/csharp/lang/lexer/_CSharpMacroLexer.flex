package org.mustbe.consulo.csharp.lang.lexer;

import java.util.*;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;

%%

%public
%class CSharpMacroLexer
%extends LexerBase
%unicode
%function advanceImpl
%type IElementType
%eof{  return;
%eof}

WHITE_SPACE=[ \n\r\t\f]+

IDENTIFIER=[:jletter:] [:jletterdigit:]*
%%


<YYINITIAL>
{
	"#"                  { return CSharpMacroTokens.SHARP; }

	"("                  { return CSharpMacroTokens.LPAR; }

	")"                  { return CSharpMacroTokens.RPAR; }

	"!"                  { return CSharpMacroTokens.EXCL; }

	"&&"                 { return CSharpMacroTokens.ANDAND; }

	"||"                 { return CSharpMacroTokens.OROR; }

	{IDENTIFIER}         { return CSharpMacroTokens.IDENTIFIER; }

	{WHITE_SPACE}        { return CSharpMacroTokens.WHITE_SPACE; }

	[^]                  { return CSharpMacroTokens.BAD_CHARACTER; }
}
