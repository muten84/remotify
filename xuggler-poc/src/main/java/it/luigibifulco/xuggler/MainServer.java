package it.luigibifulco.xuggler;

import java.lang.reflect.InvocationTargetException;

public class MainServer {

	public static void main(String[] args) throws InvocationTargetException,
			InterruptedException {
		final ServerSocketImageReceiver server = new ServerSocketImageReceiver();
		server.startUI();
		new Thread(new Runnable() {

			public void run() {
				try {
					server.start(4444);
				} catch (Exception e) {

				}
			}
		}).start();
		
	}
}
