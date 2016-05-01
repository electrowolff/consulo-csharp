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

package org.mustbe.consulo.csharp.ide.completion.filter;

import org.mustbe.consulo.csharp.ide.completion.CSharpCompletionUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.filters.ElementFilterBase;

/**
 * @author VISTALL
 * @since 01.05.2016
 */
public class TypeLikeFilter extends ElementFilterBase<DotNetNamedElement>
{
	public TypeLikeFilter()
	{
		super(DotNetNamedElement.class);
	}

	@Override
	protected boolean isElementAcceptable(DotNetNamedElement element, PsiElement context)
	{
		return CSharpCompletionUtil.isTypeLikeElementWithNamespace(element);
	}
}
