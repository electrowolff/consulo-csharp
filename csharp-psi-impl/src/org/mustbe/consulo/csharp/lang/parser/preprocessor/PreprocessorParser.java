/*
 * Copyright 2013-2016 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.csharp.lang.parser.preprocessor;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.lexer.CSharpMacroLexer;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 02.03.2016
 */
public class PreprocessorParser
{
	public enum State
	{
		NONE,
		SHARP,
		DIRECTIVE,
		VALUE
	}

	public enum Directive
	{
		DEFINE,
		UNDEF,
		IF,
		ELIF,
		ELSE,
		ENDIF,
		REGION,
		ENDREGION
	}

	public static void main(String[] args)
	{
		System.out.println(parse("#define TEST && TEST2"));
	}

	@Nullable
	public static PreprocessorDirective parse(String text)
	{
		CSharpMacroLexer lexer = new CSharpMacroLexer();
		lexer.start(text);

		State state = State.NONE;
		Directive directive = null;
		IElementType elementType = null;
		String value = "";

		loop:
		while((elementType = lexer.getTokenType()) != null)
		{
			switch(state)
			{
				case NONE:
					if(elementType == CSharpMacroTokens.SHARP)
					{
						state = State.SHARP;
					}
					break;
				case SHARP:
					if(isToken(lexer, "define"))
					{
						state = State.DIRECTIVE;
						directive = Directive.DEFINE;
					}
					else if( isToken(lexer, "undef"))
					{
						state = State.DIRECTIVE;
						directive =  Directive.UNDEF;
					}
					else if(isToken(lexer, "if"))
					{
						state = State.DIRECTIVE;
						directive = Directive.IF;
					}
					else if(isToken(lexer, "elif"))
					{
						state = State.DIRECTIVE;
						directive =  Directive.ELIF;
					}
					else if(isToken(lexer, "endif"))
					{
						state = State.DIRECTIVE;
						directive = Directive.ENDIF;
					}
					else if(isToken(lexer, "region"))
					{
						state = State.DIRECTIVE;
						directive = Directive.REGION;
					}
					else if(isToken(lexer, "endregion"))
					{
						state = State.DIRECTIVE;
						directive = Directive.ENDREGION;
					}
					else if(isToken(lexer, "else"))
					{
						state = State.DIRECTIVE;
						directive = Directive.ELSE;
					}
					else
					{
						break loop;
					}
					break;
				case DIRECTIVE:
					switch(directive)
					{
						case DEFINE:
						case UNDEF:
							if(elementType == CSharpMacroTokens.WHITE_SPACE)
							{
							}
							else if(elementType == CSharpMacroTokens.IDENTIFIER)
							{
								value = lexer.getTokenText();
								state = State.VALUE;
							}
							else
							{
								break loop;
							}
							break;
						case ELIF:
						case IF:
						case REGION:
							value += lexer.getTokenText();
							break;
						default:
							break loop;
					}
					break;
				default:
					break loop;
			}

			lexer.advance();
		}

		if(directive != null)
		{
			switch(directive)
			{
				case DEFINE:
				case UNDEF:
					if(StringUtil.isEmpty(value))
					{
						return null;
					}
					return new DefinePreprocessorDirective(value, directive == Directive.UNDEF);
				case IF:
				case ELIF:
					return new IfPreprocessorDirective(value, directive == Directive.ELIF);
				case REGION:
					return new RegionPreprocessorDirective(text);
				case ELSE:
					return ElsePreprocessorDirective.INSTANCE;
				case ENDIF:
					return EndIfPreprocessorDirective.INSTANCE;
				case ENDREGION:
					return EndRegionPreprocessorDirective.INSTANCE;
			}
		}

		return null;
	}

	private static boolean isToken(Lexer lexer, String keywordName)
	{
		IElementType tokenType = lexer.getTokenType();
		if(tokenType == CSharpMacroTokens.IDENTIFIER)
		{
			return Comparing.equal(keywordName, lexer.getTokenSequence());
		}
		return false;
	}
}
