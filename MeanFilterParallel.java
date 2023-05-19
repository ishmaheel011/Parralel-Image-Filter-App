import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import  java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

public class MeanFilterParallel 
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

		File f = null;
		
		// read old image and write new image
		try
		{
			// read old image
			f = new File(oldF);
			MeanParallel.IMAGE = ImageIO.read(f);
			MeanParallel.WIDTH = MeanParallel.IMAGE.getWidth();
			MeanParallel.HEIGHT = MeanParallel.IMAGE.getHeight();
			MeanParallel.FILT = filt;
			MeanParallel.RANGE = filt/2;

			
			//initialize new image object for filtered image
			MeanParallel.IMAGE2 = ImageIO.read(f);
			
			/*
			since most images are more likely to have height > width, we'll split h in 2 until we reach seq. cutoff
			then each thread will run from filt/2 to width-filt/2 for all lo <= y <= hi
			*/

			//start timer
			tik();

			//invoke pool, leaves borders out when filtering by starting from filt-2 and ending at h-filt/2
			pool.invoke(new MeanParallel(filt-2, MeanParallel.HEIGHT-(filt/2) ));

			duration = tok();
			
			f = new File(newF);
			ImageIO.write(MeanParallel.IMAGE2, "png", f);

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

class MeanParallel extends RecursiveAction
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
	static final int SEQUENTIAL_CUTOFF=100;
	
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
	public MeanParallel(int lo, int hi)
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
					IMAGE2.setRGB(n,m, mean(n,m)); // set new RGB in image 2
				}
			}
		}
		else 
		{
			MeanParallel left = new MeanParallel(lo,(hi+lo)/2);
			MeanParallel right = new MeanParallel((hi+lo)/2,hi);
		    	left.fork();
		    	right.compute();
		    	left.join(); 
		}		
	
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
		for(int t=x-RANGE; t <= x+RANGE ; t++)
		{
			for(int u=y-RANGE; u <= y+RANGE ; u++)
			{
				//get rgb value of pixel in (a,b)
				rgb = IMAGE.getRGB(t,u);
				
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
		a = a/(FILT*FILT);
		
		// calculate mean r
		r = r/(FILT*FILT);
				
		// calculate mean g
		g = g/(FILT*FILT);
		
		// calculate mean b
		b = b/(FILT*FILT);
		
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

	} //methods ends here
	
}
