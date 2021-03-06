/*
 * Copyright 2013-2015 must-be.org
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
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 01.11.2015
 */
public class CS0692 extends CompilerCheck<DotNetGenericParameter>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetGenericParameter element)
	{
		DotNetGenericParameterListOwner listOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
		if(listOwner != null)
		{
			for(DotNetGenericParameter parameter : listOwner.getGenericParameters())
			{
				if(parameter == element)
				{
					continue;
				}
				if(Comparing.equal(parameter.getName(), element.getName()))
				{
					if(element.getIndex() > parameter.getIndex())
					{
						PsiElement nameIdentifier = element.getNameIdentifier();
						assert nameIdentifier != null;
						return newBuilder(nameIdentifier, formatElement(element));
					}
				}
			}
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
