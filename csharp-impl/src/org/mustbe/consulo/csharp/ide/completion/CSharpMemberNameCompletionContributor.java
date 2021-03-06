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

package org.mustbe.consulo.csharp.ide.completion;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.codeInsight.completion.CompletionProvider;
import org.mustbe.consulo.csharp.ide.completion.insertHandler.CSharpTailInsertHandlerWithChar;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpIdentifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class CSharpMemberNameCompletionContributor extends CompletionContributor
{
	public CSharpMemberNameCompletionContributor()
	{
		extend(CompletionType.BASIC, StandardPatterns.psiElement(CSharpTokens.IDENTIFIER).withParent(CSharpIdentifier.class), new CompletionProvider()
		{
			@Override
			@RequiredReadAction
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result)
			{
				PsiElement position = parameters.getPosition();
				CSharpIdentifier identifier = PsiTreeUtil.getParentOfType(position, CSharpIdentifier.class);
				assert identifier != null;

				PsiElement parent = identifier.getParent();
				if(parent instanceof DotNetVariable)
				{
					DotNetVariable variable = (DotNetVariable) parent;
					Collection<String> suggestedNames = CSharpNameSuggesterUtil.getSuggestedVariableNames(variable);

					for(String suggestedName : suggestedNames)
					{
						LookupElementBuilder element = LookupElementBuilder.create(suggestedName);
						element = element.withInsertHandler(new CSharpTailInsertHandlerWithChar(TailType.EQ, '='));
						result.addElement(element);
					}
				}
			}
		});
	}
}
