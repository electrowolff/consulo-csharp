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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SandboxUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public abstract class CSharpElementImpl extends ASTWrapperPsiElement
{
	public CSharpElementImpl(@NotNull ASTNode node)
	{
		super(node);

		if(SandboxUtil.isInsideSandbox())
		{
			String name = getClass().getName();
			if(name.contains("Expression") && !name.contains("Statement") && !(this instanceof CSharpExpressionImpl))
			{
				throw new IllegalArgumentException();
			}
		}
	}

	@NotNull
	@Override
	public GlobalSearchScope getResolveScope()
	{
		return super.getResolveScope();
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			accept((CSharpElementVisitor) visitor);
		}
		else
		{
			super.accept(visitor);
		}
	}

	public abstract void accept(@NotNull CSharpElementVisitor visitor);
}
