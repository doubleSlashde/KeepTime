package de.ds.keeptime.view.time;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Interval {

	public static List<CallBackListener> callBackListeners = new CopyOnWriteArrayList<>();

	private static Timeline timelineSession;

	public static void registerCallBack(CallBackListener cbl) {
		if (timelineSession == null)
			createTimeLine();
		callBackListeners.add(cbl);
	}

	/**
	 * only create timeLine if needed
	 */
	private static void createTimeLine() {
		timelineSession = new Timeline(new KeyFrame(Duration.seconds(1), (ae) -> {
			callBackListeners.forEach(CallBackListener::call);
		}));
		timelineSession.setCycleCount(Animation.INDEFINITE);
		timelineSession.play();

	}

	public static void removeCallBack(CallBackListener cbl) {
		if (timelineSession == null)
			return;

		callBackListeners.remove(cbl);

		if (callBackListeners.isEmpty()) {
			timelineSession.stop();
			timelineSession = null;
		}
	}

}
