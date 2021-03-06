package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.lang.formatter.CSharpFormattingBlock;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpStatementAsStatementOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerCompositeValueImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayInitializerSingleValueImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIfStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.formatting.Indent;
import com.intellij.formatting.templateLanguages.BlockWithParent;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.codeInsight.CommentUtilCore;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpIndentProcessor implements CSharpTokens, CSharpElements
{
	private final CSharpFormattingBlock myBlock;
	private final CommonCodeStyleSettings myCodeStyleSettings;

	public CSharpIndentProcessor(CSharpFormattingBlock block, CommonCodeStyleSettings codeStyleSettings, CSharpCodeStyleSettings customSettings)
	{
		myBlock = block;
		myCodeStyleSettings = codeStyleSettings;
	}

	@RequiredReadAction
	public Indent getIndent()
	{
		ASTNode node = myBlock.getNode();
		PsiElement psi = node.getPsi();
		PsiElement parent = psi.getParent();
		if(parent instanceof PsiFile)
		{
			return Indent.getNoneIndent();
		}

		final IElementType elementType = node.getElementType();
		if(elementType == NAMESPACE_DECLARATION ||
				elementType == TYPE_DECLARATION ||
				elementType == METHOD_DECLARATION ||
				elementType == CONVERSION_METHOD_DECLARATION ||
				elementType == FIELD_DECLARATION ||
				elementType == NAMED_FIELD_OR_PROPERTY_SET ||
				elementType == ARRAY_METHOD_DECLARATION ||
				elementType == PROPERTY_DECLARATION ||
				elementType == XXX_ACCESSOR ||
				elementType == EVENT_DECLARATION ||
				elementType == ENUM_CONSTANT_DECLARATION ||
				elementType == USING_TYPE_STATEMENT ||
				elementType == USING_NAMESPACE_STATEMENT ||
				elementType == TYPE_DEF_STATEMENT ||
				elementType == CONSTRUCTOR_DECLARATION)
		{
			return Indent.getNormalIndent();
		}
		else if(parent instanceof CSharpArrayInitializerSingleValueImpl)
		{
			return Indent.getNormalIndent();
		}
		else if(elementType == LBRACE || elementType == RBRACE)
		{
			if(parent instanceof CSharpArrayInitializerCompositeValueImpl)
			{
				return Indent.getNormalIndent();
			}
			return Indent.getNoneIndent();
		}
		else if(elementType == CSharpTokens.PREPROCESSOR_DIRECTIVE)
		{
			return Indent.getAbsoluteNoneIndent();
		}
		else if(elementType == DICTIONARY_INITIALIZER)
		{
			return Indent.getNormalIndent();
		}
		else if(CommentUtilCore.isComment(node))
		{
			return Indent.getNormalIndent();
		}
		else if(elementType == CSharpElements.MODIFIER_LIST || elementType == CSharpStubElements.MODIFIER_LIST)
		{
			return Indent.getNoneIndent();
		}
	/*	else if(elementType == CSharpParserDefinition.FILE_ELEMENT_TYPE)
		{
			return Indent.getNoneIndent();
		}  */
		else if(elementType == CSharpStubElements.FILE)
		{
			return Indent.getNoneIndent();
		}
		/*else if(elementType == MACRO_BLOCK_START || elementType == MACRO_BLOCK_STOP)
		{
			PsiElement psi = getNode().getPsi();
			if(psi.getParent() instanceof CSharpMacroBlockImpl)
			{
				return Indent.getNoneIndent();
			}
			return Indent.getNormalIndent();
		} */
		else
		{
			if(CSharpFormattingUtil.wantContinuationIndent(psi))
			{
				return Indent.getContinuationIndent();
			}

			if(psi instanceof CSharpBlockStatementImpl)
			{
				BlockWithParent parentBlock = myBlock.getParent();
				if(parentBlock != null && ((CSharpFormattingBlock) parentBlock).getElementType() == CSharpElements.SWITCH_LABEL_STATEMENT)
				{
					return Indent.getNoneIndent();
				}

				if(parent instanceof CSharpBlockStatementImpl)
				{
					return Indent.getNormalIndent();
				}
				return Indent.getNoneIndent();
			}

			if(psi instanceof DotNetStatement && parent instanceof CSharpIfStatementImpl)
			{
				if(psi instanceof CSharpIfStatementImpl)
				{
					return Indent.getNoneIndent();
				}
				return Indent.getNormalIndent();
			}

			if(parent instanceof CSharpStatementAsStatementOwner)
			{
				DotNetStatement childStatement = ((CSharpStatementAsStatementOwner) parent).getChildStatement();
				if(childStatement == psi)
				{
					return Indent.getNormalIndent();
				}
			}

			if(parent instanceof CSharpBlockStatementImpl)
			{
				return Indent.getNormalIndent();
			}

			return Indent.getNoneIndent();
		}
	}

	@RequiredReadAction
	@NotNull
	public Indent getChildIndent()
	{
		IElementType elementType = myBlock.getNode().getElementType();
		if(elementType == CSharpStubElements.FILE ||
				elementType == CSharpElements.MODIFIER_LIST ||
				elementType == CSharpElements.IF_STATEMENT ||
				elementType == CSharpElements.TRY_STATEMENT ||
				elementType == CSharpStubElements.MODIFIER_LIST)
		{
			return Indent.getNoneIndent();
		}

		return Indent.getNormalIndent();
	}
}
