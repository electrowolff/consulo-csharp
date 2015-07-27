/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.refactoring.move;

import java.util.Set;

import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPsiUtilImpl;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.move.MoveHandlerDelegate;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpMoveHandlerDelegate extends MoveHandlerDelegate
{
	@Override
	public boolean isValidTarget(PsiElement psiElement, PsiElement[] sources)
	{
		for(PsiElement source : sources)
		{
			if(source instanceof CSharpTypeDeclaration)
			{
				CSharpFile containingFile = (CSharpFile) source.getContainingFile();
				if(containingFile == null)
				{
					continue;
				}
				DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement(containingFile);
				if(singleElement == source)
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canMove(DataContext dataContext)
	{
		PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
		return canMove(new PsiElement[] {psiElement}, null);
	}

	@Override
	public void collectFilesOrDirsFromContext(DataContext dataContext, Set<PsiElement> filesOrDirs)
	{
		PsiElement psiElement = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
		if(psiElement instanceof CSharpTypeDeclaration)
		{
			CSharpFile containingFile = (CSharpFile) psiElement.getContainingFile();
			DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement(containingFile);
			if(singleElement == psiElement)
			{
				filesOrDirs.add(containingFile);
			}
		}
	}
}
