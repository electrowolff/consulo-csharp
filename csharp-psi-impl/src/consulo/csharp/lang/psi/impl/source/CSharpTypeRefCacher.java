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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.lang.psi.impl.DotNetTypeRefCacheUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.util.NotNullFunction;

/**
 * @author VISTALL
 * @since 13-May-16
 */
public abstract class CSharpTypeRefCacher<E extends PsiElement>
{
	private static class Resolver<E extends PsiElement> implements NotNullFunction<E, DotNetTypeRef>
	{
		private CSharpTypeRefCacher<E> myCacher;
		private boolean myValue;

		private Resolver(CSharpTypeRefCacher<E> cacher, boolean value)
		{
			myCacher = cacher;
			myValue = value;
		}

		@NotNull
		@Override
		@RequiredReadAction
		public DotNetTypeRef fun(E e)
		{
			return myCacher.toTypeRefImpl(e, myValue);
		}
	}

	private static Key<CachedValue<DotNetTypeRef>> ourTrueTypeRefKey = Key.create("CSharpTypeCacheUtil.ourTrueTypeRefKey");
	private static Key<CachedValue<DotNetTypeRef>> ourFalseTypeRefKey = Key.create("CSharpTypeCacheUtil.ourFalseTypeRefKey");

	private NotNullFunction<E, DotNetTypeRef> myTrueFunction = new Resolver<E>(this, true);
	private NotNullFunction<E, DotNetTypeRef> myFalseFunction = new Resolver<E>(this, false);

	private final boolean myLocal;

	protected CSharpTypeRefCacher(boolean local)
	{
		myLocal = local;
	}

	@NotNull
	@RequiredReadAction
	protected abstract DotNetTypeRef toTypeRefImpl(E element, boolean resolveFromParentOrInitializer);

	@NotNull
	@RequiredReadAction
	public DotNetTypeRef toTypeRef(E element, boolean resolveFromParentOrInitializer)
	{
		Key<CachedValue<DotNetTypeRef>> key = resolveFromParentOrInitializer ? ourTrueTypeRefKey : ourFalseTypeRefKey;
		NotNullFunction<E, DotNetTypeRef> resolver = resolveFromParentOrInitializer ? myTrueFunction : myFalseFunction;
		return myLocal ? DotNetTypeRefCacheUtil.localCacheTypeRef(key, element, resolver) : DotNetTypeRefCacheUtil.cacheTypeRef(key, element, resolver);
	}
}
