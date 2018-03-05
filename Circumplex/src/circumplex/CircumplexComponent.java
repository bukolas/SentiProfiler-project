package circumplex;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.*;
import java.text.DecimalFormat;

/** CircumplexComponent.java - Specification of Graphic component of the circumplex annotation tool.
 *  Defines X and Y axis labels, individual emotional labels, and angle and intensity selection
 *  
 * @version 1.0 - 2011
 * */


public class CircumplexComponent extends Component implements MouseListener, MouseMotionListener {

	int pointX, pointY; 
	float lastRadAngle;
	int radius;
	
	/* angle represents the emotional quality of a sentence
	 * and length represents intensity of emotion */
	
	float angle;
	float length;
	
	/* X and Y axis labels */
	String unpleasant = "Unpleasant (180)";
	String pleasant = "Pleasant (0/360)";	
	String hEnergy = "High Energy (270)";
	String lEnergy = "Low Energy (90)";
	String label;
	
	/* Predefined emotional quality labels outlined in Russell (1980)*/
	String happy = "Happy";
	String excited = "Excited";
	String aroused = "Aroused";
	String angry = "Angry";
	String afraid = "Afraid";
	String frustrated = "Frustrated";
	String miserable= "Miserable";
	String sad = "Sad";
	String depressed = "Depressed";
	String bored = "Bored";
	String atease = "Atease";
	String pleased = "Pleased";
	
	
	/** Mouse listener to annotation interface component */	
	public CircumplexComponent() {		
		addMouseMotionListener(this);
		addMouseListener(this);
	}
	
	@Override
	public void paint(Graphics g) {

		/* Drawing of X and Y axis labels*/
		g.setFont(new Font("Monospace", Font.BOLD, 11));
		//g.setColor(Color.getHSBColor(1, 0, 0.68f));
		g.setColor(Color.BLUE);
		g.drawString(hEnergy, this.getWidth()/2 + 5, 10);
		g.drawString(lEnergy, this.getWidth()/2 + 5, this.getHeight()-2);
		g.drawString(unpleasant, 0, this.getHeight()/2 + 10);
		g.drawString(pleasant, (int) (this.getWidth()- unpleasant.length()*4.2f-15), this.getHeight()/2 + 10);
		
		//X and Y axis thick lines
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.BLUE);
		g2.setStroke(new BasicStroke(2));
		g2.drawLine(0, this.getHeight()/2, this.getWidth(), this.getHeight()/2);
		g2.drawLine(this.getWidth()/2, 0, this.getWidth()/2, this.getHeight());

		//central point of circumplex component
		int BegX=this.getWidth()/2;
		int BegY=this.getHeight()/2;
		
		/* Drawing of emotion angle lines and labels - angles calculated from Russell(1980)*/
		//Line thickness of emotions
		g2.setColor(Color.GRAY);
		g2.setStroke(new BasicStroke(1));
		
