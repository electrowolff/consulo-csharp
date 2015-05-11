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

package org.mustbe.consulo.csharp.ide.msil.representation.builder;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import org.mustbe.dotnet.msil.decompiler.textBuilder.block.LineStubBlock;
import org.mustbe.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.PairFunction;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public class CSharpStubBuilderVisitor extends CSharpElementVisitor
{
	@NotNull
	public static List<StubBlock> buildBlocks(PsiElement qualifiedElement)
	{
		CSharpStubBuilderVisitor visitor = new CSharpStubBuilderVisitor();
		qualifiedElement.accept(visitor);
		return visitor.getBlocks();
	}

	private List<StubBlock> myBlocks = new ArrayList<StubBlock>(2);

	@Override
	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration);
		processModifierList(builder, declaration);
		appendTypeRef(declaration, builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendName(declaration, builder);
		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		for(DotNetXXXAccessor dotNetXXXAccessor : declaration.getAccessors())
		{
			e.getBlocks().addAll(buildBlocks(dotNetXXXAccessor));
		}
		myBlocks.add(e);
	}

	@Override
	public void visitXXXAccessor(DotNetXXXAccessor accessor)
	{
		DotNetXXXAccessor.Kind accessorKind = accessor.getAccessorKind();
		if(accessorKind == null)
		{
			return;
		}

		StringBuilder builder = new StringBuilder();

		processModifierList(builder, accessor);

		builder.append(accessorKind.name().toLowerCase());

		boolean canHaveBody = !accessor.hasModifier(CSharpModifier.ABSTRACT);

		if(canHaveBody)
		{
			builder.append("; //compiled code\n");
		}
		else
		{
			builder.append(";\n");
		}
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
	{
		processAttributeListAsLine(declaration);
		StringBuilder builder = new StringBuilder();
		builder.append(declaration.getName());
		builder.append(",\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration);
		processModifierList(builder, declaration);
		if(declaration.isConstant())
		{
			builder.append("const ");
		}
		appendTypeRef(declaration, builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendValidName(builder, declaration.getName());
		builder.append(";\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitEventDeclaration(CSharpEventDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration);
		processModifierList(builder, declaration);
		builder.append("event ");
		appendTypeRef(declaration, builder, declaration.toTypeRef(false));
		builder.append(" ");
		appendName(declaration, builder);

		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		for(DotNetXXXAccessor dotNetXXXAccessor : declaration.getAccessors())
		{
			e.getBlocks().addAll(buildBlocks(dotNetXXXAccessor));
		}
		myBlocks.add(e);
	}

	@Override
	public void visitArrayMethodDeclaration(CSharpArrayMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processAttributeListAsLine(declaration);
		processModifierList(builder, declaration);
		appendTypeRef(declaration, builder, declaration.getReturnTypeRef());
		builder.append(" ");
		appendName(declaration, builder);
		processParameterList(declaration, builder, '[', ']');

		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		for(DotNetXXXAccessor dotNetXXXAccessor : declaration.getAccessors())
		{
			e.getBlocks().addAll(buildBlocks(dotNetXXXAccessor));
		}
		myBlocks.add(e);
	}

	@Override
	public void visitConversionMethodDeclaration(CSharpConversionMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processAttributeListAsLine(declaration);
		processModifierList(builder, declaration);
		appendTypeRef(declaration, builder, declaration.getConversionTypeRef());
		builder.append(" ");
		builder.append("operator ");
		appendTypeRef(declaration, builder, declaration.getReturnTypeRef());
		processParameterList(declaration, builder, '(', ')');
		builder.append("; //compiled code\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		if(declaration.isDeConstructor())
		{
			builder.append("~");
		}
		else
		{
			processAttributeListAsLine(declaration);
			processModifierList(builder, declaration);
		}

		builder.append(declaration.getName());
		processParameterList(declaration, builder, '(', ')');
		builder.append("; //compiled code\n");
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();

		processAttributeListAsLine(declaration);
		processModifierList(builder, declaration);

		if(declaration.isDelegate())
		{
			builder.append("delegate ");
		}
		appendTypeRef(declaration, builder, declaration.getReturnTypeRef());
		builder.append(" ");
		if(declaration.isOperator())
		{
			builder.append("operator ");
		}
		appendName(declaration, builder);
		processGenericParameterList(builder, declaration);
		processParameterList(declaration, builder, '(', ')');
		processGenericConstraintList(builder, declaration);

		boolean canHaveBody = !declaration.hasModifier(CSharpModifier.ABSTRACT) && !declaration.isDelegate();

		if(canHaveBody)
		{
			builder.append("; //compiled code\n");
		}
		else
		{
			builder.append(";\n");
		}
		myBlocks.add(new LineStubBlock(builder));
	}

	@Override
	public void visitTypeDeclaration(final CSharpTypeDeclaration declaration)
	{
		StringBuilder builder = new StringBuilder();
		processAttributeListAsLine(declaration);
		processModifierList(builder, declaration);

		if(declaration.isEnum())
		{
			builder.append("enum ");
		}
		else if(declaration.isStruct())
		{
			builder.append("struct ");
		}
		else if(declaration.isInterface())
		{
			builder.append("interface ");
		}
		else
		{
			builder.append("class ");
		}

		builder.append(declaration.getName());
		processGenericParameterList(builder, declaration);

		if(declaration.isEnum())
		{
			DotNetTypeRef typeRefForEnumConstants = declaration.getTypeRefForEnumConstants();
			if(!DotNetTypeRefUtil.isVmQNameEqual(typeRefForEnumConstants, declaration, DotNetTypes.System.Int32))
			{
				builder.append(" : ");
				appendTypeRef(declaration, builder, typeRefForEnumConstants);
			}
		}
		else
		{
			DotNetTypeRef[] extendTypeRefs = declaration.getExtendTypeRefs();
			List<DotNetTypeRef> temp = ContainerUtil.filter(extendTypeRefs, new Condition<DotNetTypeRef>()
			{
				@Override
				public boolean value(DotNetTypeRef typeRef)
				{
					return !DotNetTypeRefUtil.isVmQNameEqual(typeRef, declaration, DotNetTypes.System.Object) && !DotNetTypeRefUtil.isVmQNameEqual
							(typeRef, declaration, DotNetTypes.System.ValueType);
				}
			});

			if(!temp.isEmpty())
			{
				builder.append(" : ");

				StubBlockUtil.join(builder, temp, new PairFunction<StringBuilder, DotNetTypeRef, Void>()
				{
					@Nullable
					@Override
					public Void fun(StringBuilder builder, DotNetTypeRef typeRef)
					{
						appendTypeRef(declaration, builder, typeRef);
						return null;
					}
				}, ", ");
			}
		}
		processGenericConstraintList(builder, declaration);
		StubBlock e = new StubBlock(builder, null, StubBlock.BRACES);
		myBlocks.add(e);

		for(DotNetNamedElement dotNetNamedElement : declaration.getMembers())
		{
			e.getBlocks().addAll(buildBlocks(dotNetNamedElement));
		}
	}

	private static void appendTypeRef(@NotNull final PsiElement scope, @NotNull StringBuilder builder, @NotNull DotNetTypeRef typeRef)
	{
		CSharpTypeRefPresentationUtil.appendTypeRef(scope, builder, typeRef, CSharpTypeRefPresentationUtil.QUALIFIED_NAME |
				CSharpTypeRefPresentationUtil.TYPE_KEYWORD);
	}

	private static <T extends DotNetVirtualImplementOwner & DotNetNamedElement> void appendName(T element, StringBuilder builder)
	{
		DotNetTypeRef typeRefForImplement = element.getTypeRefForImplement();
		if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
		{
			appendTypeRef(element, builder, typeRefForImplement);
			builder.append(".");
			if(element instanceof CSharpArrayMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				appendValidName(builder, element.getName());
			}
		}
		else
		{
			if(element instanceof CSharpArrayMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				appendValidName(builder, element.getName());
			}
		}
	}

	private static void processGenericParameterList(StringBuilder builder, DotNetGenericParameterListOwner owner)
	{
		DotNetGenericParameter[] genericParameters = owner.getGenericParameters();
		if(genericParameters.length == 0)
		{
			return;
		}
		builder.append("<");
		StubBlockUtil.join(builder, genericParameters, new PairFunction<StringBuilder, DotNetGenericParameter, Void>()
		{
			@Nullable
			@Override
			public Void fun(StringBuilder t, DotNetGenericParameter v)
			{
				if(v.hasModifier(CSharpModifier.OUT))
				{
					t.append("out ");
				}
				else if(v.hasModifier(CSharpModifier.IN))
				{
					t.append("in ");
				}
				t.append(v.getName());
				return null;
			}
		}, ", ");
		builder.append(">");
	}

	private static void processGenericConstraintList(final StringBuilder builder, final CSharpGenericConstraintOwner owner)
	{
		CSharpGenericConstraint[] genericConstraints = owner.getGenericConstraints();
		if(genericConstraints.length == 0)
		{
			return;
		}

		builder.append(" ");
		StubBlockUtil.join(builder, genericConstraints, new PairFunction<StringBuilder, CSharpGenericConstraint, Void>()
		{
			@Nullable
			@Override
			public Void fun(final StringBuilder builder, CSharpGenericConstraint v)
			{
				builder.append("where ");
				DotNetGenericParameter resolve = v.resolve();
				assert resolve != null;
				builder.append(resolve.getName());
				builder.append(" : ");

				CSharpGenericConstraintValue[] genericConstraintValues = v.getGenericConstraintValues();
				for(CSharpGenericConstraintValue genericConstraintValue : genericConstraintValues)
				{
					genericConstraintValue.accept(new CSharpElementVisitor()
					{
						@Override
						public void visitGenericConstraintKeywordValue(CSharpGenericConstraintKeywordValue value)
						{
							IElementType keywordElementType = value.getKeywordElementType();
							if(keywordElementType == CSharpTokens.STRUCT_KEYWORD)
							{
								builder.append("struct");
							}
							else if(keywordElementType == CSharpTokens.CLASS_KEYWORD)
							{
								builder.append("class");
							}
							else if(keywordElementType == CSharpTokens.NEW_KEYWORD)
							{
								builder.append("new()");
							}
						}

						@Override
						public void visitGenericConstraintTypeValue(CSharpGenericConstraintTypeValue value)
						{
							appendTypeRef(owner, builder, value.toTypeRef());
						}
					});
				}
				return null;
			}
		}, " ");
	}

	private static void processParameterList(final DotNetParameterListOwner declaration, StringBuilder builder, char p1, char p2)
	{
		builder.append(p1);
		StubBlockUtil.join(builder, declaration.getParameters(), new PairFunction<StringBuilder, DotNetParameter, Void>()
		{
			@Nullable
			@Override
			public Void fun(StringBuilder t, DotNetParameter v)
			{
				appendAttributeList(t, v);
				processModifierList(t, v);
				DotNetTypeRef typeRef = v.toTypeRef(false);
				appendTypeRef(declaration, t, typeRef);
				if(typeRef != CSharpStaticTypeRef.__ARGLIST_TYPE)
				{
					t.append(" ");
					appendValidName(t, v.getName());

					DotNetExpression initializer = v.getInitializer();
					if(initializer != null)
					{
						t.append(" = ");
						t.append(initializer.getText());
					}
				}
				return null;
			}
		}, ", ");
		builder.append(p2);
	}

	private void processAttributeListAsLine(DotNetModifierListOwner owner)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		for(DotNetAttribute dotNetAttribute : modifierList.getAttributes())
		{
			StringBuilder builder = new StringBuilder();
			builder.append("[");
			appendTypeRef(owner, builder, dotNetAttribute.toTypeRef());

			if(dotNetAttribute instanceof CSharpAttribute)
			{
				DotNetExpression[] parameterExpressions = ((CSharpAttribute) dotNetAttribute).getParameterExpressions();
				if(parameterExpressions.length > 0)
				{
					builder.append("(");
					StubBlockUtil.join(builder, parameterExpressions, new PairFunction<StringBuilder, DotNetExpression, Void>()
					{
						@Nullable
						@Override
						public Void fun(StringBuilder builder, DotNetExpression dotNetExpression)
						{
							builder.append(dotNetExpression.getText());
							return null;
						}
					}, ", ");

					builder.append(")");
				}
			}
			builder.append("]");
			myBlocks.add(new LineStubBlock(builder));
		}
	}

	private static void appendAttributeList(final StringBuilder builder, final DotNetModifierListOwner owner)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		DotNetAttribute[] attributes = modifierList.getAttributes();
		if(attributes.length == 0)
		{
			return;
		}

		builder.append("[");
		StubBlockUtil.join(builder, attributes, new PairFunction<StringBuilder, DotNetAttribute, Void>()
		{
			@Override
			public Void fun(StringBuilder builder, DotNetAttribute dotNetAttribute)
			{
				appendTypeRef(owner, builder, dotNetAttribute.toTypeRef());

				if(dotNetAttribute instanceof CSharpAttribute)
				{
					DotNetExpression[] parameterExpressions = ((CSharpAttribute) dotNetAttribute).getParameterExpressions();
					if(parameterExpressions.length > 0)
					{
						builder.append("(");

						StubBlockUtil.join(builder, parameterExpressions, new PairFunction<StringBuilder, DotNetExpression, Void>()
						{
							@Nullable
							@Override
							public Void fun(StringBuilder builder, DotNetExpression dotNetExpression)
							{
								builder.append(dotNetExpression.getText());
								return null;
							}
						}, ", ");

						builder.append(")");
					}
				}
				return null;
			}
		}, ", ");
		builder.append("] ");
	}

	private static void processModifierList(StringBuilder builder, DotNetModifierListOwner owner)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		for(DotNetModifier dotNetModifier : modifierList.getModifiers())
		{
			if(dotNetModifier == CSharpModifier.OUT || dotNetModifier == CSharpModifier.IN)
			{
				continue;
			}

			if(owner instanceof DotNetVariable && ((DotNetVariable) owner).isConstant() && dotNetModifier == CSharpModifier.STATIC)
			{
				continue;
			}

			if(dotNetModifier == CSharpModifier.ABSTRACT && (isInterface(owner) || isInterface(owner.getParent())))
			{
				continue;
			}

			if(dotNetModifier == CSharpModifier.PUBLIC && isInterface(owner.getParent()))
			{
				continue;
			}

			if((dotNetModifier == CSharpModifier.ABSTRACT || dotNetModifier == CSharpModifier.PUBLIC) && owner instanceof DotNetXXXAccessor &&
					isInterface(owner.getParent().getParent()))
			{
				continue;
			}

			builder.append(dotNetModifier.getPresentableText()).append(" ");
		}
	}

	private static boolean isInterface(@Nullable PsiElement element)
	{
		return element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).isInterface();
	}

	public static void appendValidName(StringBuilder builder, String name)
	{
		if(CSharpNameSuggesterUtil.isKeyword(name))
		{
			builder.append("@");
		}
		builder.append(name);
	}

	public List<StubBlock> getBlocks()
	{
		return myBlocks;
	}
}
