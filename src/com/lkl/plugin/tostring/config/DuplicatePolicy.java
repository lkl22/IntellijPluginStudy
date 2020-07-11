/*
 * Copyright 2001-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lkl.plugin.tostring.config;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.config.ConflictResolutionPolicy;
import org.jetbrains.java.generate.config.InsertNewMethodStrategy;

/**
 * This policy is to create a duplicate {@code toString} method.
 */
public final class DuplicatePolicy implements ConflictResolutionPolicy {

    private static final DuplicatePolicy instance = new DuplicatePolicy();
    private InsertNewMethodStrategy newMethodStrategy = InsertAtCaretStrategy.getInstance();

    private DuplicatePolicy() {}

    public static DuplicatePolicy getInstance() {
        return instance;
    }

    @Override
    public void setNewMethodStrategy(InsertNewMethodStrategy strategy) {
        newMethodStrategy = strategy;
    }

    @Override
    public PsiMethod applyMethod(PsiClass clazz, PsiMethod existingMethod, @NotNull PsiMethod newMethod, Editor editor) {
        return newMethodStrategy.insertNewMethod(clazz, newMethod, editor);
    }

    public String toString() {
        return "Duplicate";
    }
}