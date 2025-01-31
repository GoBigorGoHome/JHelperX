package name.admitriev.jhelper.ui;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;

public class Notificator {

    private static final NotificationGroup GROUP = NotificationGroupManager.getInstance().getNotificationGroup("JHelper");

	private Notificator() {
	}

	public static void showNotification(String title, String content, NotificationType notificationType) {
		GROUP.createNotification(title, content, notificationType).notify(null);
	}

	public static void showNotification(String content, NotificationType notificationType) {
		showNotification("", content, notificationType);
	}
}
