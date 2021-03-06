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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;

/**
 * @author VISTALL
 * @since 08.01.2016
 */
public class CS0509 extends CompilerCheck<DotNetType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetType element)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof DotNetTypeList && PsiUtilCore.getElementType(parent) == CSharpElements.EXTENDS_LIST)
		{
			PsiElement superParent = parent.getParent();
			if(superParent instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) superParent).isEnum())
			{
				return null;
			}
			PsiElement psiElement = element.toTypeRef().resolve().getElement();
			if(psiElement instanceof CSharpTypeDeclaration)
			{
				if(((CSharpTypeDeclaration) psiElement).hasModifier(DotNetModifier.SEALED))
				{
					return newBuilder(element, formatElement(parent.getParent()), ((CSharpTypeDeclaration) psiElement).getVmQName());
				}
			}
		}
		return null;
	}
}
