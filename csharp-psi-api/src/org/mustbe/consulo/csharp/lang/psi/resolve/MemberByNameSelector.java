package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class MemberByNameSelector extends UserDataHolderBase implements CSharpNamedResolveSelector
{
	private String myName;

	public MemberByNameSelector(@NotNull String name)
	{
		myName = name;
	}

	@NotNull
	public String getName()
	{
		return myName;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
	{
		return context.findByName(myName, deep, this);
	}

	@Override
	public boolean isNameEqual(@NotNull String name)
	{
		return Comparing.equal(myName, name);
	}
}
