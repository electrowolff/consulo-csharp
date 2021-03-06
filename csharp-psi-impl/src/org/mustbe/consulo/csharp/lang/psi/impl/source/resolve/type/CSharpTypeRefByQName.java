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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpTypeRefByQName extends DotNetTypeRefWithCachedResult
{
	@NotNull
	private final Project myProject;
	@NotNull
	private final GlobalSearchScope mySearchScope;
	@NotNull
	private final String myQualifiedName;

	public CSharpTypeRefByQName(@NotNull Project project, @NotNull GlobalSearchScope searchScope, @NotNull String qualifiedName)
	{
		myProject = project;
		mySearchScope = searchScope;
		myQualifiedName = qualifiedName;
	}

	@RequiredReadAction
	public CSharpTypeRefByQName(@NotNull PsiElement scope, @NotNull String qualifiedName)
	{
		this(scope.getProject(), scope.getResolveScope(), qualifiedName);
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		if(DumbService.isDumb(myProject))
		{
			return DotNetTypeResolveResult.EMPTY;
		}

		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myProject).findType(myQualifiedName, mySearchScope, CSharpTransform.INSTANCE);

		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}

		return new CSharpUserTypeRef.Result<DotNetTypeDeclaration>(type, DotNetGenericExtractor.EMPTY);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return myQualifiedName;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Delegate)
		{
			obj = ((Delegate) obj).getDelegate();
		}
		return obj instanceof CSharpTypeRefByQName && ((CSharpTypeRefByQName) obj).myQualifiedName.equals(myQualifiedName);
	}
}
