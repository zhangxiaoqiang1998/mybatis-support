package org.muchu.mybatis.support.service.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.muchu.mybatis.support.dom.model.Mapper;
import org.muchu.mybatis.support.dom.model.Statement;
import org.muchu.mybatis.support.service.MyDomService;

import java.util.Collection;
import java.util.List;

public class MyDomServiceImpl implements MyDomService {

    @Override
    public Mapper getMapper(@NotNull PsiClass psiClass, @Nullable GlobalSearchScope scope) {
        if (!psiClass.isInterface()) {
            return null;
        }
        Project project = psiClass.getProject();
        Collection<VirtualFile> domFileCandidates = DomService.getInstance().getDomFileCandidates(Mapper.class,
                psiClass.getProject(), scope != null ? scope : GlobalSearchScope.allScope(project));
        for (VirtualFile file : domFileCandidates) {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile instanceof XmlFile) {
                final DomFileElement<Mapper> element = DomManager.getDomManager(project)
                        .getFileElement((XmlFile) psiFile, Mapper.class);
                if (element != null && StringUtils.equals(psiClass.getQualifiedName(), element.getRootElement().getNamespace().getValue())) {
                    return element.getRootElement();
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Statement getStatement(@NotNull PsiMethod psiMethod, @Nullable GlobalSearchScope scope) {
        PsiClass psiClass = psiMethod.getContainingClass();
        if (psiClass == null) {
            return null;
        }
        Mapper mapper = getMapper(psiClass, scope);
        if (mapper != null) {
            List<Statement> statements = mapper.getStatements();
            for (Statement statement : statements) {
                if (StringUtils.equals(psiMethod.getName(), statement.getId().getValue())) {
                    //TODO check return type
                    return statement;
                }
            }
        }
        return null;
    }
}
