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

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpGenericParameterImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpGenericParameterStub;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpGenericParameterStubElementType extends CSharpAbstractStubElementType<CSharpGenericParameterStub, DotNetGenericParameter>
{
	public CSharpGenericParameterStubElementType()
	{
		super("GENERIC_PARAMETER");
	}

	@NotNull
	@Override
	public DotNetGenericParameter createElement(@NotNull ASTNode astNode)
	{
		return new CSharpGenericParameterImpl(astNode);
	}

	@Override
	public DotNetGenericParameter createPsi(@NotNull CSharpGenericParameterStub cSharpGenericParameterStub)
	{
		return new CSharpGenericParameterImpl(cSharpGenericParameterStub);
	}

	@RequiredReadAction
	@Override
	public CSharpGenericParameterStub createStub(@NotNull DotNetGenericParameter genericParameter, StubElement stubElement)
	{
		return new CSharpGenericParameterStub(stubElement);
	}

	@Override
	public void serialize(@NotNull CSharpGenericParameterStub cSharpGenericParameterStub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
	}

	@NotNull
	@Override
	public CSharpGenericParameterStub deserialize(@NotNull StubInputStream stubInputStream, StubElement stubElement) throws IOException
	{
		return new CSharpGenericParameterStub(stubElement);
	}
}
