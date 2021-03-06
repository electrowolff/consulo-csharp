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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.RemoveModifierFix;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class CS0106 extends CompilerCheck<DotNetModifierListOwner>
{
	public static enum Owners
	{
		Constructor(CSharpModifier.PUBLIC, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL),
		StaticConstructor(CSharpModifier.STATIC),
		Constant(CSharpModifier.PUBLIC, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL),
		DeConstructor,
		InterfaceMember(CSharpModifier.NEW),
		GenericParameter(CSharpModifier.IN, CSharpModifier.OUT),
		NamespaceEnum(CSharpModifier.PUBLIC, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL),
		NestedEnum(CSharpModifier.PUBLIC, CSharpModifier.PROTECTED, CSharpModifier.PRIVATE, CSharpModifier.INTERNAL),
		NamespaceType(CSharpModifier.STATIC, CSharpModifier.PUBLIC, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL, CSharpModifier.ABSTRACT, CSharpModifier.PARTIAL, CSharpModifier.SEALED),
		NestedType(CSharpModifier.PUBLIC, CSharpModifier.PRIVATE, CSharpModifier.PROTECTED, CSharpModifier.INTERNAL, CSharpModifier.ABSTRACT, CSharpModifier.PARTIAL, CSharpModifier.SEALED,
				CSharpModifier.STATIC),
		Unknown
				{
					@Override
					public boolean isValidModifier(DotNetModifier modifier)
					{
						return true;
					}
				};

		private DotNetModifier[] myValidModifiers;

		Owners(DotNetModifier... validModifiers)
		{
			myValidModifiers = validModifiers;
		}

		public boolean isValidModifier(DotNetModifier modifier)
		{
			return ArrayUtil.contains(modifier, myValidModifiers);
		}
	}

	@RequiredReadAction
	@NotNull
	@Override
	public List<CompilerCheckBuilder> check(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetModifierListOwner element)
	{
		DotNetModifierList modifierList = element.getModifierList();
		if(modifierList == null)
		{
			return Collections.emptyList();
		}

		List<CompilerCheckBuilder> list = Collections.emptyList();
		Owners owners = toOwners(element);

		DotNetModifier[] modifiers = modifierList.getModifiers();
		if(modifiers.length == 0)
		{
			return list;
		}

		for(DotNetModifier modifier : modifiers)
		{
			if(!owners.isValidModifier(modifier))
			{
				PsiElement modifierElement = modifierList.getModifierElement(modifier);
				if(modifierElement == null)
				{
					continue;
				}

				if(list.isEmpty())
				{
					list = new ArrayList<CompilerCheckBuilder>(2);
				}

				list.add(newBuilder(modifierElement, modifier.getPresentableText()).addQuickFix(new RemoveModifierFix(modifier, element)));
			}
		}
		return list;
	}

	@RequiredReadAction
	public static Owners toOwners(DotNetModifierListOwner owner)
	{
		if(owner instanceof CSharpFieldDeclaration && ((CSharpFieldDeclaration) owner).isConstant())
		{
			return Owners.Constant;
		}

		if(owner instanceof CSharpConstructorDeclaration)
		{
			if(((CSharpConstructorDeclaration) owner).isDeConstructor())
			{
				return Owners.DeConstructor;
			}

			if(owner.hasModifier(DotNetModifier.STATIC))
			{
				return Owners.StaticConstructor;
			}
			return Owners.Constructor;
		}

		if(owner instanceof CSharpMethodDeclaration || owner instanceof CSharpPropertyDeclaration || owner instanceof CSharpIndexMethodDeclaration)
		{
			PsiElement parent = owner.getParent();

			if(parent instanceof CSharpTypeDeclaration)
			{
				if(((CSharpTypeDeclaration) parent).isInterface())
				{
					return Owners.InterfaceMember;
				}
			}
		}

		if(owner instanceof DotNetGenericParameter)
		{
			return Owners.GenericParameter;
		}

		if(owner instanceof DotNetTypeDeclaration)
		{
			boolean anEnum = ((DotNetTypeDeclaration) owner).isEnum();

			PsiElement parent = owner.getParent();
			if(parent instanceof CSharpNamespaceDeclaration || parent instanceof CSharpFile)
			{
				return anEnum ? Owners.NamespaceEnum : Owners.NamespaceType;
			}
			else if(parent instanceof DotNetTypeDeclaration)
			{
				return anEnum ? Owners.NestedEnum : Owners.NestedType;
			}
		}
		return Owners.Unknown;
	}
}
