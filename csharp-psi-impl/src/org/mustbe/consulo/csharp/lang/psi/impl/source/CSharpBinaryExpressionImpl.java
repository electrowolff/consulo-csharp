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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpConstantBaseTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpBinaryExpressionImpl extends CSharpExpressionWithOperatorImpl implements DotNetExpression, CSharpCallArgumentListOwner
{
	private static class BinaryTypeRef extends CSharpConstantBaseTypeRef
	{
		BinaryTypeRef(CSharpConstantExpressionImpl leftExpression, DotNetTypeRef delegate)
		{
			super(leftExpression, delegate);
		}
	}

	public CSharpBinaryExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitBinaryExpression(this);
	}

	@NotNull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetTypeRef delegate = super.toTypeRefImpl(resolveFromParent);

		IElementType operatorElementType = getOperatorElement().getOperatorElementType();
		if(operatorElementType == CSharpTokenSets.LTLT || operatorElementType == CSharpTokenSets.GTGT)
		{
			DotNetExpression leftExpression = getLeftExpression();
			if(leftExpression instanceof CSharpConstantExpressionImpl)
			{
				return new BinaryTypeRef((CSharpConstantExpressionImpl) leftExpression, delegate);
			}
			else if(leftExpression instanceof CSharpPrefixExpressionImpl)
			{
				DotNetExpression expression = ((CSharpPrefixExpressionImpl) leftExpression).getExpression();
				if(expression instanceof CSharpConstantExpressionImpl)
				{
					return new CSharpPrefixExpressionImpl.PrefixTypeRef((CSharpPrefixExpressionImpl)leftExpression, (CSharpConstantExpressionImpl)expression, delegate);
				}
			}
		}
		return delegate;
	}

	@Nullable
	public DotNetExpression getLeftExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getRightExpression()
	{
		PsiElement operatorElement = getOperatorElement();
		PsiElement nextSibling = operatorElement.getNextSibling();
		while(nextSibling != null)
		{
			if(nextSibling instanceof DotNetExpression)
			{
				return (DotNetExpression) nextSibling;
			}
			nextSibling = nextSibling.getNextSibling();
		}
		return null;
	}
}