		//Guidelines of intensities
		int W = (int)(this.getWidth()/2.65);
		int H = (int)(this.getHeight()/2.65);
		g2.setColor(Color.YELLOW);
		g2.drawArc(W, H, BegX/2, BegY/2, 0, 360);
		g2.setColor(Color.ORANGE);
		g2.drawArc(BegX/2, BegY/2, BegX, BegY, 0, 360);
		g2.setColor(Color.RED);
		g2.drawArc(BegX/4, BegY/4,(int)(BegX*1.5),(int)(BegY*1.5), 0, 360);
		//uncomment for the outer (extreme) intensity guideline - But it makes the component not look clean.
		//The extreme intensity would be the full length of the radius
		//g2.setColor(Color.MAGENTA);
		//g2.drawArc(0, 0,this.getWidth(),this.getHeight(), 0, 360); 
			
		
		//Line thickness and color of emotions
		g.setFont(new Font("Monospace", Font.PLAIN, 10));
		g2.setColor(Color.GRAY);
		float [] dashPattern = {10,5,10,5};		
		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
		
		
		// Happy
		int EndYh= (int) ((int)BegX + getWidth()/2.0f * Math.sin(6.147835024));
		int EndXh= (int) ((int)BegX + getWidth()/2.0f * Math.cos(6.147835024));
		g.drawLine(BegX, BegY, EndXh, EndYh);
		g.drawString(happy, (int)(this.getWidth() + 20 - unpleasant.length()*4.2f), this.getHeight()/2 - 20);
		/*
		// delighted
		int EndYd= (int) ((int)BegX + getWidth()/2.0f * Math.sin(5.847922));
		int EndXd= (int) ((int)BegX + getWidth()/2.0f * Math.cos(5.847922));
		g.drawLine(BegX, BegY, EndXd, EndYd);
		*/
		// excited
		int EndYe= (int) ((int)BegX + getWidth()/2.0f * Math.sin(5.43536841));
		int EndXe= (int) ((int)BegX + getWidth()/2.0f * Math.cos(5.43536841));
		g.drawLine(BegX, BegY, EndXe, EndYe);
		g.drawString(excited, (int)(this.getWidth() - 40 - unpleasant.length()*4.2f), 60);
		/*
		// astonished
		int EndYa= (int) ((int)BegX + getWidth()/2.0f * Math.sin(5.065153009));
		int EndXa= (int) ((int)BegX + getWidth()/2.0f * Math.cos(5.065153009));
		g.drawLine(BegX, BegY, EndXa, EndYa);
		*/
		// aroused
		int EndYar= (int) ((int)BegX + getWidth()/2.0f * Math.sin(5.065153009));
		int EndXar= (int) ((int)BegX + getWidth()/2.0f * Math.cos(5.065153009));
		g.drawLine(BegX, BegY, EndXar, EndYar);
		g.drawString(aroused, (int)(this.getWidth() - unpleasant.length()*4.2f - 90) , 20);
		/*
		// tense
		int EndYt= (int) ((int)BegX + getWidth()/2.0f * Math.sin(4.66431959));
		int EndXt= (int) ((int)BegX + getWidth()/2.0f * Math.cos(4.66431959));
		g.drawLine(BegX, BegY, EndXt, EndYt);
		
		
		// alarmed
		int EndYal= (int) ((int)BegX + getWidth()/2.0f * Math.sin(4.59907887));
		int EndXal= (int) ((int)BegX + getWidth()/2.0f * Math.cos(4.59907887));
		g.drawLine(BegX, BegY, EndXal, EndYal);
		*/
		
		// angry
		int EndYan= (int) ((int)BegX + getWidth()/2.0f * Math.sin(4.548396653));
		int EndXan= (int) ((int)BegX + getWidth()/2.0f * Math.cos(4.548396653));
		g.drawLine(BegX, BegY, EndXan, EndYan);
		g.drawString(angry, this.getWidth()/2 -70, 10);
		
		// afraid
		int EndYaf= (int) ((int)BegX + getWidth()/2.0f * Math.sin(4.213865092));
		int EndXaf= (int) ((int)BegX + getWidth()/2.0f * Math.cos(4.213865092));
		g.drawLine(BegX, BegY, EndXaf, EndYaf);
		g.drawString(afraid, this.getWidth()/2 -150, 40);
		/*
		// annoyed
		int EndYann= (int) ((int)BegX + getWidth()/2.0f * Math.sin(4.035923792));
		int EndXann= (int) ((int)BegX + getWidth()/2.0f * Math.cos(4.035923792));
		g.drawLine(BegX, BegY, EndXann, EndYann);
		
		
		// distress
		int EndYdi= (int) ((int)BegX + getWidth()/2.0f * Math.sin(3.840846031));
		int EndXdi= (int) ((int)BegX + getWidth()/2.0f * Math.cos(3.840846031));
		g.drawLine(BegX, BegY, EndXdi, EndYdi);
		*/
		
