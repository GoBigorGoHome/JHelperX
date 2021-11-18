package name.admitriev.jhelper.actions;

import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.components.Configurator;
import name.admitriev.jhelper.configuration.TaskConfiguration;
import name.admitriev.jhelper.exceptions.NotificationException;
import name.admitriev.jhelper.generation.CodeGenerationUtils;
import name.admitriev.jhelper.ui.Notificator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalToolWindowFactory;
import org.jetbrains.plugins.terminal.TerminalView;


public class SubmitCodeAction extends BaseAction {
    @Override
    protected void performAction(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null)
            throw new NotificationException("No project found", "Are you in any project?");

        Configurator configurator = project.getService(Configurator.class);
        Configurator.State configuration = configurator.getState();

        RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
        RunnerAndConfigurationSettings selectedConfiguration = runManager.getSelectedConfiguration();
        if (selectedConfiguration == null) {
            return;
        }

        RunConfiguration runConfiguration = selectedConfiguration.getConfiguration();
        if (!(runConfiguration instanceof TaskConfiguration)) {
            Notificator.showNotification(
                    "Not a JHelper configuration",
                    "You have to choose JHelper Task to copy",
                    NotificationType.WARNING
            );
            return;
        }

        CodeGenerationUtils.generateSubmissionFileForTask(project, (TaskConfiguration) runConfiguration);

        VirtualFile file = IDEUtils.getBaseDir(project).findFileByRelativePath(configuration.getOutputFile());
        if (file == null)
            throw new NotificationException("Couldn't find output file");

        TerminalView terminalView = TerminalView.getInstance(project);
        ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(TerminalToolWindowFactory.TOOL_WINDOW_ID);
        if (window == null) {
            return;
        }

        String url = ((TaskConfiguration) runConfiguration).getUrl();
        String command;
        if (url.startsWith("https://codeforces")) {
            command = "cf submit -f " + file.getPath() + " " + url;
        } else {
            command = "oj s --wait=0 --yes " + url + " " + file.getPath();
        }

        ContentManager contentManager = window.getContentManager();
        String workingDirectory = project.getBasePath();
        assert workingDirectory != null;
        Pair<Content, ShellTerminalWidget> pair = getSuitableProcess(contentManager);
        try {
            if (pair == null) {
                terminalView.createLocalShellWidget(workingDirectory, "Submit", true).executeCommand(command);
                return;
            }
            window.activate(null, false);
            pair.second.executeCommand(command);
        }
        catch (IOException err) {
            throw new NotificationException("Cannot run command: " + command);
        }
    }

    private static @Nullable Pair<Content, ShellTerminalWidget> getSuitableProcess(@NotNull ContentManager contentManager) {
        Content selectedContent = contentManager.findContent("Submit");
        if (selectedContent != null) {
            Pair<Content, ShellTerminalWidget> pair = getSuitableProcess(selectedContent);
            if (pair != null) return pair;
        }

        return Arrays.stream(contentManager.getContents())
                .map(SubmitCodeAction::getSuitableProcess)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static @Nullable Pair<Content, ShellTerminalWidget> getSuitableProcess(@NotNull Content content) {
        JBTerminalWidget widget = TerminalView.getWidgetByContent(content);
        if (!(widget instanceof ShellTerminalWidget)) {
            return null;
        }

        ShellTerminalWidget shellTerminalWidget = (ShellTerminalWidget)widget;
        if (!shellTerminalWidget.getTypedShellCommand().isEmpty() || shellTerminalWidget.hasRunningCommands()) {
            return null;
        }

        return new Pair<>(content, shellTerminalWidget);
    }
}
