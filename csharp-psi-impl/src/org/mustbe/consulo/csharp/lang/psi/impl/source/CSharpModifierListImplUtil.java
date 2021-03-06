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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifierList;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpModifierListImplUtil
{
	public static final Map<CSharpModifier, IElementType> ourModifiers = new LinkedHashMap<CSharpModifier, IElementType>()
	{
		{
			put(CSharpModifier.PUBLIC, CSharpTokens.PUBLIC_KEYWORD);
			put(CSharpModifier.PROTECTED, CSharpTokens.PROTECTED_KEYWORD);
			put(CSharpModifier.PRIVATE, CSharpTokens.PRIVATE_KEYWORD);
			put(CSharpModifier.STATIC, CSharpTokens.STATIC_KEYWORD);
			put(CSharpModifier.SEALED, CSharpTokens.SEALED_KEYWORD);
			put(CSharpModifier.ABSTRACT, CSharpTokens.ABSTRACT_KEYWORD);
			put(CSharpModifier.READONLY, CSharpTokens.READONLY_KEYWORD);
			put(CSharpModifier.UNSAFE, CSharpTokens.UNSAFE_KEYWORD);
			put(CSharpModifier.PARAMS, CSharpTokens.PARAMS_KEYWORD);
			put(CSharpModifier.THIS, CSharpTokens.THIS_KEYWORD);
			put(CSharpModifier.PARTIAL, CSharpSoftTokens.PARTIAL_KEYWORD);
			put(CSharpModifier.INTERNAL, CSharpTokens.INTERNAL_KEYWORD);
			put(CSharpModifier.REF, CSharpTokens.REF_KEYWORD);
			put(CSharpModifier.OUT, CSharpTokens.OUT_KEYWORD);
			put(CSharpModifier.VIRTUAL, CSharpTokens.VIRTUAL_KEYWORD);
			put(CSharpModifier.NEW, CSharpTokens.NEW_KEYWORD);
			put(CSharpModifier.OVERRIDE, CSharpTokens.OVERRIDE_KEYWORD);
			put(CSharpModifier.ASYNC, CSharpSoftTokens.ASYNC_KEYWORD);
			put(CSharpModifier.IN, CSharpSoftTokens.IN_KEYWORD);
			put(CSharpModifier.EXTERN, CSharpSoftTokens.EXTERN_KEYWORD);
		}
	};

	@RequiredReadAction
	public static boolean hasModifier(@NotNull CSharpModifierList modifierList, @NotNull DotNetModifier modifier)
	{
		if(modifierList.hasModifierInTree(modifier))
		{
			return true;
		}

		CSharpModifier cSharpModifier = CSharpModifier.as(modifier);
		PsiElement parent = modifierList.getParent();
		switch(cSharpModifier)
		{
			case PUBLIC:
				if(parent instanceof CSharpEnumConstantDeclaration)
				{
					return true;
				}
				if(parent instanceof DotNetVirtualImplementOwner && parent.getParent() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent.getParent()).isInterface())
				{
					return true;
				}
				break;
			case READONLY:
				if(parent instanceof CSharpEnumConstantDeclaration)
				{
					return true;
				}
				break;
			case STATIC:
				if(parent instanceof CSharpFieldDeclaration)
				{
					if(((CSharpFieldDeclaration) parent).isConstant() && parent.getParent() instanceof CSharpTypeDeclaration)
					{
						return true;
					}
				}
				if(parent instanceof CSharpEnumConstantDeclaration)
				{
					return true;
				}
				if(parent instanceof DotNetXXXAccessor)
				{
					PsiElement superParent = parent.getParent();
					return superParent instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) superParent).hasModifier(DotNetModifier.STATIC);
				}
				break;
			case INTERFACE_ABSTRACT:
				if(parent instanceof DotNetVirtualImplementOwner && parent.getParent() instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent.getParent()).isInterface())
				{
					return true;
				}
				if(parent instanceof DotNetXXXAccessor)
				{
					if(((DotNetXXXAccessor) parent).getCodeBlock() == null)
					{
						PsiElement accessorOwner = parent.getParent();
						if(accessorOwner instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) accessorOwner).hasModifier(modifier))
						{
							return true;
						}
					}
				}
				break;
			case ABSTRACT:
				if(parent instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) parent).isInterface())
				{
					return true;
				}
				if(hasModifier(modifierList, CSharpModifier.INTERFACE_ABSTRACT))
				{
					return true;
				}
				if(parent instanceof DotNetXXXAccessor)
				{
					PsiElement superParent = parent.getParent();
					return superParent instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) superParent).hasModifier(DotNetModifier.ABSTRACT);
				}
				break;
			case SEALED:
				if(parent instanceof DotNetTypeDeclaration && (((DotNetTypeDeclaration) parent).isEnum() || ((DotNetTypeDeclaration) parent).isStruct()))
				{
					return true;
				}
				break;
		}
		return false;
	}

	@RequiredReadAction
	public static void addModifier(@NotNull CSharpModifierList modifierList, @NotNull DotNetModifier modifier)
	{
		PsiElement anchor = modifierList.getLastChild();

		CSharpFieldDeclaration field = CSharpFileFactory.createField(modifierList.getProject(), modifier.getPresentableText() + " int b");
		PsiElement modifierElement = field.getModifierList().getModifierElement(modifier);

		PsiElement psiElement = modifierList.addAfter(modifierElement, anchor);
		modifierList.addAfter(PsiParserFacade.SERVICE.getInstance(modifierList.getProject()).createWhiteSpaceFromText(" "), psiElement);
	}

	public static void removeModifier(@NotNull CSharpModifierList modifierList, @NotNull DotNetModifier modifier)
	{
		CSharpModifier as = CSharpModifier.as(modifier);
		PsiElement modifierElement = modifierList.getModifierElement(as);
		if(modifierElement != null)
		{
			PsiElement next = modifierElement.getNextSibling();
			if(next instanceof PsiWhiteSpace)
			{
				next.delete();
			}

			modifierElement.delete();
		}
	}
}
