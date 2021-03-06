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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.MsilHelper;
import org.mustbe.consulo.msil.lang.psi.impl.elementType.stub.MsilClassEntryStub;
import org.mustbe.consulo.msil.lang.psi.impl.elementType.stub.MsilCustomAttributeStub;
import org.mustbe.consulo.msil.lang.psi.impl.elementType.stub.MsilStubIndexer;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class CSharpMsilStubIndexer extends MsilStubIndexer
{
	@Override
	public void indexClass(@NotNull MsilClassEntryStub stub, @NotNull IndexSink indexSink)
	{
		String name = stub.getName();
		if(StringUtil.isEmpty(name))
		{
			return;
		}

		if(stub.isNested())
		{
			return;
		}

		List<StubElement> childrenStubs = stub.getChildrenStubs();
		for(StubElement childrenStub : childrenStubs)
		{
			if(childrenStub instanceof MsilCustomAttributeStub && Comparing.equal(((MsilCustomAttributeStub) childrenStub).getTypeRef(),
					DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute))
			{
				indexSink.occurrence(CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX, DotNetNamespaceStubUtil.getIndexableNamespace(stub
						.getNamespace()));
				break;
			}
		}

		indexSink.occurrence(CSharpIndexKeys.TYPE_BY_QNAME_INDEX, MsilHelper.appendNoGeneric(stub.getNamespace(), stub.getName()));
	}

	@Override
	public int getVersion()
	{
		return 3;
	}
}
