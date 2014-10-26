package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import gnu.trove.THashMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.MsilHelper;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import lombok.val;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContext implements CSharpResolveContext
{
	private static class Collector extends CSharpElementVisitor
	{
		private final DotNetGenericExtractor myGenericExtractor;
		private List<CSharpConstructorDeclaration> myDeConstructors;
		private List<CSharpConstructorDeclaration> myConstructors;
		private MultiMap<IElementType, CSharpMethodDeclaration> myOperatorsMap;
		private MultiMap<String, CSharpMethodDeclaration> myExtensionMap;
		private MultiMap<DotNetTypeRef, CSharpConversionMethodDeclaration> myConversionMap;
		private List<CSharpArrayMethodDeclaration> myIndexMethods;
		private MultiMap<String, PsiElement> myOtherElements = new MultiMap<String, PsiElement>();

		public Collector(DotNetGenericExtractor genericExtractor)
		{
			myGenericExtractor = genericExtractor;
		}

		@Override
		public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
		{
			if(declaration.isDeConstructor())
			{
				if(myDeConstructors == null)
				{
					myDeConstructors = new SmartList<CSharpConstructorDeclaration>();
				}
				myDeConstructors.add(declaration);
			}
			else
			{
				if(myConstructors == null)
				{
					myConstructors = new SmartList<CSharpConstructorDeclaration>();
				}
				myConstructors.add(declaration);
			}
		}

		@Override
		public void visitConversionMethodDeclaration(CSharpConversionMethodDeclaration element)
		{
			if(myConversionMap == null)
			{
				myConversionMap = new MultiMap<DotNetTypeRef, CSharpConversionMethodDeclaration>();
			}

			myConversionMap.putValue(element.getConversionTypeRef(), element);
		}

		@Override
		public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
		{
			if(declaration.isOperator())
			{
				IElementType operatorElementType = declaration.getOperatorElementType();
				if(operatorElementType == null)
				{
					return;
				}

				if(myOperatorsMap == null)
				{
					myOperatorsMap = new MultiMap<IElementType, CSharpMethodDeclaration>();
				}

				myOperatorsMap.putValue(operatorElementType, declaration);
			}
			else
			{
				// we dont interest in private impl
				if(declaration.getTypeForImplement() != null)
				{
					return;
				}

				if(CSharpMethodImplUtil.isExtensionMethod(declaration))
				{
					String name = declaration.getName();
					if(name == null)
					{
						return;
					}

					if(myExtensionMap == null)
					{
						myExtensionMap = new MultiMap<String, CSharpMethodDeclaration>();
					}

					myExtensionMap.putValue(name, declaration);
				}

				putIfNotNull(declaration.getName(), declaration, myOtherElements);
			}
		}

		@Override
		public void visitArrayMethodDeclaration(CSharpArrayMethodDeclaration declaration)
		{
			// we dont interest in private impl
			if(declaration.getTypeForImplement() != null)
			{
				return;
			}

			if(myIndexMethods == null)
			{
				myIndexMethods = new SmartList<CSharpArrayMethodDeclaration>();
			}
			myIndexMethods.add(GenericUnwrapTool.extract(declaration, myGenericExtractor, false));
		}

		@Override
		public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitEventDeclaration(CSharpEventDeclaration declaration)
		{
			// we dont interest in private impl
			if(declaration.getTypeForImplement() != null)
			{
				return;
			}

			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
		{
			// we dont interest in private impl
			if(declaration.getTypeForImplement() != null)
			{
				return;
			}

			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		@Override
		public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
		{
			putIfNotNull(declaration.getName(), declaration, myOtherElements);
		}

		private <K, V extends DotNetNamedElement> void putIfNotNull(@Nullable K key, @NotNull V value, @NotNull MultiMap<K, PsiElement> map)
		{
			if(key == null)
			{
				return;
			}
			map.putValue(key, GenericUnwrapTool.extract(value, myGenericExtractor, false));
		}
	}

	@Nullable
	private CSharpElementGroup<CSharpArrayMethodDeclaration> myIndexMethodGroup;
	@Nullable
	private CSharpElementGroup<CSharpConstructorDeclaration> myConstructorGroup;
	@Nullable
	private CSharpElementGroup<CSharpConstructorDeclaration> myDeConstructorGroup;

	@Nullable
	private final Map<DotNetTypeRef, CSharpElementGroup<CSharpConversionMethodDeclaration>> myConversionMap;
	@Nullable
	private final Map<IElementType, CSharpElementGroup<CSharpMethodDeclaration>> myOperatorMap;
	@Nullable
	private final Map<String, CSharpElementGroup<CSharpMethodDeclaration>> myExtensionMap;
	@Nullable
	private final Map<String, CSharpElementGroup<PsiElement>> myOtherElements;

	public CSharpTypeResolveContext(@NotNull CSharpTypeDeclaration typeDeclaration, @NotNull DotNetGenericExtractor genericExtractor)
	{
		val project = typeDeclaration.getProject();

		val collector = new Collector(genericExtractor);

		DotNetNamedElement[] members = typeDeclaration.getMembers();
		for(DotNetNamedElement member : members)
		{
			member.accept(collector);
		}

		for(CSharpAdditionalTypeMemberProvider provider : getAdditionalTypeMemberProviders())
		{
			for(DotNetElement element : provider.getAdditionalMembers(typeDeclaration))
			{
				element.accept(collector);
			}
		}

		myOtherElements = convertToGroup(project, collector.myOtherElements);
		myIndexMethodGroup = toGroup(project, "[]", collector.myIndexMethods);
		myConstructorGroup = toGroup(project, MsilHelper.CONSTRUCTOR_NAME, collector.myConstructors);
		myDeConstructorGroup = toGroup(project, "~" + MsilHelper.CONSTRUCTOR_NAME, collector.myDeConstructors);
		myOperatorMap = convertToGroup(project, collector.myOperatorsMap);
		myExtensionMap = convertToGroup(project, collector.myExtensionMap);
		myConversionMap = convertToGroup(project, collector.myConversionMap);
	}

	@NotNull
	@LazyInstance
	private static CSharpAdditionalTypeMemberProvider[] getAdditionalTypeMemberProviders()
	{
		return CSharpAdditionalTypeMemberProvider.EP_NAME.getExtensions();
	}

	@Nullable
	private static <T extends PsiElement> CSharpElementGroup<T> toGroup(@NotNull Project project, @NotNull String key,
			@Nullable List<T> elements)
	{
		if(ContainerUtil.isEmpty(elements))
		{
			return null;
		}
		return new CSharpElementGroupImpl<T>(project, key, elements);
	}

	@Nullable
	public static <K, V extends PsiElement> Map<K, CSharpElementGroup<V>> convertToGroup(@NotNull Project project, @Nullable MultiMap<K, V> multiMap)
	{
		if(multiMap == null || multiMap.isEmpty())
		{
			return null;
		}
		Map<K, CSharpElementGroup<V>> map = new THashMap<K, CSharpElementGroup<V>>(multiMap.size());
		for(Map.Entry<K, Collection<V>> entry : multiMap.entrySet())
		{
			map.put(entry.getKey(), new CSharpElementGroupImpl<V>(project, entry.getKey(), entry.getValue()));
		}
		return map;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup()
	{
		return myIndexMethodGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return myConstructorGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return myDeConstructorGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type)
	{
		if(myOperatorMap == null)
		{
			return null;
		}
		return myOperatorMap.get(type);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef)
	{
		if(myConversionMap == null)
		{
			return null;
		}
		return myConversionMap.get(typeRef);
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		if(myExtensionMap == null)
		{
			return null;
		}
		return myExtensionMap.get(name);
	}

	@NotNull
	@Override
	public Collection<CSharpElementGroup<CSharpMethodDeclaration>> getExtensionMethodGroups()
	{
		if(myExtensionMap == null)
		{
			return Collections.emptyList();
		}
		return myExtensionMap.values();
	}

	@Override
	@Nullable
	public PsiElement findByName(@NotNull String name, @NotNull UserDataHolder holder)
	{
		if(myOtherElements == null)
		{
			return null;
		}
		return myOtherElements.get(name);
	}

	@NotNull
	@Override
	public Collection<? extends PsiElement> getElements()
	{
		return myOtherElements == null ? Collections.<PsiElement>emptyList() : myOtherElements.values();
	}
}
