package it.luigibifulco.xuggler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.xuggle.xuggler.demos.VideoImage;

public class ImageListenerViewer implements Runnable {

	private static VideoImage mScreen = null;

	private BlockingQueue<BufferedImage> frames = new LinkedBlockingQueue<BufferedImage>();

	private List<BufferedImage> buffer = new ArrayList<BufferedImage>();

	private final static int MAX_BUFFER_SIZE = 3;

	private final static Object LOCK = new Object();

	private ExecutorService executor;

	public void addFrame(BufferedImage image) {
		frames.offer(image);
	}

	public void start() throws InterruptedException, InvocationTargetException {
		executor = Executors.newFixedThreadPool(10);
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				openJavaWindow();

			}
		});

		Thread bufferFiller = new Thread(this);
		bufferFiller.start();
	}

	// buffer filler
	public void run() {
		while (true) {
			synchronized (LOCK) {
				if (buffer.size() == MAX_BUFFER_SIZE) {
					System.out.println("Buffer size reached ");
					// meglio sottomettere questi thread come task ad un executor....
					BufferReader reader = new BufferReader(new ArrayList<BufferedImage>(buffer));
					// Thread readerThread = new Thread(reader);
					executor.submit(reader);
					buffer.clear();
				} else if (buffer.size() >= MAX_BUFFER_SIZE) {
					try {
						System.out.println("Buffer full waiting for reading...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				System.out.println("Waiting for new frames...");
				BufferedImage image;
				try {
					image = frames.poll(100, TimeUnit.MILLISECONDS);
					buffer.add(image);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static class BufferReader implements Runnable {
		private List<BufferedImage> buffer;

		public BufferReader(List<BufferedImage> buffer) {
			this.buffer = buffer;
		}

		public void run() {
			// read buffer and show on video screen
			synchronized (LOCK) {
				System.out.println("Reading " + this.buffer.size() + "frames");
				for (BufferedImage bi : this.buffer) {
					final BufferedImage bgrScreen = convertToType(bi, BufferedImage.TYPE_3BYTE_BGR);
					try {
						SwingUtilities.invokeAndWait(new Runnable() {

							public void run() {
								updateJavaWindow(bgrScreen);

							}
						});
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				buffer.clear();
			}
		}

	}

	private static void updateJavaWindow(BufferedImage javaImage) {
		System.out.println("update image on window");
		mScreen.setImage(javaImage);
		mScreen.repaint();
	}

	/**
	 * Opens a Swing window on screen.
	 */
	private static void openJavaWindow() {
		System.out.println("Opening java window");
		mScreen = new VideoImage();
		mScreen.setSize(400, 400);

	}

	public static BufferedImage convertToType(BufferedImage sourceImage, int targetType) {
		BufferedImage image;
		if (sourceImage.getType() == targetType) {
			image = sourceImage;
		} else {
			image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}
		return image;
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		ImageListenerViewer viewer = new ImageListenerViewer();
		viewer.start();
		System.out.println("Viewer started...");
		File folder = new File("C:/temp/images");
		File[] listOfFiles = folder.listFiles();
		int indexVal = 0;
		for (File file : listOfFiles) {
			if (file.isFile()) {
				indexVal++;
				System.out.println("file.getName() :" + file.getName());
				viewer.addFrame(getImageFromFile(file));
			}
		}

	}

	private static BufferedImage getImageFromFile(File img) {

		try {
			System.out.println("fileName :" + img.getName());

			BufferedImage in = null;
			if (img != null) {
				System.out.println("img :" + img.getName());
				in = ImageIO.read(img);
			}
			return in;

		}

		catch (Exception e) {

			e.printStackTrace();

			return null;

		}

	}

}
