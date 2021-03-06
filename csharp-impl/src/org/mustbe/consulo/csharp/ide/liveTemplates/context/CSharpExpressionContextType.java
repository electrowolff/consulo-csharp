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

package org.mustbe.consulo.csharp.ide.liveTemplates.context;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class CSharpExpressionContextType extends TemplateContextType
{
	public CSharpExpressionContextType()
	{
		super("CSHARP_EXPRESSION", "C# Expression");
	}

	@Override
	@RequiredReadAction
	public boolean isInContext(@NotNull PsiFile file, int offset)
	{
		PsiElement elementAt = file.findElementAt(offset);

		if(PsiTreeUtil.getParentOfType(elementAt, CSharpUserType.class) != null)
		{
			return false;
		}
		return PsiTreeUtil.getParentOfType(elementAt, DotNetExpression.class) != null;
	}
}
