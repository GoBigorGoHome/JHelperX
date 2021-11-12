package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.task.TaskData;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Test;
import net.egork.chelper.task.TestType;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;

/**
 * Panel for task configuration.
 */
public final class TaskSettingsComponent extends JPanel {
	private JTextField name = null;
	private JTextField url = null;
	private JTextField className = null;
	private FileSelector cppPath = null;
	private StreamConfigurationPanel input = null;
	private StreamConfigurationPanel output = null;
	private ComboBox<TestType> testType = null;
	private final boolean canChangeName;

	private final Project project;

	private final StreamConfigurationPanel.SizeChangedListener listener;

	public TaskSettingsComponent(Project project, boolean canChangeName) {
		this(project, canChangeName, null);
	}

	public TaskSettingsComponent(Project project, boolean canChangeName, StreamConfigurationPanel.SizeChangedListener listener) {
		super(new VerticalLayout());
		this.project = project;
		this.listener = listener;
		this.canChangeName = canChangeName;

		setTaskData(TaskData.emptyTaskData(project));
	}

	public void setTaskData(TaskData taskData) {
		removeAll();
		name = new JTextField(taskData.getName());
		name.setEnabled(canChangeName);
		url = new JTextField(taskData.getUrl());
		url.setEnabled(false);
		className = new JTextField(taskData.getClassName());
		cppPath = new FileSelector(
				project,
				taskData.getCppPath(),
				RelativeFileChooserDescriptor.fileChooser(IDEUtils.getBaseDir(project))
		);
		input = new StreamConfigurationPanel(
				taskData.getInput(),
				StreamConfiguration.StreamType.values(),
				"input.txt",
				listener
		);
		output = new StreamConfigurationPanel(
				taskData.getOutput(),
				StreamConfiguration.OUTPUT_TYPES,
				"output.txt",
				listener
		);

		testType = new ComboBox<>(TestType.values());
		testType.setSelectedItem(taskData.getTestType());

		add(LabeledComponent.create(name, "Task name"));
		add(LabeledComponent.create(url, "Task URL"));
		add(LabeledComponent.create(className, "Class name"));
		add(LabeledComponent.create(cppPath, "Path"));
		add(LabeledComponent.create(input, "Input"));
		add(LabeledComponent.create(output, "Output"));
		add(LabeledComponent.create(testType, "Test type"));

		UIUtils.mirrorFields(name, className);
		UIUtils.mirrorFields(name, cppPath.getTextField(), TaskData.defaultCppPathFormat(project));
	}

	public TaskData getTask() {
		return new TaskData(
				name.getText(),
				url.getText(),
				className.getText(),
				cppPath.getText(),
				input.getStreamConfiguration(),
				output.getStreamConfiguration(),
				(TestType) testType.getSelectedItem(),
				new Test[0]
		);
	}


}
