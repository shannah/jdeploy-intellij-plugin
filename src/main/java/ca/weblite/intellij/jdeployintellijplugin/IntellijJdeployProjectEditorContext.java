package ca.weblite.intellij.jdeployintellijplugin;

import ca.weblite.jdeploy.gui.JDeployProjectEditorContext;
import ca.weblite.jdeploy.interop.DesktopInterop;
import ca.weblite.jdeploy.interop.FileChooserInterop;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;

public class IntellijJdeployProjectEditorContext extends JDeployProjectEditorContext {

    private final Project project;

    private final IntellijDesktopInterop desktopInterop = new IntellijDesktopInterop();

    private final IntelliJFileChooserInterop fileChooserInterop = new IntelliJFileChooserInterop();

    public IntellijJdeployProjectEditorContext(Project project) {
        this.project = project;
    }
    @Override
    public boolean shouldDisplayExitMenu() {
        return false;
    }

    @Override
    public boolean shouldShowPublishButton() {
        return false;
    }

    @Override
    public boolean shouldDisplayApplyButton() {
        return true;
    }

    @Override
    public boolean shouldDisplayCheerpJPanel() {
        return true;
    }

    @Override
    public boolean shouldDisplayCancelButton() {
        return true;
    }

    @Override
    public boolean shouldDisplayMenuBar() {
        return false;
    }

    @Override
    public DesktopInterop getDesktopInterop() {
        return desktopInterop;
    }

    @Override
    public IntelliJFileChooserInterop getFileChooserInterop() {
        return fileChooserInterop;
    }

    @Override
    public Component getParentFrame() {
        return WindowManager.getInstance().getIdeFrame(project).getComponent();
    }

    @Override
    public void onFileUpdated(File file) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file.getAbsolutePath());
        if (virtualFile != null) {
            ApplicationManager.getApplication().runWriteAction(() -> {
                virtualFile.refresh(false, false);
            });
        }
    }

    private class IntellijDesktopInterop extends DesktopInterop {
        @Override
        public void edit(File file) throws Exception {
            ApplicationManager.getApplication().invokeLater(() -> {
                VirtualFile fileToOpen = LocalFileSystem.getInstance().findFileByPath(file.getAbsolutePath());
                if (fileToOpen != null) {
                    // Open the file in the editor
                    FileEditorManager.getInstance(project).openFile(fileToOpen, true);
                }
            });

        }

        @Override
        public void browse(URI url) throws Exception {
            BrowserUtil.browse(url);
        }
    }

    private class IntelliJFileChooserInterop extends FileChooserInterop {
        @Override
        public File showFileChooser(Frame parent, String title, java.util.Set<String> extensions) {
            FileChooserDescriptor descriptor = new FileChooserDescriptor(
                    true,  // chooseFiles
                    false, // chooseFolders
                    false, // chooseJars
                    false, // chooseJarsAsFiles
                    false, // chooseJarContents
                    false  // chooseMultiple
            );

            descriptor.setTitle("Select File");
            descriptor.setDescription("Choose a file to open");
            descriptor.setHideIgnored(false);

            // Optionally, set a file filter
            descriptor.withFileFilter(virtualFile -> extensions.contains(virtualFile.getExtension()));

            VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
            if (file != null) {
                // Handle file selection
                return new File(file.getPath());
            }

            return null;
        }
    }
}
