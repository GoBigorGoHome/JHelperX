package name.admitriev.jhelper.components;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import org.jetbrains.annotations.NotNull;

@Service
public final class AutoSwitcherService {
    private final Project project;
    private boolean busy;

    public AutoSwitcherService(Project project) {
        this.project = project;
        busy = false;
    }

    public void selectTask(VirtualFile file) {
        Runnable selectTaskRunnable = () -> {
            if (busy || file == null) {
                return;
            }
            RunManagerImpl runManager = RunManagerImpl.getInstanceImpl(project);
            RunnerAndConfigurationSettings oldConfiguration = runManager.getSelectedConfiguration();
            if (oldConfiguration != null && !(oldConfiguration.getConfiguration() instanceof TaskConfiguration)) {
                return;
            }
            for (RunConfiguration configuration : runManager.getAllConfigurationsList()) {
                if (configuration instanceof TaskConfiguration) {
                    TaskConfiguration task = (TaskConfiguration) configuration;
                    String pathToClassFile = task.getCppPath();
                    VirtualFile expectedFie = IDEUtils.getBaseDir(project).findFileByRelativePath(pathToClassFile);
                    if (file.equals(expectedFie)) {
                        busy = true;
                        RunManager.getInstance(project).setSelectedConfiguration(
                                new RunnerAndConfigurationSettingsImpl(
                                        runManager,
                                        configuration,
                                        false
                                )
                        );
                        busy = false;
                        return;
                    }
                }
            }
        };

        DumbService.getInstance(project).smartInvokeLater(selectTaskRunnable);
    }

    public void openTaskFile(@NotNull VirtualFile taskFile) {
        if (busy) {
            return;
        }
        busy = true;
        ApplicationManager.getApplication().invokeAndWait(() -> FileEditorManager.getInstance(project).openFile(
                taskFile,
                true
        ));
        busy = false;
    }

    public boolean isBusy() {
        return busy;
    }
}
