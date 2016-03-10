package org.mustbe.consulo.csharp.lang.formatter.processors;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import org.mustbe.consulo.csharp.lang.formatter.CSharpFormattingBlock;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpSpacingProcessor implements CSharpTokens, CSharpElements
{
	private static class OperatorReferenceSpacingBuilder
	{
		private final CommonCodeStyleSettings myCommonSettings;
		private TokenSet myParentSet;
		private final TokenSet myTokenSet;
		private final boolean myCondition;

		OperatorReferenceSpacingBuilder(CommonCodeStyleSettings commonSettings, @NotNull TokenSet parentSet, IElementType[] types, boolean condition)
		{
			myCommonSettings = commonSettings;
			myParentSet = parentSet;
			myTokenSet = TokenSet.create(types);
			myCondition = condition;
		}

		public boolean match(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
		{
			CSharpOperatorReferenceImpl operatorReference = findOperatorReference(child1, child2);
			if(operatorReference != null && myParentSet != TokenSet.EMPTY)
			{
				IElementType elementType = PsiUtilCore.getElementType(operatorReference.getParent());
				if(!myParentSet.contains(elementType))
				{
					return false;
				}
			}
			return operatorReference != null && myTokenSet.contains(operatorReference.getOperatorElementType());
		}

		@Nullable
		private static CSharpOperatorReferenceImpl findOperatorReference(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
		{
			if(child1 != null)
			{
				PsiElement psi = child1.getNode().getPsi();
				if(psi instanceof CSharpOperatorReferenceImpl)
				{
					return (CSharpOperatorReferenceImpl) psi;
				}
			}
			PsiElement psi = child2.getNode().getPsi();
			if(psi instanceof CSharpOperatorReferenceImpl)
			{
				return (CSharpOperatorReferenceImpl) psi;
			}
			return null;
		}

		@NotNull
		public Spacing createSpacing()
		{
			int count = myCondition ? 1 : 0;
			return Spacing.createSpacing(count, count, 0, myCommonSettings.KEEP_LINE_BREAKS, myCommonSettings.KEEP_BLANK_LINES_IN_CODE);
		}
	}

	private static TokenSet ourMultiDeclarationSet = TokenSet.create(CSharpStubElements.FIELD_DECLARATION, CSharpElements.LOCAL_VARIABLE, CSharpStubElements.EVENT_DECLARATION);

	private final CSharpFormattingBlock myParent;
	private final CommonCodeStyleSettings myCommonSettings;

	private SpacingBuilder myBuilder;
	private List<OperatorReferenceSpacingBuilder> myOperatorReferenceSpacingBuilders = new ArrayList<OperatorReferenceSpacingBuilder>();

	public CSharpSpacingProcessor(CSharpFormattingBlock parent, CommonCodeStyleSettings commonSettings, CSharpCodeStyleSettings customSettings)
	{
		myParent = parent;
		myCommonSettings = commonSettings;

		myBuilder = new SpacingBuilder(commonSettings);

		myBuilder.between(CSharpTokens.IF_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_IF_PARENTHESES);
		myBuilder.between(CSharpTokens.FOR_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_FOR_PARENTHESES);
		myBuilder.between(CSharpTokens.FOREACH_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_FOREACH_PARENTHESES);
		myBuilder.between(CSharpTokens.WHILE_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_WHILE_PARENTHESES);
		myBuilder.between(CSharpTokens.SWITCH_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_SWITCH_PARENTHESES);
		myBuilder.between(CSharpTokens.CATCH_KEYWORD, CSharpTokens.LPAR).spaceIf(commonSettings.SPACE_BEFORE_CATCH_PARENTHESES);
		myBuilder.between(CSharpTokens.USING_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_USING_PARENTHESES);
		myBuilder.between(CSharpTokens.LOCK_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_LOCK_PARENTHESES);
		myBuilder.between(CSharpTokens.FIXED_KEYWORD, CSharpTokens.LPAR).spaceIf(customSettings.SPACE_BEFORE_FIXED_PARENTHESES);

		IElementType[] arrayInitializerElementsTypes = {
				CSharpElements.ARRAY_INITIALIZER,
				CSharpElements.IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION,
				CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE
		};
		//myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.ARRAY_INITIALIZER).none();
		//myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION).none();
		//myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE).none();

		//myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.ARRAY_INITIALIZER).none();
		//myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION).none();
		//myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE).none();

		for(IElementType arrayInitializerElementsType : arrayInitializerElementsTypes)
		{
			myBuilder.betweenInside(CSharpTokens.COMMA, CSharpElements.ARRAY_INITIALIZER_SINGLE_VALUE, arrayInitializerElementsType).spaces(1);
			myBuilder.betweenInside(CSharpTokens.COMMA, CSharpElements.ARRAY_INITIALIZER_COMPOSITE_VALUE, arrayInitializerElementsType).spaces(1);
		}

		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, TYPE_DECLARATION), commonSettings.SPACE_BEFORE_CLASS_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, PROPERTY_DECLARATION), customSettings.SPACE_BEFORE_PROPERTY_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, EVENT_DECLARATION), customSettings.SPACE_BEFORE_EVENT_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, ARRAY_METHOD_DECLARATION), customSettings.SPACE_BEFORE_INDEX_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(LBRACE, NAMESPACE_DECLARATION), customSettings.SPACE_BEFORE_NAMESPACE_LBRACE);

		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, METHOD_DECLARATION), commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, CONSTRUCTOR_DECLARATION), commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, CONVERSION_METHOD_DECLARATION), commonSettings.SPACE_BEFORE_METHOD_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, XXX_ACCESSOR), commonSettings.SPACE_BEFORE_METHOD_LBRACE);

		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, SWITCH_STATEMENT), commonSettings.SPACE_BEFORE_SWITCH_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FOR_STATEMENT), commonSettings.SPACE_BEFORE_FOR_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FOREACH_STATEMENT), customSettings.SPACE_BEFORE_FOREACH_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, WHILE_STATEMENT), commonSettings.SPACE_BEFORE_WHILE_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, TRY_STATEMENT), commonSettings.SPACE_BEFORE_TRY_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, CATCH_STATEMENT), commonSettings.SPACE_BEFORE_CATCH_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FINALLY_STATEMENT), commonSettings.SPACE_BEFORE_FINALLY_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, UNSAFE_STATEMENT), customSettings.SPACE_BEFORE_UNSAFE_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, USING_STATEMENT), customSettings.SPACE_BEFORE_USING_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, LOCK_STATEMENT), customSettings.SPACE_BEFORE_LOCK_LBRACE);
		spaceIfNoBlankLines(myBuilder.beforeInside(BLOCK_STATEMENT, FIXED_STATEMENT), customSettings.SPACE_BEFORE_FIXED_LBRACE);

		if(customSettings.KEEP_AUTO_PROPERTY_IN_ONE_LINE)
		{
			spaceIfNoBlankLines(myBuilder.afterInside(LBRACE, PROPERTY_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.afterInside(LBRACE, EVENT_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.afterInside(LBRACE, ARRAY_METHOD_DECLARATION), true);

			spaceIfNoBlankLines(myBuilder.between(XXX_ACCESSOR, XXX_ACCESSOR), true);

			spaceIfNoBlankLines(myBuilder.beforeInside(RBRACE, PROPERTY_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.beforeInside(RBRACE, EVENT_DECLARATION), true);
			spaceIfNoBlankLines(myBuilder.beforeInside(RBRACE, ARRAY_METHOD_DECLARATION), true);
		}

		// between members - one line
		myBuilder.between(CSharpStubElements.FIELD_DECLARATION, CSharpStubElements.FIELD_DECLARATION).blankLines(commonSettings.BLANK_LINES_AROUND_FIELD);
		myBuilder.between(CSharpStubElements.QUALIFIED_MEMBERS, CSharpStubElements.QUALIFIED_MEMBERS).blankLines(commonSettings.BLANK_LINES_AROUND_METHOD);

		myBuilder.afterInside(CSharpTokens.LBRACE, CSharpStubElements.QUALIFIED_MEMBERS).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);
		myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpStubElements.QUALIFIED_MEMBERS).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);

		myBuilder.afterInside(CSharpTokens.LBRACE, CSharpElements.BLOCK_STATEMENT).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);
		myBuilder.beforeInside(CSharpTokens.RBRACE, CSharpElements.BLOCK_STATEMENT).spacing(0, 0, 1, commonSettings.KEEP_LINE_BREAKS, commonSettings.KEEP_BLANK_LINES_BEFORE_RBRACE);

		// call(arg
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES);
		// call[arg
		myBuilder.afterInside(CSharpTokens.LBRACKET, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS);
		// arg)
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES);
		// arg]
		myBuilder.beforeInside(CSharpTokens.RBRACKET, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS);
		// arg, arg
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		// call(
		myBuilder.before(CSharpElements.CALL_ARGUMENT_LIST).spaceIf(commonSettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES);

		// (Type
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.afterInside(CSharpTokens.LPAR, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		// , type CSharpTokens.IDENTIFIER)
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_AFTER_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		myBuilder.beforeInside(CSharpTokens.COMMA, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_COMMA);
		// name)
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);
		myBuilder.beforeInside(CSharpTokens.RPAR, CSharpElements.LAMBDA_PARAMETER_LIST).spaceIf(commonSettings.SPACE_WITHIN_METHOD_PARENTHESES);

		// <Type
		myBuilder.afterInside(CSharpTokens.LT, CSharpElements.TYPE_ARGUMENTS).none();
		myBuilder.afterInside(CSharpTokens.LT, CSharpStubElements.TYPE_ARGUMENTS).none();
		// <Type, Type
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpElements.TYPE_ARGUMENTS).spaceIf(commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS);
		myBuilder.afterInside(CSharpTokens.COMMA, CSharpStubElements.TYPE_ARGUMENTS).spaceIf(commonSettings.SPACE_AFTER_COMMA_IN_TYPE_ARGUMENTS);
		// Type>
		myBuilder.beforeInside(CSharpTokens.GT, CSharpElements.TYPE_ARGUMENTS).none();
		myBuilder.beforeInside(CSharpTokens.GT, CSharpStubElements.TYPE_ARGUMENTS).none();

		// <modifier-list> <type>
		myBuilder.between(CSharpStubElements.MODIFIER_LIST, CSharpStubElements.TYPE_SET).spaces(1);
		// <modifier> <modifier>
		myBuilder.between(CSharpTokenSets.MODIFIERS, CSharpTokenSets.MODIFIERS).spaces(1);
		// [Att]
		// [Att]
		myBuilder.between(CSharpStubElements.ATTRIBUTE_LIST, CSharpStubElements.ATTRIBUTE_LIST).blankLines(0);
		// [Att]
		// <modifier>
		myBuilder.between(CSharpStubElements.ATTRIBUTE_LIST, CSharpTokenSets.MODIFIERS).blankLines(0);

		// name(parameterList)
		myBuilder.between(CSharpTokens.IDENTIFIER, CSharpStubElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_METHOD_PARENTHESES);
		// delegate(parameterList)
		myBuilder.between(CSharpTokens.DELEGATE_KEYWORD, CSharpElements.PARAMETER_LIST).spaceIf(commonSettings.SPACE_BEFORE_METHOD_PARENTHESES);

		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, TYPE_DECLARATION).spaces(1);
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, LOCAL_VARIABLE).spaces(1);
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, FIELD_DECLARATION).spaces(1);
		myBuilder.betweenInside(DOT, CSharpTokens.IDENTIFIER, EVENT_DECLARATION).none();
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, EVENT_DECLARATION).spaces(1);
		myBuilder.betweenInside(DOT, CSharpTokens.IDENTIFIER, PROPERTY_DECLARATION).none();
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, PROPERTY_DECLARATION).spaces(1);
		myBuilder.betweenInside(DOT, CSharpTokens.IDENTIFIER, METHOD_DECLARATION).none();
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, METHOD_DECLARATION).spaces(1);
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, CONSTRUCTOR_DECLARATION).spaces(1);
		myBuilder.beforeInside(THIS_KEYWORD, ARRAY_METHOD_DECLARATION).spaces(1);
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, CSharpElements.PARAMETER).spaces(1);
		myBuilder.beforeInside(CSharpTokens.IDENTIFIER, CSharpStubElements.PARAMETER).spaces(1);

		spaceIfNoBlankLines(myBuilder.afterInside(COLON, CSharpStubElements.EXTENDS_LIST), true);
		spaceIfNoBlankLines(myBuilder.before(CSharpStubElements.EXTENDS_LIST), true);

		// constructor declaration
		spaceIfNoBlankLines(myBuilder.afterInside(COLON, CSharpStubElements.CONSTRUCTOR_DECLARATION), true);
		spaceIfNoBlankLines(myBuilder.beforeInside(COLON, CSharpStubElements.CONSTRUCTOR_DECLARATION), true);

		myBuilder.around(COLONCOLON).none();
		myBuilder.around(DARROW).spaceIf(commonSettings.SPACE_AROUND_LAMBDA_ARROW);
		myBuilder.around(ARROW).none();

		myBuilder.before(CSharpTokens.ELSE_KEYWORD).spaceIf(commonSettings.SPACE_BEFORE_ELSE_KEYWORD);
		myBuilder.betweenInside(CSharpTokens.ELSE_KEYWORD, CSharpElements.BLOCK_STATEMENT, CSharpElements.IF_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_ELSE_LBRACE);

		// need be after else declaration
		myBuilder.beforeInside(BLOCK_STATEMENT, IF_STATEMENT).spaceIf(commonSettings.SPACE_BEFORE_IF_LBRACE);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_EQUALITY_OPERATORS, CSharpTokens.EQEQ, CSharpTokens.NTEQ);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_LOGICAL_OPERATORS, CSharpTokens.ANDAND, CSharpTokens.OROR);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS, CSharpTokens.LT, CSharpTokens.GT, CSharpTokens.LTEQ, CSharpTokens.GTEQ);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS, CSharpTokens.EQ, CSharpTokens.PLUSEQ, CSharpTokens.MINUSEQ, CSharpTokens.MULEQ, CSharpTokens.DIVEQ,
				CSharpTokens.PERCEQ);

		// field, local var, etc initialization
		myBuilder.around(CSharpTokens.EQ).spaceIf(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_SHIFT_OPERATORS, CSharpTokens.GTGT, CSharpTokens.GTGTEQ, CSharpTokens.LTLT, CSharpTokens.LTLTEQ);

		operatorReferenceSpacingWithParent(commonSettings.SPACE_AROUND_UNARY_OPERATOR, TokenSet.create(POSTFIX_EXPRESSION, PREFIX_EXPRESSION), CSharpTokens.PLUS, CSharpTokens.MINUS,
				CSharpTokens.PLUSPLUS, CSharpTokens.MINUSMINUS, CSharpTokens.MUL, CSharpTokens.AND);

		operatorReferenceSpacing(commonSettings.SPACE_AROUND_ADDITIVE_OPERATORS, CSharpTokens.PLUS, CSharpTokens.MINUS);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS, CSharpTokens.MUL, CSharpTokens.DIV, CSharpTokens.PERC);
		operatorReferenceSpacing(commonSettings.SPACE_AROUND_BITWISE_OPERATORS, CSharpTokens.XOR, CSharpTokens.AND, CSharpTokens.OR);

		// doc
		myBuilder.after(CSharpDocTokenType.DOC_LINE_START).spacing(1, 1, 0, true, 0);
	}

	private void spaceIfNoBlankLines(SpacingBuilder.RuleBuilder builder, boolean config)
	{
		int count = config ? 1 : 0;
		builder.spacing(count, count, 0, false, 0);
	}

	public void operatorReferenceSpacing(boolean ifCondition, IElementType... types)
	{
		operatorReferenceSpacingWithParent(ifCondition, TokenSet.EMPTY, types);
	}

	public void operatorReferenceSpacingWithParent(boolean ifCondition, @NotNull TokenSet parents, IElementType... types)
	{
		myOperatorReferenceSpacingBuilders.add(new OperatorReferenceSpacingBuilder(myCommonSettings, parents, types, ifCondition));
	}

	@Nullable
	public Spacing getSpacing(@Nullable ASTBlock child1, @NotNull ASTBlock child2)
	{
		for(OperatorReferenceSpacingBuilder operatorReferenceSpacingBuilder : myOperatorReferenceSpacingBuilders)
		{
			if(operatorReferenceSpacingBuilder.match(child1, child2))
			{
				return operatorReferenceSpacingBuilder.createSpacing();
			}
		}

		IElementType elementType1 = PsiUtilCore.getElementType(child1 == null ? null : child1.getNode());
		IElementType elementType2 = PsiUtilCore.getElementType(child2.getNode());
		if(ourMultiDeclarationSet.contains(elementType1) && elementType1 == elementType2)
		{
			ASTNode commaNode = child1.getNode().findChildByType(CSharpTokens.COMMA);
			if(commaNode != null)
			{
				return Spacing.createSpacing(1, 1, 0, false, 0);
			}
		}
		return myBuilder.getSpacing(myParent, child1, child2);
	}
}
