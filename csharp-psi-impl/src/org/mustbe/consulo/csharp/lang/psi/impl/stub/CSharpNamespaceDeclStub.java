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

package org.mustbe.consulo.csharp.lang.psi.impl.stub;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpNamespaceDeclarationImpl;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpNamespaceDeclStub extends StubBase<CSharpNamespaceDeclarationImpl>
{
	private final StringRef myReferenceTextRef;

	public CSharpNamespaceDeclStub(StubElement parent, IStubElementType elementType, @Nullable StringRef referenceTextRef)
	{
		super(parent, elementType);
		myReferenceTextRef = referenceTextRef;
	}

	public CSharpNamespaceDeclStub(final StubElement parent, final IStubElementType elementType, @Nullable final String referenceTextRef)
	{
		this(parent, elementType, StringRef.fromString(referenceTextRef));
	}

	public String getReferenceTextRef()
	{
		return StringRef.toString(myReferenceTextRef);
	}
}
