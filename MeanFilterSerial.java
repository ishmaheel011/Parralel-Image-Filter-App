import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Arrays;

public class MeanFilterSerial
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
		
		//input check

		//intialise image properties
		int w = 0;
		int h = 0;
		
		BufferedImage image = null;
		BufferedImage image2 = null;
		File f = null;
		
		// try read old image and write new image
		try
		{
			// read old image
			f = new File(oldF);
			image = ImageIO.read(f);
			
			//get image dimensions 
			w = image.getWidth();
			h = image.getHeight();
			
			//create Filter object
			MeanSerial filter = new MeanSerial(image, filt);
			
			//initialize new image object for filtered image
			image2 = ImageIO.read(f);
			
			//start timer
			tik();
			
			for(int x=(filt/2); x < w-(filt/2); x++)
			{
				for(int y=(filt/2); y < h-(filt/2); y++)
				{
					image2.setRGB(x,y, filter.mean(x,y));
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
	
	
} // Main class ends here


class MeanSerial //auxillary class
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
	public MeanSerial(BufferedImage image, int filt)
	{
		this.image = image;
		this.filt = filt;
		range = (filt/2);
	}
	
	
	/*
	returns the mean of the rgb values that fall within the filter width
	*/
	public int mean(int x, int y)
	{
		this.rgb = 0;
		this.a = 0;
		this.r = 0;
		this.g = 0;
		this.b = 0;
		int mean = 0;
		// get rgb values of pixels within range of the filter width from (x,y)
		for(int t=x-range; t <= x+range ; t++)
		{
			for(int u=y-range; u <= y+range ; u++)
			{
				//get rgb value of pixel in (a,b)
				rgb = image.getRGB(t,u);
				
				//get alpha and add to mean
				a += (rgb>>24) & 0xff;
	
				//get red and add to mean
				r += (rgb>>16) & 0xff;

    				//get green and add to mean
    				g += (rgb>>8) & 0xff;

    				//get blue and add to mean blue
    				b += rgb & 0xff;
			}
		}
		
		// calculate mean a
		a = a/(filt*filt);
		
		// calculate mean r
		r = r/(filt*filt);
		
		// calculate mean g
		g = g/(filt*filt);
		
		// calculate mean b
		b = b/(filt*filt);
		
		//set new rgb
		
		//shift alpha
		mean = mean | (a<<24);
		
		// shift r
		mean = mean | (r<<16);
		
		//shift g
		mean = mean | (g<<8);
		
		//shift b
		mean = mean | b;
		
		return mean;

	}
	
}
