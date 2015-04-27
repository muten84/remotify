package it.luigibifulco.xuggler;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ServerSocketImageReceiver {

	public final static int PORT = 4445;

	private ServerSocket serverSocket;

	private ImageListenerViewer viewer;

	public ServerSocketImageReceiver() {
		viewer = new ImageListenerViewer();
	}

	public void startUI() throws InterruptedException, InvocationTargetException {
		System.out.println("start UI");
		viewer.start();
	}

	public void start(int port) throws IOException, InterruptedException, InvocationTargetException {
		System.out.println("starting server with port: " + port);
		serverSocket = new ServerSocket(port);
		System.out.println("accepting connection");
		Socket socket = serverSocket.accept();
		System.out.println("connection accepted");
		InputStream stream = socket.getInputStream();
		ImageInputStream imageIs = new MemoryCacheImageInputStream(stream);
		System.out.println("reading...");
		BufferedImage image = ImageIO.read(imageIs);
		System.out.println("image read... ");
		addFrame(image);

	}

	public void addFrame(BufferedImage image) {
		System.out.println("Adding new frame");
		viewer.addFrame(image);
	}

	public static class Client {
		private Socket socket = null;
		private OutputStream os;

		public Client() {

		}

		public void start(int port) throws UnknownHostException, IOException {
			socket = new Socket("localhost", port);
			os = socket.getOutputStream();
		}

		public void sendImage(BufferedImage image) throws IOException {
			System.out.println("Sending image");
			ImageOutputStream imageOs = new MemoryCacheImageOutputStream(os);
			boolean written = ImageIO.write(image, "jps", imageOs);
		}

	}

	public static void main(String[] args) throws AWTException, UnknownHostException, IOException, InterruptedException, InvocationTargetException {
		final ServerSocketImageReceiver server = new ServerSocketImageReceiver();
		server.startUI();
		new Thread(new Runnable() {

			public void run() {
				try {
					server.start(PORT);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

		Client client = new Client();
		client.start(PORT);

		// ImageListenerViewer viewer = new ImageListenerViewer();
		// viewer.start();
		while (true) {
			Robot r = new Robot();
			BufferedImage s = r.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			double scale = 0.75;
			int w = (int) (s.getWidth() * scale);
			int h = (int) (s.getHeight() * scale);
			BufferedImage outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			AffineTransform trans = new AffineTransform();
			trans.scale(scale, scale);
			Graphics2D g = outImage.createGraphics();
			g.drawImage(s, trans, null);
			g.dispose();
			// Thread.sleep(1);
			client.sendImage(outImage);
			// System.out.println("Adding new frame...");
			// viewer.addFrame(outImage);
		}
	}

}
