package name.admitriev.jhelper;

import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.ExecutionTargetManager;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import name.admitriev.jhelper.exceptions.NotificationException;
import org.jetbrains.annotations.NotNull;

public class IDEUtils {
	private IDEUtils() {
	}

	public static void reloadProject(Project project) {
		CMakeWorkspace.getInstance(project).scheduleReload(true);
	}

	public static void chooseConfigurationAndTarget(
			Project project,
			RunnerAndConfigurationSettings runConfiguration,
			ExecutionTarget target
	) {
		RunManager.getInstance(project).setSelectedConfiguration(runConfiguration);
		ExecutionTargetManager.getInstance(project).setActiveTarget(target);
	}

    @NotNull
    public static VirtualFile getBaseDir(@NotNull Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            throw new NotificationException("Could not get project base path", "Are you in any project?");
        }
        VirtualFile ret = LocalFileSystem.getInstance().findFileByPath(basePath);
        if (ret == null) {
            throw new NotificationException("Could not get project base path", "Are you in any project?");
        }
        return ret;
    }
}
