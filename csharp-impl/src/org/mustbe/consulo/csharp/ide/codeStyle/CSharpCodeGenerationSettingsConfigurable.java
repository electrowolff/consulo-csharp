package org.mustbe.consulo.csharp.ide.codeStyle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;

/**
 * @author VISTALL
 * @since 28.07.2015
 */
public class CSharpCodeGenerationSettingsConfigurable implements Configurable
{
	private CSharpCodeGenerationSettings mySettings;
	private JTextField myFieldPrefixField;
	private JTextField myStaticFieldPrefixField;
	private JTextField myPropertyPrefixField;
	private JTextField myStaticPropertyPrefixField;
	private JTextField myFieldSuffixField;
	private JTextField myStaticFieldSuffixField;
	private JTextField myPropertySuffixField;
	private JTextField myStaticPropertySuffixField;
	private JCheckBox myUseLanguageKeywordsCheckBox;
	private JPanel myRoot;

	public CSharpCodeGenerationSettingsConfigurable(CodeStyleSettings settings)
	{
		mySettings = settings.getCustomSettings(CSharpCodeGenerationSettings.class);
	}

	@Nls
	@Override
	public String getDisplayName()
	{
		return ApplicationBundle.message("title.code.generation");
	}

	@Nullable
	@Override
	public String getHelpTopic()
	{
		return null;
	}

	@RequiredDispatchThread
	@Nullable
	@Override
	public JComponent createComponent()
	{
		return myRoot;
	}

	@RequiredDispatchThread
	@Override
	public boolean isModified()
	{
		boolean isModified = isModified(myFieldPrefixField, mySettings.FIELD_PREFIX);
		isModified |= isModified(myStaticFieldPrefixField, mySettings.STATIC_FIELD_PREFIX);
		isModified |= isModified(myPropertyPrefixField, mySettings.PROPERTY_PREFIX);
		isModified |= isModified(myStaticPropertyPrefixField, mySettings.STATIC_PROPERTY_PREFIX);

		isModified |= isModified(myFieldSuffixField, mySettings.FIELD_SUFFIX);
		isModified |= isModified(myStaticFieldSuffixField, mySettings.STATIC_FIELD_SUFFIX);
		isModified |= isModified(myPropertySuffixField, mySettings.PROPERTY_SUFFIX);
		isModified |= isModified(myStaticPropertySuffixField, mySettings.STATIC_PROPERTY_SUFFIX);
		isModified |= myUseLanguageKeywordsCheckBox.isSelected() != mySettings.USE_LANGUAGE_DATA_TYPES;
		return isModified;
	}

	private static boolean isModified(JTextField textField, String value)
	{
		return !textField.getText().trim().equals(value);
	}

	@RequiredDispatchThread
	@Override
	public void apply() throws ConfigurationException
	{
		mySettings.FIELD_PREFIX = myFieldPrefixField.getText().trim();
		mySettings.STATIC_FIELD_PREFIX = myStaticFieldPrefixField.getText().trim();
		mySettings.PROPERTY_PREFIX = myPropertyPrefixField.getText().trim();
		mySettings.STATIC_PROPERTY_PREFIX = myStaticPropertyPrefixField.getText().trim();

		mySettings.FIELD_SUFFIX = myFieldSuffixField.getText().trim();
		mySettings.STATIC_FIELD_SUFFIX = myStaticFieldSuffixField.getText().trim();
		mySettings.PROPERTY_SUFFIX = myPropertySuffixField.getText().trim();
		mySettings.STATIC_PROPERTY_SUFFIX = myStaticPropertySuffixField.getText().trim();
		mySettings.USE_LANGUAGE_DATA_TYPES = myUseLanguageKeywordsCheckBox.isSelected();
	}

	@RequiredDispatchThread
	@Override
	public void reset()
	{
		myFieldPrefixField.setText(mySettings.FIELD_PREFIX);
		myStaticFieldPrefixField.setText(mySettings.STATIC_FIELD_PREFIX);
		myPropertyPrefixField.setText(mySettings.PROPERTY_PREFIX);
		myStaticPropertyPrefixField.setText(mySettings.STATIC_PROPERTY_PREFIX);

		myFieldSuffixField.setText(mySettings.FIELD_SUFFIX);
		myStaticFieldSuffixField.setText(mySettings.STATIC_FIELD_SUFFIX);
		myPropertySuffixField.setText(mySettings.PROPERTY_SUFFIX);
		myStaticPropertySuffixField.setText(mySettings.STATIC_PROPERTY_SUFFIX);
		myUseLanguageKeywordsCheckBox.setSelected(mySettings.USE_LANGUAGE_DATA_TYPES);
	}

	@RequiredDispatchThread
	@Override
	public void disposeUIResources()
	{

	}
}
