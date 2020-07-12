package com.lkl.plugin.PsiAugmentProvider;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.light.LightFieldBuilder;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.impl.light.LightModifierList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomPsiAugmentProvider extends PsiAugmentProvider {
    @NotNull
    @Override
    protected <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {

//        PsiManager manager = psiField.getAugmentsManager();
//        LightMethodBuilder method =  new LightMethodBuilder(manager, JavaLanguage.INSTANCE, methodName);
//        method.addModifier(PsiModifier.PUBLIC);
//        method.setContainingClass(psiClass);
//        method.setNavigationElement(psiField);
//        method.addParameter(psiField.getName(), psiField.getType());
//        method.setMethodReturnType(PsiType.VOID);
//
//        PsiType psiLoggerType = psiElementFactory.createTypeFromText(LOGGER_TYPE, psiClass);
//        LightFieldBuilder loggerField = new LightFieldBuilder(manager, LOGGER_NAME, psiLoggerType);
//        LightModifierList modifierList = (LightModifierList) loggerField.getModifierList();
//        modifierList.addModifier(PsiModifier.PRIVATE);
//        modifierList.addModifier(PsiModifier.STATIC);
//        modifierList.addModifier(PsiModifier.FINAL);
//        loggerField.setContainingClass(psiClass);
//        loggerField.setNavigationElement(psiAnnotation);

        return super.getAugments(element, type);
    }
}
