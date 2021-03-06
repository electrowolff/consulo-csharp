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

package org.mustbe.consulo.csharp.ide.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import org.consulo.psi.PsiPackage;
import org.consulo.psi.PsiPackageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.assemblyInfo.CSharpAssemblyConstants;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptor;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.actions.CreateFromTemplateAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpCreateFileAction extends CreateFromTemplateAction<PsiFile>
{
	public CSharpCreateFileAction()
	{
		super(null, null, CSharpFileType.INSTANCE.getIcon());
	}

	@Override
	@RequiredDispatchThread
	protected boolean isAvailable(DataContext dataContext)
	{
		Module module = findModule(dataContext);
		if(module != null)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(module, DotNetModuleExtension.class);
			if(extension != null && extension.isAllowSourceRoots())
			{
				final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
				if(view == null)
				{
					return false;
				}

				PsiDirectory orChooseDirectory = view.getOrChooseDirectory();
				if(orChooseDirectory == null)
				{
					return false;
				}
				PsiPackage aPackage = PsiPackageManager.getInstance(module.getProject()).findPackage(orChooseDirectory, DotNetModuleExtension.class);

				if(aPackage == null)
				{
					return false;
				}
			}
		}
		return module != null && ModuleUtilCore.getExtension(module, CSharpSimpleModuleExtension.class) != null;
	}

	@RequiredReadAction
	private static Module findModule(DataContext dataContext)
	{
		Project project = CommonDataKeys.PROJECT.getData(dataContext);
		assert project != null;
		final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
		if(view == null)
		{
			return null;
		}

		final PsiDirectory orChooseDirectory = view.getOrChooseDirectory();
		if(orChooseDirectory == null)
		{
			return null;
		}

		Module resolve = CSharpCreateFromTemplateHandler.findModuleByPsiDirectory(orChooseDirectory);
		if(resolve != null)
		{
			return resolve;
		}
		return LangDataKeys.MODULE.getData(dataContext);
	}

	@Override
	@RequiredReadAction
	protected PsiFile createFile(String name, String templateName, final PsiDirectory dir)
	{
		FileTemplate template = FileTemplateManager.getInstance().getInternalTemplate(templateName);
		try
		{
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("psiDirectory", dir);

			return (PsiFile) FileTemplateUtil.createFromTemplate(template, name, map, dir, getClass().getClassLoader());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	@Override
	@RequiredDispatchThread
	protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder)
	{
		Set<String> used = new HashSet<String>();
		addKind(builder, used, "Class", new IconDescriptor(AllIcons.Nodes.Class).toIcon(), "CSharpClass");
		addKind(builder, used, "Interface", new IconDescriptor(AllIcons.Nodes.Interface).toIcon(), "CSharpInterface");
		addKind(builder, used, "Enum", new IconDescriptor(AllIcons.Nodes.Enum).toIcon(), "CSharpEnum");
		addKind(builder, used, "Struct", new IconDescriptor(AllIcons.Nodes.Struct).toIcon(), "CSharpStruct");
		addKind(builder, used, "Attribute", new IconDescriptor(AllIcons.Nodes.Attribute).toIcon(), "CSharpAttribute");
		if(isCreationOfAssemblyFileAvailable(psiDirectory))
		{
			addKind(builder, used, "Assembly File", AllIcons.FileTypes.Config, "CSharpAssemblyFile");
		}
		addKind(builder, used, "Empty File", CSharpFileType.INSTANCE.getIcon(), "CSharpFile");

		final CSharpCreateFromTemplateHandler handler = CSharpCreateFromTemplateHandler.getInstance();
		for(FileTemplate template : FileTemplateManager.getInstance().getAllTemplates())
		{
			if(handler.handlesTemplate(template))
			{
				String name = template.getName().replaceFirst("CSharp", "");
				if(!used.add(name))
				{
					name = template.getName();
				}
				addKind(builder, used, name, CSharpFileType.INSTANCE.getIcon(), template.getName());
			}
		}

		builder.setTitle("Create New File");
	}

	private static void addKind(CreateFileFromTemplateDialog.Builder builder, @NotNull Set<String> used, @NotNull String kind, @Nullable Icon icon, @NotNull String templateName)
	{
		used.add(kind);

		builder.addKind(kind, icon, templateName);
	}

	@RequiredReadAction
	private static boolean isCreationOfAssemblyFileAvailable(PsiDirectory directory)
	{
		Module module = ModuleUtilCore.findModuleForPsiElement(directory);
		if(module != null)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(module, DotNetModuleExtension.class);
			if(extension != null && extension.isAllowSourceRoots())
			{
				return false;
			}
		}
		if(module == null || ModuleUtilCore.getExtension(module, CSharpSimpleModuleExtension.class) == null)
		{
			return false;
		}

		final Ref<VirtualFile> ref = Ref.create();
		VirtualFile moduleDir = module.getModuleDir();
		if(moduleDir == null)
		{
			return false;
		}
		VfsUtil.visitChildrenRecursively(moduleDir, new VirtualFileVisitor<Object>()
		{
			@Override
			public boolean visitFile(@NotNull VirtualFile file)
			{
				if(file.getName().equals(CSharpAssemblyConstants.FileName))
				{
					ref.set(file);
					return false;
				}
				return true;
			}
		});

		return ref.get() == null;
	}

	@Override
	protected String getActionName(PsiDirectory psiDirectory, String s, String s2)
	{
		return "Create C# File";
	}
}