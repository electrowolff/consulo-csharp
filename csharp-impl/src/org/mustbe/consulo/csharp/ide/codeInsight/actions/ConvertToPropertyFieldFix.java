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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredWriteAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class ConvertToPropertyFieldFix extends PsiElementBaseIntentionAction
{
	public ConvertToPropertyFieldFix()
	{
		setText("Convert to property");
	}

	@Override
	@RequiredWriteAction
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
	{
		DotNetModifierListOwner owner = CSharpIntentionUtil.findOwner(element);
		if(!(owner instanceof CSharpFieldDeclaration))
		{
			return;
		}

		StringBuilder builder = new StringBuilder();
		DotNetModifierList modifierList = owner.getModifierList();
		assert modifierList != null;
		String modifierText = modifierList.getText();
		if(!StringUtil.isEmpty(modifierText))
		{
			builder.append(modifierText).append(" ");
		}

		DotNetType type = ((CSharpFieldDeclaration) owner).getType();
		if(type != null)
		{
			builder.append(type.getText()).append(" ");
		}
		builder.append(((CSharpFieldDeclaration) owner).getName());
		builder.append(" { get; set; }");

		if(CSharpModuleUtil.findLanguageVersion(element).isAtLeast(CSharpLanguageVersion._6_0))
		{
			DotNetExpression initializer = ((CSharpFieldDeclaration) owner).getInitializer();
			if(initializer != null)
			{
				builder.append(" = ").append(initializer.getText());
			}
		}

		CSharpPropertyDeclaration property = CSharpFileFactory.createProperty(project, builder.toString());
		if(property == null)
		{
			return;
		}

		owner.replace(property);
	}

	@Override
	@RequiredDispatchThread
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		DotNetModifierListOwner owner = CSharpIntentionUtil.findOwner(element);
		return owner != null && owner instanceof CSharpFieldDeclaration && owner.isWritable();
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
