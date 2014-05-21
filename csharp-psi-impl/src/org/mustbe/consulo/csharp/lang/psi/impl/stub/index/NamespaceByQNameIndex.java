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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.index;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class NamespaceByQNameIndex extends StringStubIndexExtension<DotNetNamespaceDeclaration>
{
	public static NamespaceByQNameIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtension(NamespaceByQNameIndex.class);
	}

	@NotNull
	@Override
	public StubIndexKey<String, DotNetNamespaceDeclaration> getKey()
	{
		return CSharpIndexKeys.NAMESPACE_BY_QNAME_INDEX;
	}
}
