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

package org.mustbe.consulo.csharp.ide.actions.internal;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.csharp.ide.msil.representation.builder.CSharpStubBuilderVisitor;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import com.intellij.ide.actions.OpenFileAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightVirtualFile;

/**
 * @author VISTALL
 * @since 23.03.2016
 */
public class OpenTypeFromVariableAction extends AnAction
{
	@RequiredDispatchThread
	@Override
	public void actionPerformed(@NotNull AnActionEvent e)
	{
		PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
		if(!(psiElement instanceof DotNetVariable))
		{
			return;
		}

		DotNetTypeRef typeRef = ((DotNetVariable) psiElement).toTypeRef(true);

		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();

		PsiElement element = typeResolveResult.getElement();
		if(!(element instanceof DotNetNamedElement))
		{
			return;
		}

		DotNetNamedElement extract = GenericUnwrapTool.extract((DotNetNamedElement) element, typeResolveResult.getGenericExtractor());

		List<StubBlock> stubBlocks = CSharpStubBuilderVisitor.buildBlocks(extract);

		CharSequence text = StubBlockUtil.buildText(stubBlocks);

		LightVirtualFile lightVirtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text);

		OpenFileAction.openFile(lightVirtualFile, psiElement.getProject());
	}
}
