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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.12.14
 */
public class CSharpPointerTypeRef extends DotNetTypeRefWithCachedResult implements DotNetPointerTypeRef
{
	private PsiElement myScope;
	private DotNetTypeRef myInnerTypeRef;

	public CSharpPointerTypeRef(@NotNull PsiElement scope, @NotNull DotNetTypeRef innerTypeRef)
	{
		myScope = scope;
		myInnerTypeRef = innerTypeRef;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myScope.getProject()).findType(DotNetTypes.System.Object, myScope.getResolveScope());
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(myScope);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return myInnerTypeRef.toString() + "*";
	}

	@NotNull
	@Override
	public DotNetTypeRef getInnerTypeRef()
	{
		return myInnerTypeRef;
	}
}
