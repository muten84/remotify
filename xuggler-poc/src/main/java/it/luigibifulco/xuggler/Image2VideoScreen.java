package it.luigibifulco.xuggler;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import com.xuggle.xuggler.demos.VideoImage;

public class Image2VideoScreen {

	private static VideoImage mScreen = null;

	private static Map<String, File> imageMap = new HashMap<String, File>();

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		openJavaWindow();
		File folder = new File("C:/temp/images");
		File[] listOfFiles = folder.listFiles();

		int indexVal = 0;
		for (File file : listOfFiles) {
			if (file.isFile()) {
				indexVal++;
				System.out.println("file.getName() :" + file.getName());
				imageMap.put(file.getName(), file);
			}
		}

		// for (int index = 0; index < SECONDS_TO_RUN_FOR * FRAME_RATE; index++) {
		for (int index = 1; index <= listOfFiles.length; index++) {
			BufferedImage screen = getImage(index);
			final BufferedImage bgrScreen = convertToType(screen, BufferedImage.TYPE_3BYTE_BGR);
			// writer.encodeVideo(0, bgrScreen, 300 * index, TimeUnit.MILLISECONDS);
			SwingUtilities.invokeAndWait(new Runnable() {

				public void run() {
					updateJavaWindow(bgrScreen);

				}
			});

			Thread.sleep(300);
		}
	}

	private static BufferedImage getImage(int index) {

		try {
			String fileName = index + ".jpg";
			System.out.println("fileName :" + fileName);
			File img = imageMap.get(fileName);

			BufferedImage in = null;
			if (img != null) {
				System.out.println("img :" + img.getName());
				in = ImageIO.read(img);
			} else {
				System.out.println("++++++++++++++++++++++++++++++++++++++index :" + index);
				img = imageMap.get(1);
				in = ImageIO.read(img);
			}
			return in;

		}

		catch (Exception e) {

			e.printStackTrace();

			return null;

		}

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

}
