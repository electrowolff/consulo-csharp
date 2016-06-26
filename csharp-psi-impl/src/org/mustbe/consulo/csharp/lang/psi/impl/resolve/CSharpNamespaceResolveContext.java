package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.ToNativeElementTransformers;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransformer;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeWithExtensionMethodsIndex;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.lang.psi.impl.BaseDotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.DotNetNamespaceStubUtil;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import consulo.csharp.lang.CSharpCastType;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class CSharpNamespaceResolveContext implements CSharpResolveContext
{
	private final DotNetNamespaceAsElement myNamespaceAsElement;
	private final GlobalSearchScope myResolveScope;

	public CSharpNamespaceResolveContext(DotNetNamespaceAsElement namespaceAsElement, GlobalSearchScope resolveScope)
	{
		myNamespaceAsElement = namespaceAsElement;
		myResolveScope = resolveScope;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep)
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep)
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull CSharpCastType castType, boolean deep)
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		String presentableName = DotNetNamespaceStubUtil.getIndexableNamespace(myNamespaceAsElement.getPresentableQName());

		Collection<DotNetTypeDeclaration> decls = TypeWithExtensionMethodsIndex.getInstance().get(presentableName, myNamespaceAsElement.getProject(), myResolveScope);

		if(decls.isEmpty())
		{
			return null;
		}
		List<CSharpElementGroup<CSharpMethodDeclaration>> list = new SmartList<CSharpElementGroup<CSharpMethodDeclaration>>();
		Set<String> processed = new THashSet<String>();
		for(DotNetTypeDeclaration typeDeclaration : decls)
		{
			PsiElement wrappedDeclaration = ToNativeElementTransformers.transform(typeDeclaration);

			if(typeDeclaration instanceof CSharpTypeDeclaration && typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
			{
				String vmQName = typeDeclaration.getVmQName();
				if(processed.contains(vmQName))
				{
					continue;
				}
				processed.add(vmQName);
			}

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, myResolveScope, wrappedDeclaration);

			CSharpElementGroup<CSharpMethodDeclaration> extensionMethodByName = context.findExtensionMethodGroupByName(name);
			if(extensionMethodByName != null)
			{
				list.add(extensionMethodByName);
			}
		}
		return new CSharpCompositeElementGroupImpl<CSharpMethodDeclaration>(myNamespaceAsElement.getProject(), list);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@NotNull final Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		return processExtensionMethodGroups(myNamespaceAsElement.getPresentableQName(), myNamespaceAsElement.getProject(), myResolveScope, processor);
	}

	@RequiredReadAction
	public static boolean processExtensionMethodGroups(@Nullable final String qName,
			@NotNull final Project project,
			@NotNull final GlobalSearchScope scope,
			@NotNull final Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		if(qName == null)
		{
			return true;
		}
		String indexableName = DotNetNamespaceStubUtil.getIndexableNamespace(qName);

		final Set<String> processed = new THashSet<String>();
		final Set<PsiElement> typeDeclarations = new THashSet<PsiElement>();

		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX, indexableName, project, scope, DotNetTypeDeclaration.class, new Processor<DotNetTypeDeclaration>()
		{
			@Override
			@RequiredReadAction
			public boolean process(DotNetTypeDeclaration typeDeclaration)
			{
				PsiElement wrappedDeclaration = ToNativeElementTransformers.transform(typeDeclaration);

				if(typeDeclaration instanceof CSharpTypeDeclaration && typeDeclaration.hasModifier(CSharpModifier.PARTIAL))
				{
					String vmQName = typeDeclaration.getVmQName();
					if(processed.contains(vmQName))
					{
						return true;
					}
					processed.add(vmQName);
				}

				typeDeclarations.add(wrappedDeclaration);
				return true;
			}
		});


		for(PsiElement typeDeclaration : typeDeclarations)
		{
			ProgressManager.checkCanceled();

			CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, scope, typeDeclaration);

			if(!context.processExtensionMethodGroups(processor))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		DotNetNamespaceAsElement.ChildrenFilter filter = holder.getUserData(BaseDotNetNamespaceAsElement.FILTER);
		if(filter == null)
		{
			filter = DotNetNamespaceAsElement.ChildrenFilter.NONE;
		}

		return myNamespaceAsElement.findChildren(name, myResolveScope, CSharpTransformer.INSTANCE, filter);
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		DotNetNamespaceAsElement.ChildrenFilter filter = DotNetNamespaceAsElement.ChildrenFilter.ONLY_ELEMENTS;
		if(StringUtil.isEmpty(myNamespaceAsElement.getPresentableQName()))
		{
			filter = DotNetNamespaceAsElement.ChildrenFilter.NONE;
		}
		PsiElement[] children = myNamespaceAsElement.getChildren(myResolveScope, CSharpTransformer.INSTANCE, filter);
		children = CSharpCompositeTypeDeclaration.wrapPartialTypes(myResolveScope, myNamespaceAsElement.getProject(), children);

		for(PsiElement element : children)
		{
			ProgressManager.checkCanceled();
			if(!processor.process(element))
			{
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public PsiElement getElement()
	{
		return myNamespaceAsElement;
	}
}
