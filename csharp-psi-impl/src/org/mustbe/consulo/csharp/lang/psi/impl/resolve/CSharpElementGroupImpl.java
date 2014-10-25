package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpOperatorHelper;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpElementGroupImpl extends LightElement implements CSharpElementGroup
{
	private final Object myKey;
	private final Collection<? extends PsiElement> myElements;

	public CSharpElementGroupImpl(Project project, @NotNull Object key, Collection<? extends PsiElement> elements)
	{
		super(PsiManager.getInstance(project), CSharpLanguage.INSTANCE);
		myKey = key;
		myElements = elements;
	}

	@NotNull
	@Override
	public String getName()
	{
		if(myKey instanceof IElementType)
		{
			return CSharpOperatorHelper.getInstance(getProject()).getOperatorName((IElementType) myKey);
		}
		return myKey.toString();
	}

	@NotNull
	@Override
	public Collection<? extends PsiElement> getElements()
	{
		return myElements;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		for(PsiElement element : getElements())
		{
			if(element instanceof Navigatable)
			{
				((Navigatable) element).navigate(requestFocus);
				break;
			}
		}
	}

	@Override
	public String toString()
	{
		return "CSharpElementGroup: " + getName();
	}
}