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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 01.03.14
 */
public class CSharpLightIndexMethodDeclaration extends CSharpLightLikeMethodDeclarationWithImplType<CSharpIndexMethodDeclaration> implements CSharpIndexMethodDeclaration
{
	public CSharpLightIndexMethodDeclaration(CSharpIndexMethodDeclaration arrayMethodDeclaration, DotNetParameterList parameterList)
	{
		super(arrayMethodDeclaration, parameterList);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitIndexMethodDeclaration(this);
	}

	@NotNull
	@Override
	public DotNetXXXAccessor[] getAccessors()
	{
		return myOriginal.getAccessors();
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return myOriginal.getMembers();
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return myOriginal.getTypeForImplement();
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}
}
