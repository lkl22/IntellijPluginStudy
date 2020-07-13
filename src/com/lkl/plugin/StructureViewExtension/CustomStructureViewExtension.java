package com.lkl.plugin.StructureViewExtension;

import com.intellij.ide.structureView.StructureViewExtension;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.java.JavaClassTreeElement;
import com.intellij.ide.structureView.impl.java.PsiFieldTreeElement;
import com.intellij.ide.structureView.impl.java.PsiMethodTreeElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class CustomStructureViewExtension implements StructureViewExtension {
    @Override
    public Class<? extends PsiElement> getType() {
        return null;
    }

    @Nullable
    @Override
    public Object getCurrentEditorElement(Editor editor, PsiElement psiElement) {
        return null;
    }

    @Override
    public StructureViewTreeElement[] getChildren(PsiElement parent) {
        Collection<StructureViewTreeElement> result = new ArrayList<StructureViewTreeElement>();
        final PsiClass psiClass = (PsiClass) parent;

//        for (PsiField psiField : psiClass.getFields()) {
//            if (psiField instanceof LombokLightFieldBuilder) {
//                result.add(new PsiFieldTreeElement(psiField, false));
//            }
//        }
//
//        for (PsiMethod psiMethod : psiClass.getMethods()) {
//            if (psiMethod instanceof LombokLightMethodBuilder) {
//                result.add(new PsiMethodTreeElement(psiMethod, false));
//            }
//        }
//
//        for (PsiClass psiInnerClass : psiClass.getInnerClasses()) {
//            if (psiInnerClass instanceof LombokLightClassBuilder) {
//                result.add(new JavaClassTreeElement(psiInnerClass, false, new HashSet<PsiClass>() {{
//                    add(psiClass);
//                }}));
//            }
//        }

        if (!result.isEmpty()) {
            return result.toArray(new StructureViewTreeElement[result.size()]);
        } else {
            return StructureViewTreeElement.EMPTY_ARRAY;
        }
    }
}
