import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Arrays;
public class MedianFilterSerial
{
	static double startTime = 0.0;
	static double duration = 0.0;
	
	private static void tik(){
		startTime = System.currentTimeMillis();
	}
	private static double tok(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}
	public static void main(String[] args) throws IOException
	{
		
		// get image names and filter width from args[]
		String oldF = args[0];
		String newF = args[1];
		int filt = Integer.valueOf(args[2]);
		
		if(!(filt >= 3 && filt%2 != 0))
		{
			System.out.println("filter size must be = 3 or >3 and odd");
			System.exit(0);
		}
		
		if(!(filt >= 3 && filt%2 != 0))
		{
			System.out.println("filter size must be = 3 or >3 and odd");
			System.exit(0);
		}
		
		//input check

		//intialise image properties
		int w = 0;
		int h = 0;
		
		BufferedImage image = null;
		BufferedImage image2 = null;
		File f = null;
		
		// read old image and write new image
		try
		{
			// read old image
			f = new File(oldF);
			image = ImageIO.read(f);
			
			//get image dimensions 
			w = image.getWidth();
			h = image.getHeight();
			
			//create Filter object
			MedianSerial filter = new MedianSerial(image, filt);
			
			//initialize new image object for filtered image
			image2 = ImageIO.read(f);
			
			//start timer
			tik();
			
			// only start at filt/2, end at dimension-filt/2, leaves borders unfiltered
			for(int x=(filt/2); x < w-(filt/2); x++)
			{
				for(int y=(filt/2); y < h-(filt/2); y++)
				{
					image2.setRGB(x,y, filter.median(x,y));
				}
			}
			
			// end timer
			duration = tok();
			
			f = new File(newF);
			ImageIO.write(image2, "png", f);
			
			//print message for user
			System.out.println(String.valueOf(duration) + " seconds");
			
			
		}
		catch(IOException e)
		{
			//print message if source image can't be read
			System.out.println("Error : " +e);
		}
		
		
	} // main method ends here
	
}// class ends here


class MedianSerial //auxillary class
{
	int filt;
	int range;
	BufferedImage image;
	int rgb;
	int a;
	int r;
	int g;
	int b;
	
	/*
	intialises fields for our filter class
	*/
	public MedianSerial(BufferedImage image, int filt)
	{
		this.image = image;
		this.filt = filt;
		range = (filt/2);
	}
	
	/*
	returns the mean of the rgb values that fall within the filter width
	*/
	public int median(int x, int y)
	{
		
	
		int counter = 0;
		int median = 0;
		int[] alpha = new int[filt*filt];
		int[] red = new int[filt*filt];
		int[] green = new int[filt*filt];
		int[] blue = new int[filt*filt];
		
		// get rgb values of pixels within range of the filter width from (x,y)
		for(int a=x-range; a <= x+range ; a++)
		{
			for(int b=y-range; b <= y+range ; b++)
			{
				//get rgb value of pixel in (a,b)
				rgb = image.getRGB(a,b);
				
				//get alpha and add to array of alpha values
				alpha[counter] = (rgb>>24) & 0xff;
	
				//get red and add to array of red values
				red[counter] = (rgb>>16) & 0xff;

    				//get green and add to array of green values
    				green[counter] = (rgb>>8) & 0xff;

    				//get blue and add to array of blue values
    				blue[counter] = rgb & 0xff;
    				
    				counter = counter+1;
			}
		}
		
		// sort the arrays
		Arrays.sort(alpha);
		Arrays.sort(red);
		Arrays.sort(green);
		Arrays.sort(blue);
		
		//find median of a
		a = alpha[(filt*filt)/2];
		
		//find median of a
		r = red[(filt*filt)/2];
		
		//find median of a
		g = green[(filt*filt)/2];
		
		//find median of a
		b = blue[(filt*filt)/2];
		
		//set new rgb
		
		//shift alpha
		median = median | (a<<24);
		
		// shift r
		median = median | (r<<16);
		
		//shift g
		median = median | (g<<8);
		
		//shift b
		median = median | b;
		
		return median;
	}
	
	
}
