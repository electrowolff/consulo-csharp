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

package org.mustbe.consulo.csharp.ide.highlight;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import com.intellij.codeInsight.TargetElementUtil;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 06.03.2016
 */
public class CSharpHighlightUsagesHandlerFactory implements HighlightUsagesHandlerFactory
{
	@Nullable
	@Override
	@RequiredReadAction
	public HighlightUsagesHandlerBase createHighlightUsagesHandler(Editor editor, PsiFile file)
	{
		int offset = TargetElementUtil.adjustOffset(file, editor.getDocument(), editor.getCaretModel().getOffset());
		PsiElement target = file.findElementAt(offset);
		if(target != null && target.getNode().getElementType() == CSharpTokens.USING_KEYWORD)
		{
			CSharpUsingListChild listChild = PsiTreeUtil.getParentOfType(target, CSharpUsingListChild.class);
			if(listChild == null)
			{
				return null;
			}
			return new CSharpUsingHighlightUsagesHandler(editor, file, listChild);
		}
		return null;
	}
}
