package org.mustbe.consulo.csharp.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompileFailedException;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ui.configuration.SdkComboBox;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public abstract class CSharpCompilerProvider
{
	public static final ExtensionPointName<CSharpCompilerProvider> EP_NAME = ExtensionPointName.create("org.mustbe.consulo.csharp.compilerProvider");

	@Nullable
	public abstract SdkType getBundleType(@NotNull DotNetSimpleModuleExtension<?> moduleExtension);

	public void insertCustomSdkItems(@Nullable DotNetSimpleModuleExtension extension, @NotNull SdkComboBox comboBox)
	{
	}

	public abstract void setupCompiler(@NotNull DotNetModuleExtension<?> netExtension,
			@NotNull CSharpModuleExtension<?> csharpExtension,
			@NotNull MSBaseDotNetCompilerOptionsBuilder builder,
			@Nullable VirtualFile compilerSdkHome) throws DotNetCompileFailedException;

	protected final void setExecutable(CSharpModuleExtension cSharpModuleExtension, DotNetCompilerOptionsBuilder builder, @Nullable VirtualFile executable) throws DotNetCompileFailedException
	{
		if(executable == null)
		{
			throw new DotNetCompileFailedException("Compiler is not resolved");
		}

		cSharpModuleExtension.setCompilerExecutable(builder, executable);
	}

	public boolean isSelected(@NotNull DotNetSimpleModuleExtension<?> moduleExtension, @NotNull String name, @Nullable Sdk sdk)
	{
		return sdk != null && getBundleType(moduleExtension) == sdk.getSdkType();
	}
}
