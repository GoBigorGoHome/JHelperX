package name.admitriev.jhelper.components;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.psi.PsiElement;
import com.intellij.util.text.StringTokenizer;
import com.jetbrains.cidr.lang.psi.OCFile;
import name.admitriev.jhelper.network.SimpleHttpServer;
import name.admitriev.jhelper.task.TaskData;
import name.admitriev.jhelper.task.TaskUtils;
import name.admitriev.jhelper.ui.Notificator;
import name.admitriev.jhelper.ui.UIUtils;
import net.egork.chelper.task.StreamConfiguration;
import net.egork.chelper.task.Test;
import net.egork.chelper.task.TestType;
import org.jetbrains.annotations.NotNull;

//import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.PrintStream;
import java.net.InetSocketAddress;
import com.google.gson.Gson;

/**
 * A Component to monitor request from CHelper Chrome Extension and parse them to Tasks
 */
public class ChromeParser implements ProjectManagerListener {
	private static final int PORT = 4243;

	private SimpleHttpServer server = null;
	private final Project project;

	public ChromeParser(Project project) {
		this.project = project;
	}

	@Override
	public void projectOpened(@NotNull Project project) {
		try {
//			try {
//				PrintStream fileOut = new PrintStream("C:/Users/zjsdu/Desktop/out.txt");
//				System.setOut(fileOut);
//
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
			server = new SimpleHttpServer(
					new InetSocketAddress("localhost", PORT),
					request -> {
						StringTokenizer st = new StringTokenizer(request);
						String type = st.nextToken();
//						System.out.println(request);
						if (!type.equals("json")) {
							Notificator.showNotification(
									"Response type is not json",
									type + ": unknown type, ignored",
									NotificationType.INFORMATION
							);
							return;
						}
						String json = request.substring(st.getCurrentPosition());
						Gson gson = new Gson();
						CcTaskData rawTask = gson.fromJson(json, CcTaskData.class);
						if (rawTask == null) {
							Notificator.showNotification(
									"Couldn't parse any task",
									"Maybe format changed?",
									NotificationType.WARNING
							);
						}

						Configurator configurator = project.getService(Configurator.class);
						Configurator.State configuration = configurator.getState();
						String path = configuration.getTasksDirectory();
                        assert rawTask != null;
                        RawTest[] rawTests = rawTask.tests;
                        final int test_num = rawTests.length;
                        Test[] tests = new Test[test_num];
                        for (int i = 0; i < test_num; i++) {
                            tests[i] = new Test(rawTests[i].input, rawTests[i].output, i);
                        }
                        TaskData task = new TaskData(
                                rawTask.name,
                                rawTask.url,
                                rawTask.taskClass(),
                                String.format("%s/%s.cpp", path, rawTask.taskClass()),
                                StreamConfiguration.STANDARD,
                                StreamConfiguration.STANDARD,
                                TestType.SINGLE,
                                tests
                        );
                        PsiElement generatedFile = TaskUtils.saveNewTask(task, project);
                        UIUtils.openMethodInEditor(project, (OCFile) generatedFile, "solve");
					}
			);

			new Thread(server, "ChromeParserThread").start();
		}
		catch (IOException ignored) {
			Notificator.showNotification(
					"Could not create serverSocket for Chrome parser",
					"Probably another CHelper or JHelper project is running?",
					NotificationType.ERROR
			);
		}
	}

	@Override
	public void projectClosing(@NotNull Project project) {
		if (server != null) {
			server.stop();
		}
	}
}