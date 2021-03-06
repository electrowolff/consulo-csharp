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

package org.mustbe.consulo.csharp.module.extension;

import org.consulo.module.extension.impl.ModuleInheritableNamedPointerImpl;
import org.consulo.util.pointers.NamedPointer;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.sdk.SdkUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;

/**
 * @author VISTALL
 * @since 08.06.15
 */
public class CSharpCustomCompilerSdkPointer extends ModuleInheritableNamedPointerImpl<Sdk>
{
	private final String myExtensionId;

	public CSharpCustomCompilerSdkPointer(@NotNull Project project, @NotNull String id)
	{
		super(project, "custom-compiler-sdk");
		myExtensionId = id;
	}

	@Override
	public String getItemNameFromModule(@NotNull Module module)
	{
		final CSharpModuleExtension<?> extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getCustomCompilerSdkPointer().getName();
		}
		return null;
	}

	@Override
	public Sdk getItemFromModule(@NotNull Module module)
	{
		final CSharpModuleExtension<?> extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getCustomCompilerSdkPointer().get();
		}
		return null;
	}

	@NotNull
	@Override
	public NamedPointer<Sdk> getPointer(@NotNull Project project, @NotNull String name)
	{
		return SdkUtil.createPointer(name);
	}

	@Override
	public Sdk getDefaultValue()
	{
		return null;
	}
}
