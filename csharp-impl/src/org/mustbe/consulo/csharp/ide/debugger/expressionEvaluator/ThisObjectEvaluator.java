/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.dotnet.debugger.nodes.DotNetDebuggerCompilerGenerateUtil;
import org.mustbe.consulo.dotnet.debugger.proxy.DotNetStackFrameMirrorProxy;
import com.intellij.openapi.util.Condition;
import com.intellij.util.ObjectUtil;
import com.intellij.util.containers.ContainerUtil;
import mono.debugger.FieldMirror;
import mono.debugger.ObjectValueMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class ThisObjectEvaluator extends Evaluator
{
	public static final ThisObjectEvaluator INSTANCE = new ThisObjectEvaluator();

	private ThisObjectEvaluator()
	{
	}

	@Override
	public void evaluate(@NotNull CSharpEvaluateContext context)
	{
		DotNetStackFrameMirrorProxy frame = context.getFrame();

		context.pull(calcThisObject(frame, frame.thisObject()), null);
	}

	@NotNull
	public static Value<?> calcThisObject(@NotNull DotNetStackFrameMirrorProxy proxy, Value<?> thisObject)
	{
		return ObjectUtil.notNull(tryToFindObjectInsideYieldOrAsyncThis(proxy, thisObject), thisObject);
	}

	@Nullable
	private static Value<?> tryToFindObjectInsideYieldOrAsyncThis(@NotNull DotNetStackFrameMirrorProxy proxy, Value thisObject)
	{
		if(!(thisObject instanceof ObjectValueMirror))
		{
			return null;
		}

		ObjectValueMirror objectValueMirror = (ObjectValueMirror) thisObject;

		TypeMirror type;
		try
		{
			type = thisObject.type();
			assert type != null;

			if(DotNetDebuggerCompilerGenerateUtil.isYieldOrAsyncNestedType(type))
			{
				TypeMirror parentType = type.parentType();

				if(parentType == null)
				{
					return null;
				}

				FieldMirror[] fields = type.fields();

				final FieldMirror thisFieldMirror = ContainerUtil.find(fields, new Condition<FieldMirror>()
				{
					@Override
					public boolean value(FieldMirror fieldMirror)
					{
						String name = fieldMirror.name();
						return DotNetDebuggerCompilerGenerateUtil.isYieldOrAsyncThisField(name);
					}
				});

				if(thisFieldMirror != null)
				{
					Value<?> value = thisFieldMirror.value(proxy.thread(), objectValueMirror);
					if(value != null)
					{
						return value;
					}
				}
			}
		}
		catch(Exception ignored)
		{
		}
		return null;
	}
}
