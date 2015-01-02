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

import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;

/**
* @author VISTALL
* @since 30.12.14
*/
public class BaseLikeMethodGenerateContext
{
	private CSharpReferenceExpression myExpression;
	private DotNetMemberOwner myTargetForGenerate;

	public BaseLikeMethodGenerateContext(CSharpReferenceExpression expression, DotNetMemberOwner targetForGenerate)
	{
		myExpression = expression;
		myTargetForGenerate = targetForGenerate;
	}

	public CSharpReferenceExpression getExpression()
	{
		return myExpression;
	}

	public DotNetMemberOwner getTargetForGenerate()
	{
		return myTargetForGenerate;
	}
}