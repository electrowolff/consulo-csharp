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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import consulo.lombok.annotations.ProjectService;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceAsElementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpPartialElementManager;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeByVmQNameIndex;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.impl.IndexBasedDotNetPsiSearcher;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author VISTALL
 * @since 13.07.14
 */
@ProjectService
public class CSharpPsiSearcher extends IndexBasedDotNetPsiSearcher
{
	public CSharpPsiSearcher(Project project)
	{
		super(project);
	}

	@NotNull
	@Override
	protected DotNetNamespaceAsElement createNamespace(@NotNull String indexKey, @NotNull String qName)
	{
		return new CSharpNamespaceAsElementImpl(myProject, indexKey, qName, this);
	}

	@NotNull
	@Override
	public StubIndexKey<String, DotNetQualifiedElement> getElementByQNameIndexKey()
	{
		return CSharpIndexKeys.MEMBER_BY_NAMESPACE_QNAME_INDEX;
	}

	@NotNull
	@Override
	public StubIndexKey<String, DotNetQualifiedElement> getNamespaceIndexKey()
	{
		return CSharpIndexKeys.MEMBER_BY_ALL_NAMESPACE_QNAME_INDEX;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public Collection<? extends DotNetTypeDeclaration> findTypesImpl(@NotNull String vmQName, @NotNull GlobalSearchScope scope)
	{
		if(DumbService.isDumb(myProject))
		{
			return Collections.emptyList();
		}

		Collection<DotNetTypeDeclaration> declarations = TypeByVmQNameIndex.getInstance().get(vmQName, myProject, scope);

		List<CSharpTypeDeclaration> partials = Collections.emptyList();
		for(DotNetTypeDeclaration element : declarations)
		{
			if(!(element instanceof CSharpTypeDeclaration))
			{
				continue;
			}

			if(element.hasModifier(CSharpModifier.PARTIAL))
			{
				if(partials.isEmpty())
				{
					partials = new ArrayList<CSharpTypeDeclaration>(2);
				}

				partials.add((CSharpTypeDeclaration) element);
			}
		}

		if(partials.isEmpty())
		{
			return declarations;
		}

		if(partials.size() == 1)
		{
			return declarations;
		}
		else
		{
			CSharpTypeDeclaration typeDeclaration = CSharpPartialElementManager.getInstance(myProject).getOrCreateCompositeType(scope, vmQName, partials);

			List<DotNetTypeDeclaration> anotherList = new ArrayList<DotNetTypeDeclaration>(declarations.size() - partials.size() + 1);
			anotherList.add(typeDeclaration);

			declarations.removeAll(partials);
			anotherList.addAll(declarations);

			return anotherList;
		}
	}
}
