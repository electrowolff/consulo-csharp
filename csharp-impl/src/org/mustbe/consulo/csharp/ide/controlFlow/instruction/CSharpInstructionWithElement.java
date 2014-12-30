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

package org.mustbe.consulo.csharp.ide.controlFlow.instruction;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CSharpInstructionWithElement<T extends PsiElement> extends CSharpInstruction
{
	private T myElement;

	public CSharpInstructionWithElement(@Nullable T element)
	{
		myElement = element;
	}

	@Nullable
	public T getElement()
	{
		return myElement;
	}

	@Override
	public String toString()
	{
		return super.toString() + " | " + StringUtil.escapeLineBreak(myElement == null ? "null" : myElement.getText());
	}
}
