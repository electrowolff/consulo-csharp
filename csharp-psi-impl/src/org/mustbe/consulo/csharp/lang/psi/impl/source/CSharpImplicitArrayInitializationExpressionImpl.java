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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpFastImplicitTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetArrayTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CSharpImplicitArrayInitializationExpressionImpl extends CSharpExpressionImpl implements DotNetExpression, CSharpArrayInitializerOwner
{
	private static class ImplicitArrayInitializationTypeRef extends DotNetTypeRefWithCachedResult implements CSharpFastImplicitTypeRef
	{
		@RequiredReadAction
		@NotNull
		@Override
		public String toString()
		{
			return "{...}";
		}

		@RequiredReadAction
		@NotNull
		@Override
		protected DotNetTypeResolveResult resolveResult()
		{
			return DotNetTypeResolveResult.EMPTY;
		}

		@RequiredReadAction
		@Nullable
		@Override
		public DotNetTypeRef doMirror(@NotNull DotNetTypeRef another, PsiElement scope)
		{
			if(another instanceof DotNetArrayTypeRef)
			{
				return another;
			}
			return null;
		}

		@Override
		public boolean isConversion()
		{
			return true;
		}
	}

	public CSharpImplicitArrayInitializationExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitImplicitArrayInitializationExpression(this);
	}

	@NotNull
	public DotNetExpression[] getExpressions()
	{
		return findChildrenByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return new ImplicitArrayInitializationTypeRef();
	}

	@Nullable
	@Override
	public CSharpArrayInitializerImpl getArrayInitializer()
	{
		return findChildByClass(CSharpArrayInitializerImpl.class);
	}
}
