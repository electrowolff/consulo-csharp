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

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameterList;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLambdaExpressionImpl extends CSharpExpressionImpl implements CSharpAnonymousMethodExpression
{
	public CSharpLambdaExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return getModifierList().hasModifier(modifier);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetModifierList getModifierList()
	{
		return CachedValuesManager.getManager(getProject()).createCachedValue(new CachedValueProvider<DotNetModifierList>()
		{
			@Nullable
			@Override
			public Result<DotNetModifierList> compute()
			{
				return Result.<DotNetModifierList>create(new CSharpAnonymousModifierListImpl(CSharpLambdaExpressionImpl.this), CSharpLambdaExpressionImpl.this);
			}
		}, false).getValue();
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		DotNetExpression singleExpression = findChildByClass(DotNetExpression.class);
		if(singleExpression != null)
		{
			return singleExpression;
		}
		return findChildByClass(DotNetStatement.class);
	}

	@NotNull
	public PsiElement getDArrow()
	{
		return findNotNullChildByType(CSharpTokens.DARROW);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLambdaExpression(this);
	}

	@Nullable
	public CSharpLambdaParameterList getParameterList()
	{
		return findChildByClass(CSharpLambdaParameterList.class);
	}

	@NotNull
	public CSharpLambdaParameter[] getParameters()
	{
		CSharpLambdaParameterList parameterList = getParameterList();
		return parameterList == null ? CSharpLambdaParameterImpl.EMPTY_ARRAY : parameterList.getParameters();
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place)
	{
		for(CSharpLambdaParameter parameter : getParameters())
		{
			if(!processor.execute(parameter, state))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return new CSharpLambdaTypeRef(this, null, getParameterInfos(resolveFromParent), resolveFromParent ? getReturnTypeRef() : DotNetTypeRef.AUTO_TYPE);
	}

	@NotNull
	public DotNetTypeRef toTypeRefForInference()
	{
		return new CSharpLambdaTypeRef(this, null, getParameterInfos(true), findPossibleReturnTypeRef());
	}

	@NotNull
	@RequiredReadAction
	private DotNetTypeRef findPossibleReturnTypeRef()
	{
		PsiElement codeBlock = getCodeBlock();
		if(codeBlock instanceof DotNetExpression)
		{
			return ((DotNetExpression) codeBlock).toTypeRef(false);
		}

		if(codeBlock == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		final List<DotNetTypeRef> typeRefs = new ArrayList<DotNetTypeRef>();
		codeBlock.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitAnonymMethodExpression(CSharpDelegateExpressionImpl method)
			{
				// dont need check return inside anonym
			}

			@Override
			public void visitLambdaExpression(CSharpLambdaExpressionImpl expression)
			{
				// dont need check return inside lambda
			}

			@Override
			public void visitYieldStatement(CSharpYieldStatementImpl statement)
			{
				//FIXME [VISTALL] what we need to do?
			}

			@Override
			@RequiredReadAction
			public void visitReturnStatement(CSharpReturnStatementImpl statement)
			{
				CSharpImplicitReturnModel implicitReturnModel = CSharpImplicitReturnModel.getImplicitReturnModel(statement, CSharpLambdaExpressionImpl.this);

				DotNetExpression expression = statement.getExpression();
				DotNetTypeRef expectedTypeRef;

				expectedTypeRef = expression == null ? new CSharpTypeRefByQName(statement, DotNetTypes.System.Void) : expression.toTypeRef(false);
				if(expectedTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return;
				}

				if(implicitReturnModel == CSharpImplicitReturnModel.None)
				{
					typeRefs.add(expectedTypeRef);
				}
				else
				{
					if(DotNetTypeRefUtil.isVmQNameEqual(expectedTypeRef, statement, DotNetTypes.System.Void))
					{
						typeRefs.add(new CSharpTypeRefByQName(statement, implicitReturnModel.getNoGenericTypeVmQName()));
					}
					else
					{
						typeRefs.add(new CSharpGenericWrapperTypeRef(new CSharpTypeRefByQName(statement, implicitReturnModel.getGenericVmQName()), expectedTypeRef));
					}
				}
			}
		});

		if(typeRefs.isEmpty())
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.Void);
		}
		return typeRefs.get(0);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return getParameterInfos(false);
	}

	@NotNull
	public CSharpSimpleParameterInfo[] getParameterInfos(boolean resolveFromParent)
	{
		CSharpLambdaParameter[] parameters = getParameters();
		CSharpSimpleParameterInfo[] parameterInfos = new CSharpSimpleParameterInfo[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			CSharpLambdaParameter parameter = parameters[i];
			parameterInfos[i] = new CSharpSimpleParameterInfo(i, parameter.getName(), parameter, parameter.toTypeRef(resolveFromParent));
		}
		return parameterInfos;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		CSharpLambdaResolveResult type = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(this);
		if(type == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		return type.getReturnTypeRef();
	}
}
