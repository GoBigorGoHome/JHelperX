package name.admitriev.jhelper.components;

import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import org.jetbrains.annotations.NotNull;

public class AutoSwitcher implements FileEditorManagerListener, RunManagerListener {
    private final Project project;

    public AutoSwitcher(Project project) {
        this.project = project;
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        AutoSwitcherService autoSwitcherService = project.getService(AutoSwitcherService.class);
        autoSwitcherService.selectTask(event.getNewFile());
    }

    @Override
    public void runConfigurationSelected(RunnerAndConfigurationSettings selectedConfiguration) {
        if (selectedConfiguration == null) {
            return;
        }
        RunConfiguration configuration = selectedConfiguration.getConfiguration();
        if (!(configuration instanceof TaskConfiguration)) {
            return;
        }
        AutoSwitcherService autoSwitcherService = project.getService(AutoSwitcherService.class);
        if (autoSwitcherService.isBusy()) {
            return;
        }
        String pathToClassFile = ((TaskConfiguration) configuration).getCppPath();
        VirtualFile toOpen = IDEUtils.getBaseDir(project).findFileByRelativePath(pathToClassFile);
        if (toOpen != null) {
            autoSwitcherService.openTaskFile(toOpen);
        }
    }
}
