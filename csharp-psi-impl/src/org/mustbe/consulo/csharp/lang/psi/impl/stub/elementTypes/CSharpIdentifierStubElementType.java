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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpIdentifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpStubIdentifierImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpIdentifierStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 26.07.2015
 */
public class CSharpIdentifierStubElementType extends CSharpAbstractStubElementType<CSharpIdentifierStub, CSharpIdentifier>
{
	public CSharpIdentifierStubElementType()
	{
		super("IDENTIFIER");
	}

	@NotNull
	@Override
	public PsiElement createElement(@NotNull ASTNode astNode)
	{
		return new CSharpStubIdentifierImpl(astNode);
	}

	@Override
	public CSharpIdentifier createPsi(@NotNull CSharpIdentifierStub stub)
	{
		return new CSharpStubIdentifierImpl(stub, this);
	}

	@RequiredReadAction
	@Override
	public CSharpIdentifierStub createStub(@NotNull CSharpIdentifier psi, StubElement parentStub)
	{
		String value = psi.getValue();
		return new CSharpIdentifierStub(parentStub, this, value);
	}

	@Override
	public void serialize(@NotNull CSharpIdentifierStub stub, @NotNull StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getValue());
	}

	@NotNull
	@Override
	public CSharpIdentifierStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef nameRef = dataStream.readName();
		return new CSharpIdentifierStub(parentStub, this, nameRef);
	}
}
