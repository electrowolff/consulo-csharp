package org.mustbe.consulo.csharp.ide.debugger;

import java.util.List;
import java.util.Set;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.Evaluator;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.fragment.CSharpFragmentFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.dotnet.debugger.DotNetDebugContext;
import org.mustbe.consulo.dotnet.debugger.DotNetDebuggerProvider;
import org.mustbe.consulo.dotnet.debugger.nodes.DotNetFieldOrPropertyMirrorNode;
import org.mustbe.consulo.dotnet.debugger.nodes.DotNetStructValueInfo;
import org.mustbe.consulo.dotnet.debugger.proxy.DotNetStackFrameMirrorProxy;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XNamedValue;
import mono.debugger.FieldOrPropertyMirror;
import mono.debugger.InvalidStackFrameException;
import mono.debugger.ObjectValueMirror;
import mono.debugger.StructValueMirror;
import mono.debugger.TypeMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 10.04.14
 */
@Logger
public class CSharpDebuggerProvider extends DotNetDebuggerProvider
{
	@NotNull
	@Override
	public PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull PsiElement sourcePosition, @NotNull String text, boolean isPhysical)
	{
		return CSharpFragmentFactory.createExpressionFragment(project, text, sourcePosition);
	}

	@Override
	@RequiredReadAction
	public void evaluate(@NotNull DotNetStackFrameMirrorProxy frame,
			@NotNull DotNetDebugContext debuggerContext,
			@NotNull String expression,
			@Nullable PsiElement elementAt,
			@NotNull XDebuggerEvaluator.XEvaluationCallback callback,
			@Nullable XSourcePosition sourcePosition)
	{
		if(elementAt == null)
		{
			XDebugSession session = debuggerContext.getSession();
			XSourcePosition currentPosition = session.getCurrentPosition();
			if(currentPosition == null)
			{
				callback.errorOccurred("cant evaluate");
				return;
			}

			VirtualFile file = currentPosition.getFile();
			PsiFile psiFile = PsiManager.getInstance(debuggerContext.getProject()).findFile(file);
			if(psiFile == null)
			{
				callback.errorOccurred("cant evaluate");
				return;
			}
			elementAt = psiFile.findElementAt(currentPosition.getOffset());
			if(elementAt == null)
			{
				callback.errorOccurred("cant evaluate");
				return;
			}
		}

		CSharpFragmentFileImpl expressionFragment = CSharpFragmentFactory.createExpressionFragment(debuggerContext.getProject(), expression, elementAt);

		PsiElement[] children = expressionFragment.getChildren();
		if(children.length == 0)
		{
			callback.errorOccurred("no expression");
			return;
		}

		PsiElement fragmentElement = children[0];

		DotNetExpression expressionPsi = fragmentElement instanceof DotNetExpression ? (DotNetExpression) fragmentElement : null;

		if(expressionPsi == null)
		{
			callback.errorOccurred("no expression");
			return;
		}

		CSharpExpressionEvaluator expressionEvaluator = new CSharpExpressionEvaluator();
		try
		{
			expressionPsi.accept(expressionEvaluator);

			CSharpEvaluateContext evaluateContext = new CSharpEvaluateContext(debuggerContext, frame, elementAt);

			List<Evaluator> evaluators = expressionEvaluator.getEvaluators();
			if(evaluators.isEmpty())
			{
				callback.errorOccurred("cant evaluate expression");
				return;
			}

			evaluateContext.evaluate(evaluators);
			Value<?> targetValue = evaluateContext.popValue();
			if(targetValue != null)
			{
				callback.evaluated(new CSharpWatcherNode(debuggerContext, expression, frame.thread(), targetValue));
			}
			else
			{
				callback.errorOccurred("no value");
			}
		}
		catch(InvalidStackFrameException e)
		{
			callback.errorOccurred("invalid stack frame");
		}
		catch(Exception e)
		{
			String message = e.getMessage();
			if(message == null)
			{
				message = e.getClass().getSimpleName() + " was throw";
				LOGGER.error("Exception have null message", e);
			}
			callback.errorOccurred(message);
		}
	}

	@Override
	@RequiredReadAction
	public void evaluate(@NotNull DotNetStackFrameMirrorProxy frame,
			@NotNull DotNetDebugContext debuggerContext,
			@NotNull DotNetReferenceExpression referenceExpression,
			@NotNull Set<Object> visitedVariables,
			@NotNull Consumer<XNamedValue> consumer)
	{
		PsiElement resolvedElement = referenceExpression.resolve();
		if(referenceExpression.getParent() instanceof CSharpMethodCallExpressionImpl || resolvedElement instanceof DotNetLikeMethodDeclaration)
		{
			return;
		}
		CSharpExpressionEvaluator expressionEvaluator = new CSharpExpressionEvaluator();
		try
		{
			referenceExpression.accept(expressionEvaluator);

			CSharpEvaluateContext evaluateContext = new CSharpEvaluateContext(debuggerContext, frame, referenceExpression);

			List<Evaluator> evaluators = expressionEvaluator.getEvaluators();
			if(evaluators.isEmpty())
			{
				return;
			}

			evaluateContext.evaluate(evaluators);
			Pair<Value<?>, Object> objectPair = evaluateContext.pop();
			if(objectPair != null && objectPair.getSecond() instanceof FieldOrPropertyMirror)
			{
				FieldOrPropertyMirror fieldOrPropertyMirror = (FieldOrPropertyMirror) objectPair.getSecond();
				if(visitedVariables.contains(fieldOrPropertyMirror))
				{
					return;
				}

				visitedVariables.add(fieldOrPropertyMirror);

				TypeMirror parent = fieldOrPropertyMirror.parent();

				Value thisObjectValue = frame.thisObject();
				TypeMirror type = thisObjectValue.type();
				if(thisObjectValue instanceof ObjectValueMirror && parent.equals(type))
				{
					consumer.consume(new DotNetFieldOrPropertyMirrorNode(debuggerContext, fieldOrPropertyMirror, frame.thread(), (ObjectValueMirror) thisObjectValue));
				}
				else if(thisObjectValue instanceof StructValueMirror && parent.equals(type))
				{
					StructValueMirror structValueMirror = (StructValueMirror) thisObjectValue;

					DotNetStructValueInfo valueInfo = new DotNetStructValueInfo(structValueMirror, null, fieldOrPropertyMirror, objectPair.getFirst());

					consumer.consume(new DotNetFieldOrPropertyMirrorNode(debuggerContext, fieldOrPropertyMirror, frame.thread(), null, valueInfo));
				}
				else
				{
					consumer.consume(new CSharpWatcherNode(debuggerContext, referenceExpression.getText(), frame.thread(), objectPair.getFirst()));
				}
			}
		}
		catch(Exception e)
		{
			// ignored
		}
	}

	@Override
	public boolean isSupported(@NotNull PsiFile psiFile)
	{
		return psiFile.getFileType() == CSharpFileType.INSTANCE;
	}

	@Override
	public Language getEditorLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
