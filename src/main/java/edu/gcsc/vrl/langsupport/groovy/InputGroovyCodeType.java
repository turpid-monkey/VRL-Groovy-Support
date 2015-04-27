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

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import eu.mihosoft.vrl.annotation.TypeInfo;
import eu.mihosoft.vrl.lang.VLangUtils;
import eu.mihosoft.vrl.lang.visual.EditorProvider;
import eu.mihosoft.vrl.reflection.TypeRepresentationBase;
import eu.mihosoft.vrl.visual.ResizableContainer;
import eu.mihosoft.vrl.visual.VBoxLayout;
import eu.mihosoft.vrl.visual.VCodeEditor;
import eu.mihosoft.vrl.visual.VContainer;
import groovy.lang.Script;

/**
 * TypeRepresentation for
 * <code>java.lang.String</code>.
 *
 * <p>Sample:</p> <br/> <img src="doc-files/string-text-01.png"/> <br/>
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
@TypeInfo(type = String.class, input = true, output = false, style = "groovy-code")
public class InputGroovyCodeType extends TypeRepresentationBase {

    private VCodeEditor editor;
    private int minimumEditorWidth = 120;
    private int minimumEditorHeight = 24;
    private VContainer editorContainer = new VContainer();

    public InputGroovyCodeType() {

        VBoxLayout layout = new VBoxLayout(this, VBoxLayout.Y_AXIS);

        setLayout(layout);

        nameLabel.setText("Code:");
        nameLabel.setAlignmentX(0.5f);
        this.add(nameLabel);

        editor = createEditor();

        final ResizableContainer resizeCont = new ResizableContainer(editor);

        editorContainer.add(resizeCont);

        resizeCont.addExternalListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                editor.getScrollPane().setMaxWidth(resizeCont.getWidth());
                editor.getScrollPane().setMaxHeight(resizeCont.getHeight());
            }
        });

        editor.getScrollPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                InputGroovyCodeType.this.setValueOptions("width=" + getWidth() + ";"
                        + "height=" + getHeight());
            }
        });

//        editorContainer.add(editor);

        // add minimum editor width functionality
        editorContainer.setMinPreferredWidth(minimumEditorWidth);
        editorContainer.setMinPreferredHeight(minimumEditorHeight);

        add(editorContainer);

        setInputDocument(editor.getEditor().getDocument());
    }
    
    protected VCodeEditor createEditor() {
        return EditorProvider.getEditor("groovy", this);
    }
    
    @Override
    public void dispose() {
    	super.dispose();
    	// JTextComponentResourceLoader.getTextAreaManager().free(editor.getEditor());
    }

    @Override
    public void emptyView() {
        editor.getEditor().setText("");
    }

    @Override
    public void setViewValue(Object o) {
        emptyView();
        editor.getEditor().setText(o.toString());
        editor.getEditor().revalidate();
    }

    @Override
    public Object getViewValue() {
        Object o = null;
        try {
            o = editor.getEditor().getText();
        } catch (Exception ex) {
        }
        return o;
    }

    /**
     * @return the editor
     */
    public VCodeEditor getEditor() {
        return editor;
    }

//    /**
//     * @param editor the editor to set
//     */
//    public void setEditor(VCodeEditor editor) {
//        this.editor = editor;
//    }
    /**
     * @return the minimumEditorWidth
     */
    public Integer getMinimumEditorWidth() {
        return minimumEditorWidth;
    }

    /**
     * @param minimumEditorWidth the minimumEditorWidth to set
     */
    public void setMinimumEditorWidth(Integer minimumEditorWidth) {
        this.minimumEditorWidth = minimumEditorWidth;
        editorContainer.setMinPreferredWidth(minimumEditorWidth);
        editor.revalidate();
    }

    @Override
    public void evaluationRequest(Script script) {

        super.evaluationRequest(script);

        Object property = null;
        Integer w = null;
        Integer h = null;

        if (getValueOptions() != null) {

            if (getValueOptions().contains("width")) {
                property = script.getProperty("width");
            }

//            System.out.println("Property:" + property.getClass());

            if (property != null) {
                w = (Integer) property;
            }

            property = null;

            if (getValueOptions().contains("height")) {
                property = script.getProperty("height");
            }

            if (property != null) {
                h = (Integer) property;
            }

            property = null;

        }

        if (w != null && h != null) {
            // TODO find out why offset is 5
            editor.getScrollPane().setMaxWidth(w-5);
            editor.getScrollPane().setMaxHeight(h);
            
            
            // TODO find out why offset is 26
            editor.setPreferredSize(new Dimension(w-26, h-5));
            editor.setSize(new Dimension(w-26, h-5));
        }
    }

    @Override
    public String getValueAsCode() {
        return "\""
                + VLangUtils.addEscapesToCode(getValue().toString()) + "\"";
    }
}
