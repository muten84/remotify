package it.luigibifulco.xuggler;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
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

	public void startUI() throws InterruptedException,
			InvocationTargetException {
		System.out.println("start UI");
		viewer.start();
	}

	public void start(int port) throws IOException, InterruptedException,
			InvocationTargetException {
		System.out.println("starting server with port: " + port);
		serverSocket = new ServerSocket(port);
		System.out.println("accepting connection");
		Socket socket = serverSocket.accept();
		System.out.println("connection accepted");
		InputStream stream = socket.getInputStream();
		ImageInputStream imageIs = null;
		while (true) {
			char header1 = (char) stream.read();
			char header2 = (char) stream.read();
			int size = 0;
			BigInteger bigI = null;
			if (header1 == '#' && header2 == '#') {
				size = (int) stream.read();
				byte[] buffSize = new byte[size];
				stream.read(buffSize);
				try{
				bigI = new BigInteger(buffSize);
				}catch(Exception e){
					
				}
			} else {
				continue;
			}
			char header3 = (char) stream.read();
			if(header3!='#'){
				continue;
			}
				
			if (bigI != null) {			
				imageIs = new MemoryCacheImageInputStream(stream);
				BufferedImage image = null;
				try {
					image = ImageIO.read(imageIs);
				} catch (Exception e) {
					e.printStackTrace();
					image = null;
				}
				System.out.println("image read... ");
				if (image == null) {
					System.out.println("image is null :(");
					continue;
				}
				addFrame(image);
			}
		}
	}

	public void addFrame(BufferedImage image) {
		System.out.println("Adding new frame");
		viewer.addFrame(image);
	}

	public static class Client {
		private Socket socket = null;
		private OutputStream os;
		ImageOutputStream imageOs;

		public Client() {

		}

		public void start(int port) throws UnknownHostException, IOException {
			socket = new Socket("localhost", port);
			os = socket.getOutputStream();
			imageOs = new MemoryCacheImageOutputStream(os);
		}

		public void sendImage(BufferedImage image) throws IOException {
			System.out.println("Sending image");
			ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
			imageOs = new MemoryCacheImageOutputStream(byteOs);
			boolean written = false;
			try {
				written = ImageIO.write(image, "jpg", imageOs);
			} catch (Exception e) {
				e.printStackTrace();
			}
			byte[] imageBytes = byteOs.toByteArray();
			System.out.println("Image sent: " + written + " - "
					+ imageOs.length() + " - " + imageBytes.length);
			BigInteger i = BigInteger.valueOf(imageOs.length());
			byte[] length = i.toByteArray();
			os.write((char) '#');
			os.write((char) '#');
			os.write(length.length);
			os.write(length);
			os.write((char) '#');
			os.write(imageBytes);
			os.flush();
			// imageOs.flush();
			// os.flush();

			// imageOs.reset();
		}

	}

	public static void main(String[] args) throws AWTException,
			UnknownHostException, IOException, InterruptedException,
			InvocationTargetException {
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

		final Client client = new Client();
		new Thread(new Runnable() {

			public void run() {
				try {
					client.start(PORT);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

		new Thread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				Robot r = null;
				try {
					r = new Robot();
				} catch (AWTException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (true) {
					System.out.println(">>>>>>>>>>>>>>><Resending screenshot");
					BufferedImage s = r.createScreenCapture(new Rectangle(
							Toolkit.getDefaultToolkit().getScreenSize()));
					double scale = 0.75;
					int w = (int) (s.getWidth() * scale);
					int h = (int) (s.getHeight() * scale);
					BufferedImage outImage = new BufferedImage(w, h,
							BufferedImage.TYPE_INT_RGB);
					AffineTransform trans = new AffineTransform();
					trans.scale(scale, scale);
					Graphics2D g = outImage.createGraphics();
					g.drawImage(s, trans, null);
					g.dispose();
					System.out
							.println(">>>>>>>>>>>>>>> invoking client for sending image");
					try {
						client.sendImage(outImage);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// System.out.println("Adding new frame...");
					// viewer.addFrame(outImage);
				}
			}
		}).start();
		// ImageListenerViewer viewer = new ImageListenerViewer();
		// viewer.start();

	}

}
