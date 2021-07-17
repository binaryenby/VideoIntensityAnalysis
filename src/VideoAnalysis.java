import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.*;

import java.util.ArrayList;

import javax.swing.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VideoAnalysis {
//Tells it to capture a frame every 24th of a second, the standard frame-rate for Hollywood movies
public static final double SECONDS_BETWEEN_FRAMES = (1.0/24);

//New height and width for the picture to be adjusted to, saves processing power
public static final int rHeight = 180;
public static final int rWidth = 320;

	public static void main(String[] args) throws IOException {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		VideoAnalysis va = new VideoAnalysis();
		
		//Uses an JFileChooser object to bring up a file menu.
		String inputFilename = va.openDialog();
		//If the user makes a valid choice, this code executes
		if(inputFilename != null) {
			
			ImageSnapListener listener = va.listenerFactory();
			
			System.out.println(inputFilename);
			
			listener.start(inputFilename);
			
			listener.onClose();
			
			System.exit(0);
		}
		//This code executes if a proper file is not chosen
		else {
			System.out.println("No file chosen, run the program again to choose a new file.");
			
			System.exit(1);
		}
	}
	
	//Constructor
	public VideoAnalysis() {}
	
	//File Chooser window to select which video file to analyze
	public String openDialog() {
		
		//Creates a new JFrame that will exit when closed
		JFrame frame = new JFrame("Open");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		final JFileChooser fc = new JFileChooser();
		
		//Indicates whether or not a proper file was chosen
		int returnVal = fc.showOpenDialog(frame);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			//Sets inputFilename to the name and directory of the selected file
			String inputFilename = fc.getCurrentDirectory() + "\\" + fc.getSelectedFile().getName();
			//Returns the name of the file to be used
			return inputFilename;
		}
		
		else {
			//If no file is chosen
			System.out.println("Choice cancelled by user.");
			return null;
		}
	}
	/*
	 * Factory method to return an ImageSnapListener
	 */
	private ImageSnapListener listenerFactory() {
		ImageSnapListener listener = new ImageSnapListener();
		
		return listener;
	}
	
	/*
	 * The core of the program, this class goes through the video file frame by frame, 
	 * analyzes the pixels from each frame, and does the necessary calculations to get the
	 * average brightness of each frame, measure the standardized absolute difference between
	 * each two frames, and keep track of how many frames exceed a level of absolute difference
	 * that is indicative of a sudden scene change or a visually intense event.
	 */
	public class ImageSnapListener {
		//The total number of scene changes in the video
		int numSceneChanges = 0;
		//List of Integers to store brightness values for each frame
		ArrayList<Integer> brArray = new ArrayList<Integer>();
		//List of Integers to store pixel values for the frame before the current frame
		ArrayList<Integer> nMinusOneArray = new ArrayList<Integer>();
		//List of Integers to store values for the Sum of Absolute Difference for each frame, compared to the last frame
		ArrayList<Integer> sADArray = new ArrayList<Integer>();
		//List of Integers to store pixel values for the current frame
		ArrayList<Integer> nArray = new ArrayList<Integer>();
		
		int frameNumber = 0;
		
		JFrame f;
		
		public void start(String fileName) {
			
			Mat frame = new Mat();
			
			//Creates a VideoCapture object attached to the video file
			VideoCapture capture = new VideoCapture(fileName);
				
			//Opens the file if it hasn't been already
			capture.open(fileName);
				
			
			//Read out the contents of the media file and pass each frame to imageProcessing
			if (capture.isOpened()) {
				while (capture.read(frame)) {
					imageProcessing(frame);
				}
			}
			//This should never happen, but if it does:
			else {
				System.out.println("Failed to open");
				System.exit(1);
			}
		}
		
		//Is called after the video processing is done
		public void onClose() throws IOException {
			//Calls the method that will save brightness data as a csv file
			csvMaker();
		}
		
		//Is called on each frame to gather data from it
		private void imageProcessing(Mat image) {
			
			int w = image.width();
			int h = image.height();
			BufferedImage img;
	        byte[] data = new byte[w * h * (int)image.elemSize()];
	        image.get(0, 0, data);
	        
	        //Creates a new BufferedImage & converts from Mat to a BufferedImage
	        img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);

	        img.getRaster().setDataElements(0, 0, w, h, data);
	        
			//Original width and height of the frame
	   	 	int width = img.getWidth();  
	        int height = img.getHeight();  
	        //rgb values
	        int r = 0;
	        int g = 0;
	        int b = 0;
	        //brightness value
	        int br = 0;
	        //Total brightness of the frame
	        int totalbr = 0;
	        //Number of pixels
	        int pixelnum = rWidth * rHeight;
	        //Average brightness of the frame
	        int avgbr = 0;
	        
	        System.out.println("Processing Frame" + frameNumber);
	        //Converts image to 320x180 to make for a more manageable amount of pixels & faster runtime
	        BufferedImage dimg = new BufferedImage(rWidth, rHeight, img.getType());  
	        Graphics2D graph = dimg.createGraphics();  
	        graph.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);  
	        graph.drawImage(img, 0, 0, rWidth, rHeight, 0, 0, width, height, null);  
	        graph.dispose();
	        
	        //For each pixel in the frame
	        for (int i = 0; i < rWidth; i++) {
	       	 for (int j = 0; j < rHeight; j++) {
	       		 
	       		 //Gets the color of the pixel
	       		Color color = new Color(dimg.getRGB(i, j));
	       		
	       		pixelValueCalculator(color);
	        
	       			//Gets rgb values of the color
	        		r = color.getRed();
	        		g = color.getGreen();
	        		b = color.getBlue();
	        		//Standard luminance formula (Sedgewick and Wayne 2008)
	        		br = (int) (r*0.299 + g*0.587 + b*.114);
	        		//Adds this brightness value to the total
	        		totalbr = totalbr + br;		
	        
	       	 }
	        }

	        //Divides total brightness by the number of pixels and assigns that to an Integer
	        avgbr = Integer.valueOf((int)totalbr/pixelnum);
	        //Adds the Integer to the array of brightness values 
	        brArray.add(avgbr);
	        //Calculates the Sum of Absolute Difference for the current frame compared to the last one
	        sADCalculator();
	        
	        frameNumber++;
	    }
		
		private void pixelValueCalculator(Color c) {
			//RGB values for the current pixel
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();
			//Average intensity value for the pixel
			int pixelValue = (int)((r + g + b)/3);
			//Adds the pixel value to the array for the current frame
			nArray.add(pixelValue);
		}
		
		private void sADCalculator() {
			//The Sum of Absolute Difference between this frame and the last one, normalized
			int trueSAD = 0;
			//Sum of Absolute Difference for each pixel added together
			int sAD = 0;
			//Checks to ensure there is a frame before this one to compare it to
			if (nMinusOneArray.isEmpty() == false) {
				//For each pixel in the frame
				for (int i = 0; i < (rWidth * rHeight); i++) {
					//Adds the absolute difference of the current pixel to the total SAD value
					sAD = sAD + Math.abs(nMinusOneArray.get(i) - nArray.get(i));
				}
				//Normalizes the total SAD value
				trueSAD = sAD/(rWidth * rHeight);
				//Adds this frame's SAD value to the list
				sADArray.add(trueSAD);
				//Adds to the number of scene changes if the SAD is over the value of 30	
				if (trueSAD >= 30) {
					numSceneChanges++;
				}
				
			}
			//Clears the list for the last frame
			nMinusOneArray.clear();
			//Transfers the current frame's data to the list it needs to be in for the next iteration
			nMinusOneArray.addAll(nArray);
			//Clears the current list
			nArray.clear();
		}
		
		private void csvMaker() throws IOException {
			boolean valid = false;
			File output = new File("filler");
			
			//Loop to ensure a valid save file is created and the analysis is not lost
			while(valid == false) {
				String fileName = saveDialog();
				
				//NSL means the user failed to choose a save location
				if(fileName == "NSL") {
					System.out.println("Please choose a valid save location/file name or" +
							" close the program if you do not wish to save your results.");
				
					fileName = saveDialog();
				}
				
				//If a valid save location and name is chosen, attempt to create the file
				else {
					output = new File(fileName + ".csv");
					if(output.createNewFile()) {
						System.out.println("Save file created: " + output.getName());
						valid = true;
					}
					//If the file cannot be created, one of the same name most likely exists
					else {
						System.out.println("Saving failed. Please make sure the file name" + 
								" provided does not already exist in the destination folder.");
					}
				}
			}
			
			FileWriter fw = new FileWriter(output);
			//Writes the column headers to the csv file
			fw.write("Frame Number,SAD,Brightness,Number of scene changes: " + numSceneChanges + "\n");
			
			//Writes the brightness for the first frame, since there is no sAD value for it
			fw.write("1,NA," + brArray.get(0) + "\n");
			
			//Writes each frame's values to the csv file
			for(int i = 0; i < sADArray.size(); i++) {
				fw.write(i+2 + "," + sADArray.get(i) + "," + brArray.get(i + 1) + "\n");
			}
			
			fw.close();
		}
		
		//Handles the save dialog the user interacts with
		public String saveDialog() {
			//Creates a JFrame that exits on close
			JFrame frame = new JFrame("Save");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			final JFileChooser save = new JFileChooser();
			//If a valid save location was chosen, returnVal==APPROVE_OPTION
			int returnVal = save.showSaveDialog(frame);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			//Saves the file in the specified location and adds the .csv extension
			String ssFilename = save.getCurrentDirectory() + "\\" + save.getSelectedFile().getName();
			return ssFilename;
			}
			
			//If no valid save location is chosen, it will return NSL for "no save location"
			else {
				
				System.out.println("A save location is required. Please select a save location.");
				return "NSL";
			}
		}
	}
}
