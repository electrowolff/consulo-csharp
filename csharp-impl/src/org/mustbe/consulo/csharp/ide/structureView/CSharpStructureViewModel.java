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

package org.mustbe.consulo.csharp.ide.structureView;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.structureView.sorters.CSharpMemberSorter;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 31.12.13.
 */
public class CSharpStructureViewModel extends StructureViewModelBase
{
	public CSharpStructureViewModel(@NotNull PsiFile psiFile)
	{
		super(psiFile, new CSharpElementStructureViewTreeElement(psiFile));

		withSorters(CSharpMemberSorter.INSTANCE, Sorter.ALPHA_SORTER);
	}
}