		// Frustrated
		int EndYf= (int) ((int)BegX + getWidth()/2.0f * Math.sin(3.797765174));
		int EndXf= (int) ((int)BegX + getWidth()/2.0f * Math.cos(3.797765174));
		g.drawLine(BegX, BegY, EndXf, EndYf);
		g.drawString(frustrated, this.getWidth()/2 -240, 110);
		
		// miserable
		int EndYm= (int) ((int)BegX + getWidth()/2.0f * Math.sin(2.989316851));
		int EndXm= (int) ((int)BegX + getWidth()/2.0f * Math.cos(2.989316851));
		g.drawLine(BegX, BegY, EndXm, EndYm);
		g.drawString(miserable, -1, this.getHeight()/2 + 50);


		// sad
		int EndYs= (int) ((int)BegX + getWidth()/2.0f * Math.sin(2.8740287));
		int EndXs= (int) ((int)BegX + getWidth()/2.0f * Math.cos(2.8740287));
		g.drawLine(BegX, BegY, EndXs, EndYs);
		g.drawString(sad, 0, this.getHeight()/2 + 80);

		/*
		// gloomy
		int EndYg= (int) ((int)BegX + getWidth()/2.0f * Math.sin(2.645457885));
		int EndXg= (int) ((int)BegX + getWidth()/2.0f * Math.cos(2.645457885));
		g.drawLine(BegX, BegY, EndXg, EndYg);
		*/
		// depressed
		int EndYde= (int) ((int)BegX + getWidth()/2.0f * Math.sin(2.62443009));
		int EndXde= (int) ((int)BegX + getWidth()/2.0f * Math.cos(2.62443009));
		g.drawLine(BegX, BegY, EndXde, EndYde);
		g.drawString(depressed, 0, this.getHeight()/2 + 130);

		
		// bored
		int EndYb= (int) ((int)BegX + getWidth()/2.0f * Math.sin(2.085846068));
		int EndXb= (int) ((int)BegX + getWidth()/2.0f * Math.cos(2.085846068));
		g.drawLine(BegX, BegY, EndXb, EndYb);
		g.drawString(bored, this.getWidth()/2 - 130, this.getHeight()-20);

		/*
		// droopy
		int EndYdr= (int) ((int)BegX + getWidth()/2.0f * Math.sin(1.804335587));
		int EndXdr= (int) ((int)BegX + getWidth()/2.0f * Math.cos(1.804335587));
		g.drawLine(BegX, BegY, EndXdr, EndYdr);
		
		// tired
		int EndYti= (int) ((int)BegX + getWidth()/2.0f * Math.sin(1.619748542));
		int EndXti= (int) ((int)BegX + getWidth()/2.0f * Math.cos(1.619748542));
		g.drawLine(BegX, BegY, EndXti, EndYti);
		
		// sleepy
		int EndYsl= (int) ((int)BegX + getWidth()/2.0f * Math.sin(1.538530871));
		int EndXsl= (int) ((int)BegX + getWidth()/2.0f * Math.cos(1.538530871));
		g.drawLine(BegX, BegY, EndXsl, EndYsl);
		
		// calm
		int EndYc= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.764567843));
		int EndXc= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.764567843));
		g.drawLine(BegX, BegY, EndXc, EndYc);
		*/
		/*
		// relaxed
		int EndYr= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.742652617));
		int EndXr= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.742652617));
		g.drawLine(BegX, BegY, EndXr, EndYr);
		
		// satisfied
		int EndYsa= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.69425244));
		int EndXsa= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.69425244));
		g.drawLine(BegX, BegY, EndXsa, EndYsa);
		*/
		// atease
		int EndYat= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.700062711));
		int EndXat= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.700062711));
		g.drawLine(BegX, BegY, EndXat, EndYat);
		g.drawString(atease, this.getWidth()/2 + 200, this.getHeight()-80);

		/*
		// content
		int EndYco= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.628818138));
		int EndXco= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.628818138));
		g.drawLine(BegX, BegY, EndXco, EndYco);
		
		// serene
		int EndYse= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.54821456));
		int EndXse= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.54821456));
		g.drawLine(BegX, BegY, EndXse, EndYse);
		*/
		
		/*
		// glad
		int EndYgl= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.1785322));
		int EndXgl= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.1785322));
		g.drawLine(BegX, BegY, EndXgl, EndYgl);
		*/
		// pleased
		int EndYp= (int) ((int)BegX + getWidth()/2.0f * Math.sin(0.119183115));
		int EndXp= (int) ((int)BegX + getWidth()/2.0f * Math.cos(0.119183115));
		g.drawLine(BegX, BegY, EndXp, EndYp);
		g.drawString(pleased, (int) (this.getWidth()- unpleasant.length()*4.2f+20), this.getHeight()/2 +40);

		
