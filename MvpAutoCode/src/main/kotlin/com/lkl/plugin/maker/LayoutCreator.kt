package com.lkl.plugin.maker

import com.android.resources.ResourceFolderType
import com.android.tools.idea.util.dependsOnAndroidx
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.lkl.plugin.config.ItemConfigBean
import org.jetbrains.android.dom.manifest.Manifest
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.facet.AndroidRootUtil
import org.jetbrains.android.util.AndroidResourceUtil
import org.jetbrains.android.util.AndroidUtils

/**
 * @describe
 * @author  longforus
 * @date 2020/3/18  16:07
 */
@Throws(Exception::class)
fun doCreateLayoutFile(ic: ItemConfigBean, element: PsiClass?, project: Project, facet: AndroidFacet, isJava: Boolean, isActivity: Boolean = true): PsiElement? {
    return if (element == null) {
        null
    } else {
        val manifestFile = AndroidRootUtil.getManifestFileForCompiler(facet) ?: return null
        val manifest = AndroidUtils.loadDomElement(facet.module, manifestFile, Manifest::class.java)
//        val manifest = Manifest.getMainManifest(facet)
        val appPackage = manifest?.getPackage()?.value
        if (appPackage != null && appPackage.isNotEmpty()) {
            ApplicationManager.getApplication().invokeLater {
                LocalFileSystem.getInstance().findFileByPath(AndroidRootUtil.getResourceDirPath(facet) ?: "")?.let {
                    PsiManager.getInstance(project).findDirectory(it)?.let {
                        createLayoutFileForActivityOrFragment(ic, facet, element, appPackage, it, isJava, isActivity)

                    }
                }

            }
        }
        element
    }
}

fun createLayoutFileForActivityOrFragment(ic: ItemConfigBean, facet: AndroidFacet, activityClass: PsiClass, appPackage: String, resDirectory: PsiDirectory, isJava: Boolean, isActivity: Boolean) {
    if (!facet.isDisposed && activityClass.isValid) {
        val className = activityClass.name
        if (className != null) {
//            val layoutFile = CreateResourceFileAction.createFileResource(facet, ResourceFolderType.LAYOUT, "activity_auto", null, null, true, "Create Layout For '$className'",
//                resDirectory, null, false)
            val nameSb = StringBuilder()
            ic.name.forEach {
                if (it >= "A"[0] && it <= "Z"[0]) {
                    nameSb.append("_")
                }
                nameSb.append(it)
            }

            val layoutFileOriginName = if (isActivity) "activity${nameSb.toString().toLowerCase()}" else "frag${nameSb.toString().toLowerCase()}"

            val rootLayoutName = if (facet.module.dependsOnAndroidx()) "androidx.constraintlayout.widget.ConstraintLayout" else "android.support.constraint.ConstraintLayout"

            val layoutFile = AndroidResourceUtil.createFileResource(
                    layoutFileOriginName, resDirectory.findSubdirectory("layout")!!,
                    rootLayoutName,
                    ResourceFolderType.LAYOUT.getName(), false)

//            val layoutFileName = layoutFile?.name
//            val onCreateMethods = activityClass.findMethodsByName("getLayoutId", false)//todo 生成viewBinding
//            if (onCreateMethods.size != 1) {
//                return
//            }
//            if (activityClass is KtUltraLightClass){
//
//                val psiMethod = activityClass.kotlinOrigin.findFunctionByName("getLayoutId") as KtNamedFunction
//
//                val fieldName = AndroidResourceUtil.getRJavaFieldName(FileUtil.getNameWithoutExtension(layoutFileName))
//                val layoutFieldRef = "$appPackage.R.layout.$fieldName"
////                getKtStatement(psiMethod, layoutFieldRef, false)
//
//            }
//            val onCreateMethod = onCreateMethods[0]
//            val fieldName = AndroidResourceUtil.getRJavaFieldName(FileUtil.getNameWithoutExtension(layoutFileName))
//            val layoutFieldRef = "$appPackage.R.layout.$fieldName"
//            getKtStatement(onCreateMethod, layoutFieldRef, isJava)
        }
    }
}


fun addInflateStatement(body: PsiCodeBlock, layoutFieldRef: String, isJava: Boolean) {
    val project = body.project
    val statements = body.statements
    if (statements.size == 1) {
        val statement = statements[0]
        WriteCommandAction.writeCommandAction(project, body.containingFile).run<Throwable> {
            val newStatement = PsiElementFactory.getInstance(project).createStatementFromText(
                    "return $layoutFieldRef${if (isJava) ";" else ""}", body)
            statement.replace(newStatement)
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(body)
            CodeStyleManager.getInstance(project).reformat(body)
        }
    }
}

fun getKtStatement(method: PsiMethod, layoutFieldRef: String, isJava: Boolean) {
    val project = method.project
    WriteCommandAction.writeCommandAction(project, method.containingFile).run<Throwable> {
        val newStatement = PsiElementFactory.getInstance(project).createStatementFromText(
                "return $layoutFieldRef${if (isJava) ";" else ""}", method)
        method.add(newStatement)
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(method)
        CodeStyleManager.getInstance(project).reformat(method)
    }
}

