/*
 * Copyright (c) 2014, Goethe University, Goethe Center for Scientific Computing (GCSC), gcsc.uni-frankfurt.de
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.gcsc.vrl.langsupport.groovy;

import eu.mihosoft.vrl.instrumentation.VRLVisualizationTransformation;
import eu.mihosoft.vrl.lang.model.CodeRange;
import eu.mihosoft.vrl.lang.model.Scope;
import eu.mihosoft.vrl.lang.model.UIBinding;
import eu.mihosoft.vrl.lang.model.Variable;
import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.text.JTextComponent;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.VariableCompletion;

public class GroovyCompletionProvider extends DefaultCompletionProvider {

	@SuppressWarnings("rawtypes")
	@Override
	protected List getCompletionsImpl(JTextComponent textComponent) {
		List<Completion> completions = new ArrayList<Completion>();
		String groovyScript = textComponent.getText();

		UIBinding.scopes.clear();
		
		CompilerConfiguration conf = new CompilerConfiguration();
		conf.addCompilationCustomizers(new ASTTransformationCustomizer(
				new VRLVisualizationTransformation()));
		GroovyClassLoader loader = new GroovyClassLoader(this.getClass().getClassLoader(), conf);
		loader.parseClass(groovyScript);
		try {
			loader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		CodeRange cr = new CodeRange(textComponent.getCaretPosition(),
				textComponent.getCaretPosition());
		// checking whether code from new model is identical to new code
		for (Collection<Scope> scopeList : UIBinding.scopes.values()) {
			recurse(scopeList, cr, completions);
		}

		this.addCompletions(completions);
		return super.getCompletionsImpl(textComponent);
	}

	void recurse(Collection<Scope> parent, CodeRange cursor,
			List<Completion> completions) {
		for (Scope s : parent) {
			// System.out.println("scope with range " + s.getRange());
			if (s.getRange().contains(cursor)) {
				for (Variable v : s.getVariables()) {
					VariableCompletion var = new VariableCompletion(this, v.getName(),
							v.getType().getShortName());
                    completions.add(var);
				}
			}
			recurse(s.getScopes(), cursor, completions);
		}
	}

}
