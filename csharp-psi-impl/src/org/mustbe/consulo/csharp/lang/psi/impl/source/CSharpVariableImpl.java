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
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.PsiElement;
import consulo.csharp.lang.psi.impl.source.CSharpTypeRefCacher;

/**
 * @author VISTALL
 * @since 06.01.14.
 */
public abstract class CSharpVariableImpl extends CSharpMemberImpl implements DotNetVariable
{
	private static final CSharpTypeRefCacher<CSharpVariableImpl> ourCacheSystem = new CSharpTypeRefCacher<CSharpVariableImpl>(true)
	{
		@RequiredReadAction
		@NotNull
		@Override
		protected DotNetTypeRef toTypeRefImpl(CSharpVariableImpl element, boolean resolveFromParentOrInitializer)
		{
			return element.toTypeRefImpl(resolveFromParentOrInitializer);
		}
	};

	public CSharpVariableImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return ourCacheSystem.toTypeRef(this, resolveFromInitializer);
	}

	@RequiredReadAction
	@NotNull
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		if(type == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		DotNetTypeRef runtimeType = type.toTypeRef();
		if(resolveFromInitializer && runtimeType == DotNetTypeRef.AUTO_TYPE)
		{
			final DotNetExpression initializer = getInitializer();
			if(initializer == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}

			DotNetTypeRef resolvedTypeRef = RecursionManager.doPreventingRecursion(this, false, new NullableComputable<DotNetTypeRef>()
			{
				@Nullable
				@Override
				public DotNetTypeRef compute()
				{
					return initializer.toTypeRef(true);
				}
			});
			return resolvedTypeRef == null ? DotNetTypeRef.AUTO_TYPE : resolvedTypeRef;
		}
		else
		{
			return runtimeType;
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return false;
	}
}
