package com.lkl.plugin.codegenerator.util;

import com.intellij.psi.PsiMember;
import org.jetbrains.java.generate.element.Element;

public interface MemberEntry<T extends PsiMember> extends Element {
    T getRaw();
}
