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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.NotNullLazyValue;

/**
 * @author VISTALL
 * @since 24.05.2015
 */
public abstract class SingleNullableStateResolveResult implements DotNetTypeResolveResult
{
	private NotNullLazyValue<Boolean> myNullalbeCacheValue = new NotNullLazyValue<Boolean>()
	{
		@NotNull
		@Override
		@RequiredReadAction
		protected Boolean compute()
		{
			return isNullableImpl();
		}
	};

	@RequiredReadAction
	public abstract boolean isNullableImpl();

	@Override
	public final boolean isNullable()
	{
		return myNullalbeCacheValue.getValue();
	}
}