//______________________________________________________
		/* Line for user selection of angle and intensity*/
		g.setColor(Color.black);
		g2.setStroke(new BasicStroke(2));
		g.fillOval(pointX-2, pointY-2, 4, 4);
		g.drawLine(this.getWidth()/2, this.getHeight()/2, pointX, pointY);
		setLabel(getAngle(),getIntensity());
		g.drawString(label, pointX-20, pointY-20);

	}
	public void setLabel(float degree, float intense){
		//System.out.println("label: " + degree+" , "+intense);
		String d = getTwoDecimals(degree);
		String i = getTwoDecimals(intense);
		label = new String(d+ " , " + i);
	}
	
	/** Calls movePoint() method to the point where mouse is clicked
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		movePoint(e.getX(), e.getY(), true);
		this.repaint();
	}

	/** Calls movePoint() method to point when mouse is dragged and not clicked
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		movePoint(e.getX(), e.getY(), false);
		this.repaint();
	}
	
	/**Draw line to point where mouse is clicked
	 * @param cpx 			x-coordinate of clicked point
	 * @param cpy			y-coordinate of clicked point
	 * @param isClick		true or false value for whether mouse is clicked
	 */
	private void movePoint(int cpx, int cpy, boolean isClick){
		
		float radAngle = (float) Math.atan(((this.getHeight()/2.0f)-cpy)/(float)((this.getWidth()/2.0f)-cpx));
		
//		float hLimX = this.getWidth()/2 + (float) ((this.getWidth()/2) * Math.cos(radAngle));
//		float hLimY = this.getHeight()/2 + (float) ((this.getHeight()/2) * Math.sin(radAngle));
//		float lLimX = (float) ((this.getHeight()/2) * Math.cos(radAngle));
//		float lLimY = (float) ((this.getHeight()/2) * Math.sin(radAngle));
//		if(cpx > hLimX){ cpx = (int) hLimX; } else if(cpx < lLimX){ cpx = (int) lLimX; }
//		if(cpy > hLimY){ cpy = (int) hLimY; } else if(cpy < lLimY){ cpy = (int) lLimY; }
		
		float d = (float) Math.sqrt(Math.pow((this.getWidth()/2.0f-cpx), 2) + Math.pow((this.getHeight()/2.0f-cpy), 2));
		
		//System.out.println(d);
		
		if(d > this.getWidth()/2.0f){
			if(this.getWidth()/2.0f - cpx > 0){
				cpy = (int)Math.round( this.getHeight()/2.0f - (this.getHeight()/2.0f) * Math.sin( radAngle ) );
				cpx = (int)Math.round( this.getWidth()/2.0f - (this.getWidth()/2.0f) * Math.cos( radAngle ) );
			} else {
				cpy = (int)Math.round( this.getHeight()/2.0f + (this.getHeight()/2.0f) * Math.sin( radAngle ) );
				cpx = (int)Math.round( this.getWidth()/2.0f + (this.getWidth()/2.0f) * Math.cos( radAngle ) );
			}
		}
		
		float dr = (float) (Math.abs((this.getWidth()/2.0f - pointX)*(pointY - cpy) - (pointX - cpx)*(this.getWidth()/2.0f - pointY)) / Math.sqrt(Math.pow((this.getWidth()/2.0f - pointX), 2) + Math.pow((this.getHeight()/2.0f - pointY), 2)));
		
		if(dr < 10 && isClick){
			
			float l = d/(this.getWidth()/2.0f);
			
			if(this.getWidth()/2.0f - cpx > 0){
				cpy = (int)Math.round( this.getHeight()/2.0f - l*(this.getHeight()/2.0f) * Math.sin( lastRadAngle ) );
				cpx = (int)Math.round( this.getWidth()/2.0f - l*(this.getWidth()/2.0f) * Math.cos( lastRadAngle ) );
			} else {
				cpy = (int)Math.round( this.getHeight()/2.0f + l*(this.getHeight()/2.0f) * Math.sin( lastRadAngle ) );
				cpx = (int)Math.round( this.getWidth()/2.0f + l*(this.getWidth()/2.0f) * Math.cos( lastRadAngle ) );
			}
			
		} else {
			lastRadAngle = radAngle;
		}

		pointX = cpx;
		pointY = cpy;

		length = Math.round((float) (Math.sqrt(Math.pow((this.getWidth()/2.0f-pointX), 2) + Math.pow((this.getHeight()/2.0f-pointY), 2))/(this.getWidth()/2.0f))*100.0f)/100.0f;
		
		if((this.getHeight()/2.0f - cpy) < 0){
			if((this.getWidth()/2.0f - cpx) < 0){
				angle = (float) Math.toDegrees(radAngle);
			} else {
				angle = 90 + (float) (90 + Math.toDegrees(radAngle));
			}
		} else {
			if((this.getWidth()/2.0f - cpx) < 0){
				angle = 270 + (float) (90 + Math.toDegrees(radAngle));
			} else {
				angle = 180 + (float) (Math.toDegrees(radAngle));
			}
		}

		//System.out.println("Data: alpha=" + angle + " lenght=" + length);
	
	}

	@Override
	public void mouseMoved(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}


	
	@Override
	public void setPreferredSize(Dimension preferredSize) {
		
		preferredSize.setSize(preferredSize.getHeight(), preferredSize.getHeight());
		radius = (int) preferredSize.getWidth();
		pointX = (int) (radius/2.0f);
		pointY = (int) (radius/2.0f);
		angle = 0;
		super.setSize(preferredSize);
		super.setPreferredSize(preferredSize);
		super.setMaximumSize(this.getPreferredSize());
		super.setMinimumSize(this.getPreferredSize());
	}
	
	/**Retrieve the chosen angle 
	 * 
	 * @return angle (0 to 360 degrees) - A float data type
	 * */
	public float getAngle() {
		return angle;
	}
	
	/**Retrieve the chosen intensity
	 * 
	 * @return lenght of line - A float data type
	 * */
	public float getIntensity(){
		return length;
	}
	
	/**Retrieve the clicked point
	*
	 * @return an array of pointX and pointY - a float data type array
	 */
	public float[] getTouchpoint(){
		
		return new float[]{pointX, pointY};
	}
	
	public float getPointX(){
		return pointX;
	}
	
	public float getPointY(){
		return pointY;
	}


	/**Set the angle, intensity with corresponding x and y touchpoint*/
	/**
	 * @param data - A float array data type of values corresponding to sentence
	 */
	public void setData(float[] data) {
		
		if(data[2] == 0 || data[3] == 0){
			this.movePoint(this.getWidth()/2, this.getHeight()/2, false);
		
		}else {
			this.movePoint((int)data[2], (int)data[3], false);
		}
		this.repaint();
	}
	
	/**
     * Gets a string representation of a double with
     * the first two decimal digits.
     * @param d Double number.
     * @return String presentation with two decimals.
     */
    public static String getTwoDecimals(float d) {
            DecimalFormat df = new DecimalFormat("#.##");
            String formatted = df.format(d);
            //if(formatted.indexOf(",") == -1)
              //     formatted += ",00";
            return formatted.replace(',', '.');
    }
	
}
