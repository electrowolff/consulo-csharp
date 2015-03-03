/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

/* The following code was generated by JFlex 1.4 on 3/14/05 5:43 PM */

/* It's an automatically generated code. Do not modify it. */
package org.mustbe.consulo.csharp.lang.doc.lexer;

import com.intellij.lexer.FlexAdapter;

public class _CSharpDocLexer extends FlexAdapter
{
	private int myState = __CSharpDocLexer.YYINITIAL;

	public _CSharpDocLexer(final __CSharpDocLexer flexLexer)
	{
		this(flexLexer, false);
	}

	public _CSharpDocLexer(final __CSharpDocLexer flexLexer, final boolean conditionalCommentsSupport)
	{
		super(flexLexer);
		flexLexer.setConditionalCommentsSupport(conditionalCommentsSupport);
	}

	private void packState()
	{
		final __CSharpDocLexer flex = (__CSharpDocLexer) getFlex();
		myState = ((flex.yyprevstate() & 15) << 4) | (flex.yystate() & 15);
	}

	private void handleState(final int initialState)
	{
		final __CSharpDocLexer flex = (__CSharpDocLexer) getFlex();
		flex.yybegin(initialState & 15);
		flex.pushState((initialState >> 4) & 15);
		packState();
	}

	@Override
	public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState)
	{
		super.start(buffer, startOffset, endOffset, initialState);
		handleState(initialState);
	}

	@Override
	public int getState()
	{
		return myState;
	}

	@Override
	public void advance()
	{
		super.advance();
		packState();
	}
}