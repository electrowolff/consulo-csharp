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
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpXXXAccessorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpXXXAccessorStub;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 20.05.14
 */
public class CSharpXXXAccessorStubElementType extends CSharpAbstractStubElementType<CSharpXXXAccessorStub, DotNetXXXAccessor>
{
	public CSharpXXXAccessorStubElementType()
	{
		super("XXX_ACCESSOR");
	}

	@NotNull
	@Override
	public CSharpXXXAccessorImpl createElement(@NotNull ASTNode astNode)
	{
		return new CSharpXXXAccessorImpl(astNode);
	}

	@Override
	public CSharpXXXAccessorImpl createPsi(@NotNull CSharpXXXAccessorStub cSharpXXXAccessorStub)
	{
		return new CSharpXXXAccessorImpl(cSharpXXXAccessorStub);
	}

	@RequiredReadAction
	@Override
	public CSharpXXXAccessorStub createStub(@NotNull DotNetXXXAccessor accessor, StubElement stubElement)
	{
		int otherModifiers = CSharpXXXAccessorStub.getOtherModifiers(accessor);
		return new CSharpXXXAccessorStub(stubElement, otherModifiers);
	}

	@Override
	public void serialize(@NotNull CSharpXXXAccessorStub stub, @NotNull StubOutputStream stubOutputStream) throws IOException
	{
		stubOutputStream.writeInt(stub.getOtherModifierMask());
	}

	@NotNull
	@Override
	public CSharpXXXAccessorStub deserialize(@NotNull StubInputStream inputStream, StubElement stubElement) throws IOException
	{
		int otherModifiers = inputStream.readInt();
		return new CSharpXXXAccessorStub(stubElement, otherModifiers);
	}
}
