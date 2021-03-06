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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpIdentifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public class CSharpIntentionUtil
{
	@Nullable
	@RequiredReadAction
	public static DotNetModifierListOwner findOwner(@NotNull PsiElement element)
	{
		PsiElement parent = element.getParent();
		if(element instanceof CSharpIdentifier && parent instanceof DotNetModifierListOwner)
		{
			return (DotNetModifierListOwner) parent;
		}

		if(parent instanceof CSharpIdentifier && parent.getParent() instanceof DotNetModifierListOwner)
		{
			return (DotNetModifierListOwner) parent.getParent();
		}

		PsiElement prevSibling = element.getPrevSibling();
		if(prevSibling instanceof CSharpIdentifier && prevSibling.getParent() instanceof DotNetModifierListOwner)
		{
			return (DotNetModifierListOwner) prevSibling.getParent();
		}
		return null;
	}
}
