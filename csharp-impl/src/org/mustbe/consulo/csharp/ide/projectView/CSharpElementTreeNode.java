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

package org.mustbe.consulo.csharp.ide.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.CSharpElementPresentationUtil;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpElementTreeNode extends CSharpAbstractElementTreeNode<DotNetNamedElement>
{
	public static int getWeight(PsiElement element)
	{
		int defaultWeight = getDefaultWeight(element);
		if(element instanceof DotNetVirtualImplementOwner)
		{
			DotNetTypeRef typeRefForImplement = ((DotNetVirtualImplementOwner) element).getTypeRefForImplement();
			if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
			{
				return -defaultWeight;
			}
		}
		return 0;
	}

	private static int getDefaultWeight(PsiElement element)
	{
		if(element instanceof DotNetNamespaceDeclaration)
		{
			return 100;
		}
		else if(element instanceof DotNetTypeDeclaration)
		{
			return 200;
		}
		else if(element instanceof DotNetFieldDeclaration)
		{
			return 300;
		}
		else if(element instanceof DotNetPropertyDeclaration)
		{
			return 400;
		}
		else if(element instanceof DotNetEventDeclaration)
		{
			return 500;
		}
		else if(element instanceof DotNetConstructorDeclaration)
		{
			return 700;
		}
		else if(element instanceof DotNetLikeMethodDeclaration)
		{
			return 600;
		}
		return 0;
	}

	public CSharpElementTreeNode(DotNetNamedElement dotNetMemberOwner, ViewSettings viewSettings)
	{
		super(dotNetMemberOwner.getProject(), dotNetMemberOwner, viewSettings);
	}

	@Override
	public int getWeight()
	{
		// namespace
		// type
		// field
		// property
		// event
		// constructor
		// method

		DotNetNamedElement element = getValue();
		int weight = getWeight(element);
		if(weight != 0)
		{
			return weight;
		}
		return super.getWeight();
	}

	@Nullable
	@Override
	protected Collection<AbstractTreeNode> getChildrenImpl()
	{
		if(!getSettings().isShowMembers())
		{
			return Collections.emptyList();
		}
		DotNetElement value = getValue();
		if(value instanceof DotNetMemberOwner)
		{
			DotNetNamedElement[] members = ((DotNetMemberOwner) value).getMembers();
			if(members.length == 0)
			{
				return Collections.emptyList();
			}
			List<AbstractTreeNode> list = new ArrayList<AbstractTreeNode>(members.length);
			for(DotNetNamedElement dotNetElement : members)
			{
				list.add(new CSharpElementTreeNode(dotNetElement, getSettings()));
			}
			return list;
		}

		return Collections.emptyList();
	}

	@Override
	@RequiredDispatchThread
	protected void updateImpl(PresentationData presentationData)
	{
		DotNetNamedElement value = getValue();

		presentationData.setIcon(IconDescriptorUpdaters.getIcon(value, Iconable.ICON_FLAG_VISIBILITY));

		presentationData.setPresentableText(getPresentableText(value));
	}

	@RequiredReadAction
	public static String getPresentableText(PsiNamedElement value)
	{
		if(value instanceof DotNetLikeMethodDeclaration)
		{
			return CSharpElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) value, CSharpElementPresentationUtil.METHOD_SCALA_LIKE_FULL);
		}
		else if(value instanceof DotNetFieldDeclaration)
		{
			return CSharpElementPresentationUtil.formatField((DotNetFieldDeclaration) value);
		}
		else if(value instanceof DotNetNamespaceDeclaration)
		{
			return ((DotNetNamespaceDeclaration) value).getPresentableQName();
		}
		else if(value instanceof DotNetTypeDeclaration)
		{
			return DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) value);
		}
		else
		{
			return value.getName();
		}
	}
}
