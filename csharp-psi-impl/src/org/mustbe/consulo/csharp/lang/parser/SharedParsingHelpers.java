/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.parser;

import gnu.trove.THashSet;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokensImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.BitUtil;
import com.intellij.util.NotNullFunction;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class SharedParsingHelpers implements CSharpTokenSets, CSharpTokens, CSharpElements
{
	public static class DocWhitespacesAndCommentsBinder implements WhitespacesAndCommentsBinder
	{
		public static WhitespacesAndCommentsBinder INSTANCE = new DocWhitespacesAndCommentsBinder();

		@Override
		public int getEdgePosition(final List<IElementType> tokens, final boolean atStreamEdge, final TokenTextGetter getter)
		{
			if(tokens.isEmpty())
			{
				return 0;
			}

			for(int i = tokens.size() - 1; i >= 0; i--)
			{
				if(tokens.get(i) == CSharpTokensImpl.LINE_DOC_COMMENT)
				{
					return i;
				}
			}
			return tokens.size();
		}
	}

	public static final TokenSet ourSemicolonSet = TokenSet.create(CSharpTokens.SEMICOLON);

	public static final int NONE = 0;
	public static final int VAR_SUPPORT = 1 << 0;
	public static final int STUB_SUPPORT = 1 << 1;
	public static final int LT_GT_HARD_REQUIRE = 1 << 2;
	public static final int BRACKET_RETURN_BEFORE = 1 << 3;
	public static final int WITHOUT_NULLABLE = 1 << 4;
	public static final int ALLOW_EMPTY_TYPE_ARGUMENTS = 1 << 5;
	public static final int INSIDE_DOC = 1 << 6;

	public static class TypeInfo
	{
		public IElementType nativeElementType;
		public boolean isParameterized;
		public boolean isNullable;
		public boolean isArray;
		public boolean isMultiArray;
		public PsiBuilder.Marker marker;
	}

	protected static void reportErrorUntil(CSharpBuilderWrapper builder, String error, TokenSet originalSet, TokenSet softSet)
	{
		while(!builder.eof())
		{
			if(originalSet.contains(builder.getTokenType()))
			{
				break;
			}

			if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
			{
				builder.enableSoftKeywords(softSet);
				IElementType tokenType = builder.getTokenType();
				builder.disableSoftKeywords(softSet);
				if(softSet.contains(tokenType))
				{
					// remap
					builder.remapBackIfSoft();
					break;
				}
			}
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			mark.error(error);
		}
	}

	protected static void advanceUnexpectedToken(@NotNull PsiBuilder builder)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();
		mark.error("Unexpected token");
	}

	protected static void doneIdentifier(PsiBuilder builder, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();
		mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.IDENTIFIER : CSharpElements.IDENTIFIER);
	}

	protected static void done(PsiBuilder.Marker marker, IElementType elementType)
	{
		marker.done(elementType);

		if(CSharpStubElements.QUALIFIED_MEMBERS.contains(elementType))
		{
			marker.setCustomEdgeTokenBinders(DocWhitespacesAndCommentsBinder.INSTANCE, null);
		}
	}

	protected static boolean parseTypeList(@NotNull CSharpBuilderWrapper builder, int flags)
	{
		return parseTypeList(builder, flags, TokenSet.EMPTY);
	}

	protected static boolean parseTypeList(@NotNull CSharpBuilderWrapper builder, int flags, @NotNull TokenSet nameStopperSet)
	{
		boolean empty = true;
		while(!builder.eof())
		{
			TypeInfo marker = parseType(builder, flags, nameStopperSet);
			if(marker == null)
			{
				if(!empty)
				{
					builder.error("Type expected");
				}
				break;
			}

			empty = false;

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
			}
			else
			{
				break;
			}
		}
		return empty;
	}

	@Nullable
	public static TypeInfo parseType(@NotNull CSharpBuilderWrapper builder)
	{
		return parseType(builder, NONE, TokenSet.EMPTY);
	}

	@Nullable
	public static TypeInfo parseType(@NotNull CSharpBuilderWrapper builder, int flags)
	{
		return parseType(builder, flags, TokenSet.EMPTY);
	}

	@Nullable
	public static TypeInfo parseType(@NotNull CSharpBuilderWrapper builder, int flags, @NotNull TokenSet nameStopperSet)
	{
		TypeInfo typeInfo = parseInnerType(builder, flags, nameStopperSet);
		if(typeInfo == null)
		{
			return null;
		}

		PsiBuilder.Marker marker = typeInfo.marker;

		while(builder.getTokenType() == MUL)
		{
			typeInfo = new TypeInfo();

			marker = marker.precede();

			builder.advanceLexer();

			marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.POINTER_TYPE : CSharpElements.POINTER_TYPE);
		}

		while(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker newMarker = marker.precede();

			if(builder.lookAhead(1) == COMMA)
			{
				builder.advanceLexer();  // advance [

				boolean multi = false;
				while(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
					multi = true;
				}

				expect(builder, RBRACKET, "']' expected");
				newMarker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.ARRAY_TYPE : CSharpElements.ARRAY_TYPE);

				typeInfo = new TypeInfo();
				typeInfo.isArray = true;
				typeInfo.isMultiArray = multi;
				marker = newMarker;
				continue;
			}

			if(builder.lookAhead(1) == RBRACKET)
			{
				builder.advanceLexer();
				builder.advanceLexer();

				newMarker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.ARRAY_TYPE : CSharpElements.ARRAY_TYPE);

				typeInfo = new TypeInfo();
				typeInfo.isArray = true;
				marker = newMarker;
			}
			else
			{
				if(BitUtil.isSet(flags, BRACKET_RETURN_BEFORE))
				{
					newMarker.drop();
					typeInfo.marker = marker;
					return typeInfo;
				}
				else
				{
					builder.advanceLexer();  // advance [

					builder.error("']' expected");
					typeInfo = new TypeInfo();
					typeInfo.isArray = true;
					newMarker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.ARRAY_TYPE : CSharpElements.ARRAY_TYPE);

					marker = newMarker;
				}
			}
		}

		while(builder.getTokenType() == MUL)
		{
			typeInfo = new TypeInfo();

			marker = marker.precede();

			builder.advanceLexer();

			marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.POINTER_TYPE : CSharpElements.POINTER_TYPE);
		}

		if(!typeInfo.isArray)
		{
			if(!BitUtil.isSet(flags, WITHOUT_NULLABLE))
			{
				if(builder.getTokenType() == QUEST)
				{
					typeInfo = new TypeInfo();
					typeInfo.isNullable = true;

					marker = marker.precede();

					builder.advanceLexer();

					marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.NULLABLE_TYPE : CSharpElements.NULLABLE_TYPE);
				}
			}
		}

		typeInfo.marker = marker;
		return typeInfo;
	}

	private static TypeInfo parseInnerType(@NotNull CSharpBuilderWrapper builder, int flags, TokenSet nameStopperSet)
	{
		TypeInfo typeInfo = new TypeInfo();

		PsiBuilder.Marker marker = builder.mark();
		boolean varSupport = BitUtil.isSet(flags, VAR_SUPPORT) && builder.getVersion().isAtLeast(CSharpLanguageVersion._2_0);
		if(varSupport)
		{
			builder.enableSoftKeyword(CSharpSoftTokens.VAR_KEYWORD);
		}
		IElementType tokenType = builder.getTokenType();
		if(varSupport)
		{
			builder.disableSoftKeyword(CSharpSoftTokens.VAR_KEYWORD);
		}

		typeInfo.marker = marker;
		if(CSharpTokenSets.NATIVE_TYPES.contains(tokenType))
		{
			builder.advanceLexer();
			marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.NATIVE_TYPE : CSharpElements.NATIVE_TYPE);

			typeInfo.nativeElementType = tokenType;
		}
		else if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
		{
			ExpressionParsing.ReferenceInfo referenceInfo = ExpressionParsing.parseQualifiedReference(builder, null, flags, nameStopperSet);
			typeInfo.isParameterized = referenceInfo != null && referenceInfo.isParameterized;

			marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.USER_TYPE : CSharpElements.USER_TYPE);
		}
		else
		{
			marker.drop();
			return null;
		}

		return typeInfo;
	}

	protected static boolean parseAttributeList(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		boolean empty = true;
		while(builder.getTokenType() == LBRACKET)
		{
			empty = false;
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();

			builder.enableSoftKeywords(ATTRIBUTE_TARGETS);
			IElementType tokenType = builder.getTokenType();
			builder.disableSoftKeywords(ATTRIBUTE_TARGETS);

			if(builder.lookAhead(1) != COLON)
			{
				builder.remapBackIfSoft();
			}
			else
			{
				if(!ATTRIBUTE_TARGETS.contains(tokenType))
				{
					builder.error("Wrong attribute target");
				}
				builder.advanceLexer(); // target type
				builder.advanceLexer(); // colon
			}

			while(!builder.eof())
			{
				PsiBuilder.Marker attMark = parseAttribute(builder, set, flags);
				if(attMark == null)
				{
					builder.error("Attribute name expected");
					break;
				}

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else if(builder.getTokenType() == RBRACKET)
				{
					break;
				}
			}

			expect(builder, RBRACKET, "']' expected");
			mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.ATTRIBUTE_LIST : CSharpElements.ATTRIBUTE_LIST);
		}
		return empty;
	}

	private static PsiBuilder.Marker parseAttribute(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();
		if(ExpressionParsing.parseQualifiedReference(builder, null, flags, TokenSet.EMPTY) == null)
		{
			mark.drop();
			return null;
		}

		ExpressionParsing.parseArgumentList(builder, true, set);

		mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.ATTRIBUTE : CSharpElements.ATTRIBUTE);
		return mark;
	}

	protected static Pair<PsiBuilder.Marker, ModifierSet> parseModifierList(CSharpBuilderWrapper builder, int flags)
	{
		PsiBuilder.Marker marker = builder.mark();

		Set<IElementType> set = new THashSet<IElementType>();
		while(!builder.eof())
		{
			if(MODIFIERS.contains(builder.getTokenType()))
			{
				set.add(builder.getTokenType());

				builder.advanceLexer();
			}
			else
			{
				break;
			}
		}
		marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.MODIFIER_LIST : CSharpElements.MODIFIER_LIST);
		return new Pair<PsiBuilder.Marker, ModifierSet>(marker, ModifierSet.create(set));
	}

	protected static Pair<PsiBuilder.Marker, ModifierSet> parseModifierListWithAttributes(CSharpBuilderWrapper builder, int flags)
	{
		if(MODIFIERS.contains(builder.getTokenType()))
		{
			return parseModifierList(builder, flags);
		}
		else
		{
			Set<IElementType> set = new THashSet<IElementType>();
			PsiBuilder.Marker marker = builder.mark();
			if(!parseAttributeList(builder, ModifierSet.EMPTY, flags))
			{
				// FIXME [VISTALL] dummy
				set.add(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.ATTRIBUTE : CSharpElements.ATTRIBUTE);
			}
			while(!builder.eof())
			{
				if(MODIFIERS.contains(builder.getTokenType()))
				{
					set.add(builder.getTokenType());

					builder.advanceLexer();
				}
				else
				{
					break;
				}
			}
			marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.MODIFIER_LIST : CSharpElements.MODIFIER_LIST);
			return new Pair<PsiBuilder.Marker, ModifierSet>(marker, ModifierSet.create(set));
		}
	}

	@NotNull
	protected static <T> Pair<PsiBuilder.Marker, T> parseWithSoftElements(NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker, T>> func,
			CSharpBuilderWrapper builderWrapper,
			IElementType... softs)
	{
		return parseWithSoftElements(func, builderWrapper, TokenSet.create(softs));
	}

	@NotNull
	protected static <T> Pair<PsiBuilder.Marker, T> parseWithSoftElements(NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker, T>> func, CSharpBuilderWrapper builderWrapper, TokenSet softs)
	{
		builderWrapper.enableSoftKeywords(softs);
		Pair<PsiBuilder.Marker, T> fun = func.fun(builderWrapper);
		builderWrapper.disableSoftKeywords(softs);
		return fun;
	}

	@Nullable
	public static IElementType exprType(@Nullable final PsiBuilder.Marker marker)
	{
		return marker != null ? ((LighterASTNode) marker).getTokenType() : null;
	}

	public static boolean expect(PsiBuilder builder, IElementType elementType, String message)
	{
		if(builder.getTokenType() == elementType)
		{
			builder.advanceLexer();
			return true;
		}
		else
		{
			if(message != null)
			{
				builder.error(message);
			}
			return false;
		}
	}

	public static boolean expectOrReportIdentifier(PsiBuilder builder, int flags)
	{
		if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
		{
			doneIdentifier(builder, flags);
			return true;
		}
		else
		{
			reportIdentifier(builder, flags);
			return false;
		}
	}

	public static void reportIdentifier(PsiBuilder builder, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.error("Expected identifier");
		mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.IDENTIFIER : CSharpElements.IDENTIFIER);
		mark.setCustomEdgeTokenBinders(WhitespacesBinders.GREEDY_LEFT_BINDER, null);
	}

	protected static boolean expect(PsiBuilder builder, TokenSet tokenSet, String message)
	{
		if(tokenSet.contains(builder.getTokenType()))
		{
			builder.advanceLexer();
			return true;
		}
		else
		{
			if(message != null)
			{
				builder.error(message);
			}
			return false;
		}
	}

	public static void emptyElement(final PsiBuilder builder, final IElementType type)
	{
		builder.mark().done(type);
	}

	protected static boolean doneOneElement(PsiBuilder builder, IElementType elementType, IElementType to, String message)
	{
		PsiBuilder.Marker mark = builder.mark();
		if(expect(builder, elementType, message))
		{
			mark.done(to);
			return true;
		}
		else
		{
			mark.drop();
			return false;
		}
	}

	public static boolean expectGGLL(CSharpBuilderWrapper builder, IElementType elementType, String message)
	{
		if(builder.getTokenTypeGGLL() == elementType)
		{
			builder.advanceLexerGGLL();
			return true;
		}
		else
		{
			if(message != null)
			{
				builder.error(message);
			}
			return false;
		}
	}

	protected static boolean doneOneElementGGLL(CSharpBuilderWrapper builder, IElementType elementType, IElementType to, String message)
	{
		PsiBuilder.Marker mark = builder.mark();
		if(expectGGLL(builder, elementType, message))
		{
			mark.done(to);
			return true;
		}
		else
		{
			mark.drop();
			return false;
		}
	}
}
