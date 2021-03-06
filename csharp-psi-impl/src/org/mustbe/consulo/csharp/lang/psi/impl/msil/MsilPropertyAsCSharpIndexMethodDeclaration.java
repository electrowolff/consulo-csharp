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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.List;

import consulo.lombok.annotations.Lazy;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightAttributeBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilPropertyEntry;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 28.05.14
 */
public class MsilPropertyAsCSharpIndexMethodDeclaration extends MsilElementWrapper<MsilPropertyEntry> implements CSharpIndexMethodDeclaration
{
	private final MsilModifierListToCSharpModifierList myModifierList;

	private final DotNetParameter[] myParameters;

	private final DotNetXXXAccessor[] myAccessors;

	public MsilPropertyAsCSharpIndexMethodDeclaration(PsiElement parent, MsilPropertyEntry propertyEntry, List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		super(parent, propertyEntry);

		myAccessors = MsilPropertyAsCSharpPropertyDeclaration.buildAccessors(this, pairs);
		myModifierList = new MsilModifierListToCSharpModifierList(MsilPropertyAsCSharpPropertyDeclaration.getAdditionalModifiers(propertyEntry,
				pairs), this, propertyEntry.getModifierList());

		String name = getName();
		if(!Comparing.equal(name, DotNetPropertyDeclaration.DEFAULT_INDEX_PROPERTY_NAME))
		{
			CSharpLightAttributeBuilder attribute = new CSharpLightAttributeBuilder(propertyEntry, DotNetTypes.System.Runtime.CompilerServices
					.IndexerName);

			attribute.addParameterExpression(name);

			myModifierList.addAdditionalAttribute(attribute);
		}
		Pair<DotNetXXXAccessor, MsilMethodEntry> p = pairs.get(0);

		DotNetParameter firstParameter = p.getSecond().getParameters()[0];
		myParameters = new DotNetParameter[]{new MsilParameterAsCSharpParameter(this, firstParameter, this, 0)};
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitIndexMethodDeclaration(this);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return CSharpLikeMethodDeclarationImplUtil.getParametersInfos(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myOriginal.getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myOriginal.getPresentableQName();
	}

	@Override
	public String getName()
	{
		return myOriginal.getName();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getPresentableQName();
	}

	@NotNull
	@Override
	public DotNetXXXAccessor[] getAccessors()
	{
		return myAccessors;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetType getReturnType()
	{
		throw new IllegalArgumentException();
	}

	@RequiredReadAction
	@NotNull
	@Override
	@Lazy
	public DotNetTypeRef getReturnTypeRef()
	{
		return MsilToCSharpUtil.extractToCSharp(myOriginal.toTypeRef(false), myOriginal);
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return getAccessors();
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myModifierList.hasModifier(modifier);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myModifierList;
	}

	@NotNull
	@Override
	@Lazy
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		DotNetParameter[] parameters = getParameters();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			typeRefs[i] = parameter.toTypeRef(false);
		}
		return typeRefs;
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		return myParameters;
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return null;
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return DotNetGenericParameter.EMPTY_ARRAY;
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@Nullable
	@Override
	@Lazy(notNull = false)
	public DotNetType getTypeForImplement()
	{
		String nameFromBytecode = myOriginal.getNameFromBytecode();
		String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
		SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
		if(someType != null)
		{
			return new DummyType(getProject(), MsilPropertyAsCSharpIndexMethodDeclaration.this, someType);
		}
		return null;
	}

	@NotNull
	@Override
	@Lazy
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		return typeForImplement != null ? typeForImplement.toTypeRef() : DotNetTypeRef.ERROR_TYPE;
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpIndexMethodDeclaration.class;
	}
}
