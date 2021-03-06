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
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIndexAccessExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOutRefWrapExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 13-May-16
 *
 * @see CS0206
 */
public class CS1510 extends CompilerCheck<CSharpOutRefWrapExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpOutRefWrapExpressionImpl element)
	{
		DotNetExpression innerExpression = element.getInnerExpression();
		if(innerExpression == null || innerExpression instanceof CSharpIndexAccessExpressionImpl)  // ignore index access see CS0206
		{
			return null;
		}

		if(!(innerExpression instanceof CSharpReferenceExpression))
		{
			return newBuilder(innerExpression);
		}

		PsiElement psiElement = ((CSharpReferenceExpression) innerExpression).resolve();
		// reference already highlighted by error
		if(psiElement == null)
		{
			return null;
		}
		if(!(psiElement instanceof DotNetVariable) || ((DotNetVariable) psiElement).isConstant() || ((DotNetVariable) psiElement).hasModifier(CSharpModifier.READONLY))
		{
			return newBuilder(innerExpression);
		}
		return null;
	}
}
