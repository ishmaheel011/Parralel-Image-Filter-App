import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import  java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class MedianFilterParallel
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
	
		//forkjoin pool
		ForkJoinPool pool = new ForkJoinPool();
		
		// get image names and filter WIDTH from args[]
		String oldF = args[0];
		String newF = args[1];
		int filt = Integer.valueOf(args[2]);
		
		// check if filter size is odd and >= 3
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

		File f = null;
		
		// try read old image and write new image
		try
		{
			// read old image
			f = new File(oldF);
			MedianParallel.IMAGE = ImageIO.read(f);
			MedianParallel.WIDTH = MedianParallel.IMAGE.getWidth();
			MedianParallel.HEIGHT = MedianParallel.IMAGE.getHeight();
			MedianParallel.FILT = filt;
			MedianParallel.RANGE = filt/2;

			
			//initialize new image object for filtered image
			MedianParallel.IMAGE2 = ImageIO.read(f);
			
			/*
			since most images are more likely to have height > WIDTH, we'll split h in 2 until we reach seq. cutoff
			then each thread will run from filt/2 to WIDTH-filt/2 for all lo <= y <= hi
			*/
			
			//start timer
			tik();

			//invoke pool
			pool.invoke(new MedianParallel(filt-2, MedianParallel.HEIGHT-(filt/2) ));
			
			// end timer
			duration = tok();
			
			//write new image
			f = new File(newF);
			ImageIO.write(MedianParallel.IMAGE2, "png", f);
			
			//print message for user
			System.out.println(String.valueOf(duration) + " seconds");
			
		}
		catch(IOException e)
		{
			//print message if source image can't be read
			System.out.println("Error : " +e);
		}
		
		
	} // main method ends here
	
	
} // class ends here



class MedianParallel extends RecursiveAction
{
	// arguments for image object and index for each thread
	static BufferedImage IMAGE = null;
	static BufferedImage IMAGE2 = null;
	static int FILT = 0;
	static int WIDTH = 0;
	static int HEIGHT = 0;
	int lo;
	int hi;
	
	//seq cutoff
	static final int SEQUENTIAL_CUTOFF = 100;
	
	//variables for filtering pixels
	static int RANGE = 0;
	int rgb;
	int a;
	int r;
	int g;
	int b;
	/*
	intialises fields for our filter class
	*/
	public MedianParallel(int lo, int hi)
	{
	
		this.lo = lo;
		this.hi = hi;
	}
	
	//compute method
	protected void compute()
	{
		if((hi-lo)< SEQUENTIAL_CUTOFF) 
		{
			for(int n=FILT/2; n<WIDTH-(FILT/2); n++)
			{
				for(int m=lo; m< hi; m++)
				{
					IMAGE2.setRGB(n,m, median(n,m)); // set new RGB in image 2
				}
			}
		}
		else 
		{
			MedianParallel left = new MedianParallel(lo,(hi+lo)/2);
			MedianParallel right = new MedianParallel((hi+lo)/2,hi);
		    	left.fork();
		    	right.compute(); 
		    	left.join(); 
		}		
	
	}
	
	
	/*
	returns the mean of the rgb values that fall within the filter WIDTH
	*/
	public int median(int x, int y)
	{
		
	
		int counter = 0;
		int median = 0;
		int[] alpha = new int[FILT*FILT];
		int[] red = new int[FILT*FILT];
		int[] green = new int[FILT*FILT];
		int[] blue = new int[FILT*FILT];
		
		// get rgb values of pixels within range of the filter WIDTH from (x,y)
		for(int a=x-RANGE; a <= x+RANGE ; a++)
		{
			for(int b=y-RANGE; b <= y+RANGE ; b++)
			{
				//get rgb value of pixel in (a,b)
				rgb = IMAGE.getRGB(a,b);
				
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
		a = alpha[(FILT*FILT)/2];
		
		//find median of a
		r = red[(FILT*FILT)/2];
		
		//find median of a
		g = green[(FILT*FILT)/2];
		
		//find median of a
		b = blue[(FILT*FILT)/2];
		
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
