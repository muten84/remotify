package it.luigibifulco.xuggler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.xuggle.xuggler.demos.VideoImage;

public class ImageListenerViewer implements Runnable {

	private static VideoImage mScreen = null;

	private List<BufferedImage> buffer = new ArrayList<BufferedImage>();

	private final static int FLUSH_BUFFER_SIZE = 1;

	public final static int RATE = 1;

	private BlockingQueue<BufferedImage> frames = new LinkedBlockingQueue<BufferedImage>(5);

	private final static long SLOW_DOWN_FACTOR = 300;

	private final static Object LOCK = new Object();

	private ExecutorService executor;

	private BufferedImage lastImage;

	boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
		if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
			for (int x = 0; x < img1.getWidth(); x++) {
				for (int y = 0; y < img1.getHeight(); y++) {
					if (img1.getRGB(x, y) != img2.getRGB(x, y))
						return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public void addFrame(BufferedImage image) {
		// boolean proceed=false;
		// if(lastImage==null) {
		// lastImage = image;
		// }
		// if(image!=null && lastImage !=null && bufferedImagesEqual(image, lastImage)) {
		// proceed = true;
		// }
		boolean proceed = true;
		if (proceed && image != null) {
			lastImage = image;
			frames.offer(image);
		} else {
			System.out.println("image not add is null");
		}

	}
	
	public static void updateTitle(String msg) {
		mScreen.setTitle(msg);
	}

	public void start() throws InterruptedException, InvocationTargetException {
		executor = Executors.newFixedThreadPool(1);
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
			// synchronized (LOCK) {
			int size = buffer.size();
			if (size == FLUSH_BUFFER_SIZE) {
				//System.out.println("Buffer size reached :" + size);
				// meglio sottomettere questi thread come task ad un executor....
				BufferReader reader = new BufferReader(new ArrayList<BufferedImage>(buffer));
				// Thread readerThread = new Thread(reader);
				try {
					executor.submit(reader);
				} finally {
					buffer.clear();
				}
				continue;

			} else if (buffer.size() >= FLUSH_BUFFER_SIZE) {
				try {
					System.out.println("Buffer full waiting for reading...");
					Thread.sleep(SLOW_DOWN_FACTOR);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// continue;
			}
			// System.out.println("Waiting for new frames...");
			BufferedImage image = null;
			try {
//				if (frames.size() > MAX_BUFFER_SIZE) {
//					List<BufferedImage> drain = new ArrayList<BufferedImage>();
//					frames.drainTo(drain);
//					image = drain.get(drain.size() - 1);
//				}
				if (image == null) {
					image = frames.take();
					// poll(SLOW_DOWN_FACTOR, TimeUnit.MILLISECONDS);
					if (image != null) {
						buffer.add(image);
					}
				}
//				else {
//					buffer.add(image);
//				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// }
	}

	public static class BufferReader implements Runnable {
		private List<BufferedImage> buffer;

		public BufferReader(List<BufferedImage> buffer) {
			this.buffer = buffer;
		}

		public void run() {
			// read buffer and show on video screen
			// synchronized (LOCK) {
//			System.out.println(">>>>>START Reading " + this.buffer.size() + "frames");
			for (BufferedImage bi : this.buffer) {
				try {
					Thread.sleep(RATE);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				final BufferedImage bgrScreen = convertToType(bi, BufferedImage.TYPE_3BYTE_BGR);
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						public void run() {
							updateTitle("Refreshing...");
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
			}
			buffer.clear();
			// }
		}

	}

	private static void updateJavaWindow(BufferedImage javaImage) {
//		System.out.println("update image on window");		
		mScreen.setImage(javaImage);		
		// mScreen.repaint();
	}

	/**
	 * Opens a Swing window on screen.
	 */
	private static void openJavaWindow() {
		System.out.println("Opening java window");
		mScreen = new VideoImage();
		mScreen.setTitle("Server is waiting for incoming connection....");
		mScreen.setSize(640, 480);

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
		for (int i = 0; i < 100; i++) {
			for (File file : listOfFiles) {
				if (file.isFile()) {
					indexVal++;
					System.out.println("file.getName() :" + file.getName());
					// Thread.sleep(50);
					viewer.addFrame(getImageFromFile(file));
				}
			}
		}

	}

	public static BufferedImage getImageFromFile(File img) {

		try {
//			System.out.println("fileName :" + img.getName());

			BufferedImage in = null;
			if (img != null) {
//				System.out.println("img :" + img.getName());
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
