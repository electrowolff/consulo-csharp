package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public enum StaticResolveSelectors implements CSharpResolveSelector
{
	NONE
			{
				@RequiredReadAction
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
				{
					throw new UnsupportedOperationException();
				}
			},
	INDEX_METHOD_GROUP
			{
				@RequiredReadAction
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
				{
					CSharpElementGroup<CSharpIndexMethodDeclaration> group = context.indexMethodGroup(deep);
					if(group == null)
					{
						return PsiElement.EMPTY_ARRAY;
					}
					return new PsiElement[] {group};
				}
			},

	CONSTRUCTOR_GROUP
			{
				@RequiredReadAction
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
				{
					CSharpElementGroup<CSharpConstructorDeclaration> group = context.constructorGroup();
					if(group == null)
					{
						return PsiElement.EMPTY_ARRAY;
					}
					return new PsiElement[] {group};
				}
			},

	DE_CONSTRUCTOR_GROUP
			{
				@RequiredReadAction
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
				{
					CSharpElementGroup<CSharpConstructorDeclaration> group = context.deConstructorGroup();
					if(group == null)
					{
						return PsiElement.EMPTY_ARRAY;
					}
					return new PsiElement[] {group};
				}
			}
}
