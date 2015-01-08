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
import org.mustbe.consulo.csharp.lang.psi.msil.MsilToCSharpManager;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.util.NotNullPairFunction;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CSharpTransformer implements NotNullPairFunction<Module, DotNetTypeDeclaration, DotNetTypeDeclaration>
{
	public static final CSharpTransformer INSTANCE = new CSharpTransformer();


	@NotNull
	@Override
	public DotNetTypeDeclaration fun(Module t, DotNetTypeDeclaration v)
	{
		MsilToCSharpManager msilToCSharpManager = MsilToCSharpManager.getInstance(t);

		PsiElement wrap = msilToCSharpManager.wrap(v);
		if(wrap instanceof DotNetLikeMethodDeclaration)
		{
			return (DotNetTypeDeclaration) wrap;
		}
		return v;
	}
}
