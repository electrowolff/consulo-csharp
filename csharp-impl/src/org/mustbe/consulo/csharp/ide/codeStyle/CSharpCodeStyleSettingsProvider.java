package org.mustbe.consulo.csharp.ide.codeStyle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.options.Configurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * @author VISTALL
 * @since 11.11.14
 */
public class CSharpCodeStyleSettingsProvider extends CodeStyleSettingsProvider
{
	@Nullable
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}

	@Nullable
	@Override
	public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings)
	{
		return new CSharpCodeStyleSettings(settings);
	}

	@NotNull
	@Override
	public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings)
	{
		return new CSharpCodeStyleConfigurable(settings, originalSettings);
	}
}
