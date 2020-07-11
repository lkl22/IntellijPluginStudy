// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lkl.plugin.tostring.template.toString;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.lkl.plugin.tostring.exception.TemplateResourceException;
import com.lkl.plugin.tostring.template.TemplateResource;
import com.lkl.plugin.tostring.template.TemplatesManager;
import org.jetbrains.annotations.NotNull;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@State(name = "ToStringTemplates", storages = @Storage("toStringTemplates.xml"))
public final class ToStringTemplatesManager extends TemplatesManager {
    private static final String DEFAULT_CONCAT = "DefaultConcatMember.vm";
    private static final String DEFAULT_CONCAT_GROOVY = "/com/lkl/plugin/tostring/template/toString/DefaultConcatMemberGroovy.vm";
    private static final String DEFAULT_CONCAT_SUPER = "/com/lkl/plugin/tostring/template/toString/DefaultConcatMemberSuper.vm";
    private static final String DEFAULT_CONCAT_SUPER_GROOVY = "/com/lkl/plugin/tostring/template/toString/DefaultConcatMemberSuperGroovy.vm";
    private static final String DEFAULT_BUFFER = "/com/lkl/plugin/tostring/template/toString/DefaultBuffer.vm";
    private static final String DEFAULT_BUILDER = "/com/lkl/plugin/tostring/template/toString/DefaultBuilder.vm";
    private static final String DEFAULT_TOSTRINGBUILDER = "/com/lkl/plugin/tostring/template/toString/DefaultToStringBuilder.vm";
    private static final String DEFAULT_TOSTRINGBUILDER3 = "/com/lkl/plugin/tostring/template/toString/DefaultToStringBuilder3.vm";
    private static final String DEFAULT_GUAVA = "/com/lkl/plugin/tostring/template/toString/DefaultGuava.vm";
    private static final String DEFAULT_GUAVA_18 = "/com/lkl/plugin/tostring/template/toString/DefaultGuava18.vm";
    private static final String DEFAULT_STRING_JOINER = "/com/lkl/plugin/tostring/template/toString/StringJoiner.vm";

    public static TemplatesManager getInstance() {
        return ServiceManager.getService(ToStringTemplatesManager.class);
    }

    @Override
    public @NotNull List<TemplateResource> getDefaultTemplates() {
        try {
            return Arrays.asList(new TemplateResource("String concat (+)", readFile(DEFAULT_CONCAT), true),
                    new TemplateResource("String concat (+) and super.toString()", readFile(DEFAULT_CONCAT_SUPER), true),
                    new TemplateResource("StringBuffer", readFile(DEFAULT_BUFFER), true),
                    new TemplateResource("StringBuilder (JDK 1.5)", readFile(DEFAULT_BUILDER), true),
                    new TemplateResource("ToStringBuilder (Apache commons-lang)", readFile(DEFAULT_TOSTRINGBUILDER), true, "org.apache.commons.lang.builder.ToStringBuilder"),
                    new TemplateResource("ToStringBuilder (Apache commons-lang 3)", readFile(DEFAULT_TOSTRINGBUILDER3), true, "org.apache.commons.lang3.builder.ToStringBuilder"),
                    new TemplateResource("Objects.toStringHelper (Guava)", readFile(DEFAULT_GUAVA), true, "com.google.common.base.Objects"),
                    new TemplateResource("MoreObjects.toStringHelper (Guava 18+)", readFile(DEFAULT_GUAVA_18), true, "com.google.common.base.MoreObjects"),
                    new TemplateResource("StringJoiner (JDK 1.8)", readFile(DEFAULT_STRING_JOINER), true),
                    new TemplateResource("Groovy: String concat (+)", readFile(DEFAULT_CONCAT_GROOVY), true),
                    new TemplateResource("Groovy: String concat (+) and super.toString()", readFile(DEFAULT_CONCAT_SUPER_GROOVY), true));
        }
        catch (IOException e) {
            throw new TemplateResourceException("Error loading default templates", e);
        }
    }

    protected static String readFile(String resource) throws IOException {
        return readFile(resource, ToStringTemplatesManager.class);
    }
}