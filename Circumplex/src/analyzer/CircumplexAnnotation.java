package analyzer;

/**
 * Represents a circumplex annotation.
 * @author Tuomo Kakkonen
 *
 */
public class CircumplexAnnotation {
	private String annotator;
	private float angle, intensity; 
	
	/**
	 * Creates a new instance of the class.
	 * @param angle Angle.
	 * @param intensity Intensity value.
	 */
	public CircumplexAnnotation(String annotator, float angle, float intensity) {
		this.annotator = annotator;
		this.angle = angle;
		this.intensity = intensity;
	}

	/**
	 * Return the angle.
	 */
	public float getAngle() {
		return angle;
	}
	
	/**
	 * Returns the intensity value.
	 * @return
	 */
	public float getIntensity(){
		return intensity;
	}
	
	public String getAnnotator() {
		return annotator;
	}

	public String toString() {
		return annotator + ": " + angle + ", " + intensity;
	}
	
}
