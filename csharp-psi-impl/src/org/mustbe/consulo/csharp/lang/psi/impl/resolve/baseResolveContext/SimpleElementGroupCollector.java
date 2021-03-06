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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve.baseResolveContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpAdditionalMemberProvider;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpBaseResolveContext;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.util.AtomicNullableLazyValue;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 07.03.2016
 */
public abstract class SimpleElementGroupCollector<E extends PsiElement> extends ElementGroupCollector<E>
{
	private volatile AtomicNullableLazyValue<CSharpElementGroup<E>> myGroupValue = new AtomicNullableLazyValue<CSharpElementGroup<E>>()
	{
		@Nullable
		@Override
		@RequiredReadAction
		protected CSharpElementGroup<E> compute()
		{
			Collection<E> elements = calcElements();
			final DotNetGenericExtractor extractor = getExtractor();
			if(extractor != DotNetGenericExtractor.EMPTY)
			{
				elements = ContainerUtil.map(elements, new Function<E, E>()
				{
					@Override
					@SuppressWarnings("unchecked")
					public E fun(final E element)
					{
						return element instanceof DotNetNamedElement ? (E) GenericUnwrapTool.extract((DotNetNamedElement) element, extractor) : element;
					}
				});
			}

			return elements.isEmpty() ? null : new CSharpElementGroupImpl<E>(getProject(), myKey, elements);
		}
	};

	protected final Object myKey;

	public SimpleElementGroupCollector(@NotNull Object key, @NotNull CSharpAdditionalMemberProvider.Target target, @NotNull CSharpBaseResolveContext<?> context)
	{
		super(target, context);
		myKey = key;
	}

	@NotNull
	@RequiredReadAction
	@SuppressWarnings("unchecked")
	private Collection<E> calcElements()
	{
		final Ref<List<E>> listRef = Ref.create();
		Consumer consumer = new Consumer<E>()
		{
			@Override
			public void consume(PsiElement e)
			{
				List<E> es = listRef.get();
				if(es == null)
				{
					listRef.set(es = new SmartList<E>());
				}

				es.add((E) e);
			}
		};

		CSharpElementVisitor visitor = createVisitor(consumer);

		myResolveContext.acceptChildren(visitor);

		for(CSharpAdditionalMemberProvider memberProvider : ourAdditionalMemberProviders)
		{
			if(memberProvider.getTarget() == myTarget)
			{
				memberProvider.processAdditionalMembers(myResolveContext.getElement(), getExtractor(), consumer);
			}
		}

		List<E> list = listRef.get();
		if(list == null)
		{
			return Collections.emptyList();
		}
		return list;
	}

	@RequiredReadAction
	@Nullable
	public CSharpElementGroup<E> toGroup()
	{
		return myGroupValue.getValue();
	}
}
