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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDefStatementImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.EmptyStub;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpTypeDefStubElementType extends CSharpEmptyStubElementType<CSharpTypeDefStatement>
{
	public CSharpTypeDefStubElementType()
	{
		super("TYPE_DEF");
	}

	@NotNull
	@Override
	public CSharpTypeDefStatement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpTypeDefStatementImpl(astNode);
	}

	@Override
	public CSharpTypeDefStatement createPsi(@NotNull EmptyStub<CSharpTypeDefStatement> stub)
	{
		return new CSharpTypeDefStatementImpl(stub);
	}
}
