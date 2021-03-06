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

package org.mustbe.consulo.csharp.lang.doc.inspection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.doc.CSharpDocLanguage;
import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.psi.PsiErrorElement;

/**
 * @author VISTALL
 * @since 20.03.2016
 */
public class CSharpDocHighlightErrorFilter extends HighlightErrorFilter
{
	@Override
	@RequiredReadAction
	public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement element)
	{
		// dont highlight all elements as error inside docs
		return element.getLanguage() != CSharpDocLanguage.INSTANCE;
	}
}
