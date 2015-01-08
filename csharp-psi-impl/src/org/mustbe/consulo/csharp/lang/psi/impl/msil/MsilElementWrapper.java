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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightElement;
import org.mustbe.consulo.csharp.lang.psi.msil.MsilToCSharpManager;
import org.mustbe.consulo.msil.representation.MsilRepresentationNavigateUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public abstract class MsilElementWrapper<T extends PsiElement> extends CSharpLightElement<T>
{
	@NotNull
	protected final MsilToCSharpManager myMsilToCSharpManager;
	private final PsiElement myParent;

	public MsilElementWrapper(@NotNull MsilToCSharpManager msilToCSharpManager, @Nullable PsiElement parent, T msilElement)
	{
		super(msilElement);
		myMsilToCSharpManager = msilToCSharpManager;
		myParent = parent;
	}

	@NotNull
	public MsilToCSharpManager getMsilToCSharpManager()
	{
		return myMsilToCSharpManager;
	}

	@Override
	public PsiElement getParent()
	{
		return myParent;
	}

	@Override
	public PsiFile getContainingFile()
	{
		return null;
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		MsilRepresentationNavigateUtil.navigateToRepresentation(myOriginal, CSharpFileType.INSTANCE);
	}

	@NotNull
	@Override
	public PsiElement getNavigationElement()
	{
		return this;
	}
}
