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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpNativeType;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 11.06.14
 */
public class CS1980 extends CompilerCheck<CSharpNativeType>
{
	private static final String ourCheckType = "System.Runtime.CompilerServices.DynamicAttribute";
	private static final String ourCheckType2 = "Microsoft.CSharp.RuntimeBinder.Binder";

	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpNativeType element)
	{
		PsiElement typeElement = element.getTypeElement();
		if(element.getTypeElementType() == CSharpTokens.DYNAMIC_KEYWORD)
		{
			DotNetSimpleModuleExtension<?> extension = highlightContext.getDotNetModuleExtension();
			if(extension == null)
			{
				return null;
			}

			// getAvailableSystemLibraries is too expensive
			// Map<String, String> availableSystemLibraries = extension.getAvailableSystemLibraries();
			Map<String, String> availableSystemLibraries = Collections.singletonMap("System.Core", "1.0");

			if(noRuntimeType(element))
			{
				return availableSystemLibraries.containsKey("System.Core") ? newBuilder(typeElement) : newBuilderImpl(CS0518.class, element);
			}
		}
		return null;
	}

	@RequiredReadAction
	private boolean noRuntimeType(@NotNull CSharpNativeType element)
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(element.getProject()).findType(ourCheckType, element.getResolveScope());
		if(type == null)
		{
			if(DotNetPsiSearcher.getInstance(element.getProject()).findType(ourCheckType2, element.getResolveScope()) == null)
			{
				return true;
			}
		}
		return false;
	}
}
