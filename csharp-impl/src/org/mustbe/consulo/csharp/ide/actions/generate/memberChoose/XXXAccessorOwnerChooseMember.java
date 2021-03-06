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

package org.mustbe.consulo.csharp.ide.actions.generate.memberChoose;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpXXXAccessorOwner;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.psi.PsiElement;
import com.intellij.util.PairConsumer;

/**
 * @author VISTALL
 * @since 02.04.2016
 */
public class XXXAccessorOwnerChooseMember extends ImplementMemberChooseObject<CSharpXXXAccessorOwner>
{
	public XXXAccessorOwnerChooseMember(CSharpXXXAccessorOwner declaration,
			PairConsumer<PsiElement, StringBuilder> additionalModifiersAppender,
			PairConsumer<PsiElement, StringBuilder> returnAppender,
			boolean canGenerateBlock)
	{
		super(declaration, additionalModifiersAppender, returnAppender, canGenerateBlock);
	}

	@RequiredReadAction
	@NotNull
	@Override
	@RequiredDispatchThread
	public String getPresentationText()
	{
		if(myDeclaration instanceof CSharpPropertyDeclaration)
		{
			return CSharpElementPresentationUtil.formatProperty((DotNetPropertyDeclaration) myDeclaration, 0);
		}
		else if(myDeclaration instanceof CSharpIndexMethodDeclaration)
		{
			return CSharpElementPresentationUtil.formatMethod((CSharpIndexMethodDeclaration) myDeclaration, CSharpElementPresentationUtil.METHOD_WITH_RETURN_TYPE | CSharpElementPresentationUtil
					.METHOD_PARAMETER_NAME);
		}
		throw new UnsupportedOperationException(myDeclaration.getClass().getSimpleName() + " is not supported");
	}

	@Override
	@RequiredDispatchThread
	public String getText()
	{
		StringBuilder builder = new StringBuilder();
		CSharpAccessModifier modifier = CSharpAccessModifier.findModifier(myDeclaration);
		boolean canGenerateCodeBlock = myCanGenerateBlock;
		if(modifier != CSharpAccessModifier.NONE && canGenerateCodeBlock)
		{
			builder.append(modifier.getPresentableText()).append(" ");
		}

		builder.append(getPresentationText());
		builder.append(" {\n");
		for(DotNetXXXAccessor accessor : myDeclaration.getAccessors())
		{
			DotNetXXXAccessor.Kind accessorKind = accessor.getAccessorKind();
			if(accessorKind == null)
			{
				continue;
			}
			builder.append(accessorKind.name().toLowerCase(Locale.US));
			if(myCanGenerateBlock)
			{
				builder.append(" {\n");
				myReturnAppender.consume(accessor, builder);
				builder.append("}");
			}
			else
			{
				builder.append(";");
			}
		}
		builder.append("}");
		return builder.toString();
	}
}
