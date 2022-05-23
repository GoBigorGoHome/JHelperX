package name.admitriev.jhelper.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.vfs.VirtualFile;
import name.admitriev.jhelper.IDEUtils;
import name.admitriev.jhelper.components.Configurator;
import org.jdesktop.swingx.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConfigurationDialog extends DialogWrapper {
	private final JComponent component;
	private final JTextField author;
	private final FileSelector tasksDirectory;
	private final FileSelector outputFile;
	private final FileSelector runFile;
	private final JCheckBox codeEliminationOn;
	private final JCheckBox codeReformattingOn;

	public ConfigurationDialog(@NotNull Project project, Configurator.State configuration) {
		super(project);
		setTitle("JHelper configuration for " + project.getName());

		author = new JTextField(configuration.getAuthor());
		VirtualFile baseDir = IDEUtils.getBaseDir(project);
		tasksDirectory = new FileSelector(
				project,
				configuration.getTasksDirectory(),
				RelativeFileChooserDescriptor.directoryChooser(baseDir)
		);
		outputFile = new FileSelector(
				project,
				configuration.getOutputFile(),
				RelativeFileChooserDescriptor.fileChooser(baseDir)
		);
		runFile = new FileSelector(
				project,
				configuration.getRunFile(),
				RelativeFileChooserDescriptor.fileChooser(baseDir)
		);

		codeEliminationOn = new JCheckBox("Eliminate code?", configuration.isCodeEliminationOn());
		codeReformattingOn = new JCheckBox("Reformat code?", configuration.isCodeReformattingOn());

		JPanel panel = new JPanel(new VerticalLayout());
		panel.add(LabeledComponent.create(author, "Author"));
		panel.add(LabeledComponent.create(tasksDirectory, "Tasks directory"));
		panel.add(LabeledComponent.create(outputFile, "Output file"));
		panel.add(LabeledComponent.create(runFile, "Run file"));
		panel.add(codeEliminationOn);
		panel.add(codeReformattingOn);

		component = panel;

		init();
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		return component;
	}

	public Configurator.State getConfiguration() {
		return new Configurator.State(
				author.getText(),
				tasksDirectory.getText(),
				outputFile.getText(),
				runFile.getText(),
				codeEliminationOn.isSelected(),
				codeReformattingOn.isSelected()
		);
	}
}
