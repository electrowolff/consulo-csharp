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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class AddInternalModifierFix extends AddAccessModifierFix
{
	public AddInternalModifierFix()
	{
		super(CSharpModifier.INTERNAL);
	}

	@RequiredReadAction
	@Override
	protected boolean hasModifiers(DotNetModifierListOwner owner)
	{
		if(owner.hasModifier(CSharpModifier.PROTECTED) && owner.hasModifier(DotNetModifier.INTERNAL))
		{
			//if we have protected internal - it not our, return true
			return false;
		}
		return super.hasModifiers(owner);
	}
}
