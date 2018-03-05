package ssrunner;

public class SAProfile {
	private int id, pos, neg;
	
	public SAProfile(int id, int pos, int neg) {
		this.id = id;
		this.pos = pos;
		this.neg = neg;
	}
	
	public int getPos() {
		return pos;
	}
	
	public int getNeg() {
		return neg;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
