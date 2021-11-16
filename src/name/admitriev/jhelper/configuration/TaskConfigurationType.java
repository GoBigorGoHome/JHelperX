package name.admitriev.jhelper.configuration;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import name.admitriev.jhelper.icons.JhelperIcons;
import org.jetbrains.annotations.NotNull;


public class TaskConfigurationType extends SimpleConfigurationType {
	public TaskConfigurationType() {
		super(
				"name.admitriev.jhelper.configuration.TaskConfigurationType",
				"Task",
				"Task for JHelper",
				NotNullLazyValue.lazy(() -> JhelperIcons.TaskIcon)
		);
	}

	@NotNull
	@Override
	public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
		return new TaskConfiguration(project, this);
	}

}
