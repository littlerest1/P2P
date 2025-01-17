

class Peer {
	private int id = -1;
	private int p1 = 0;
	private int p2 = 0;
	private int port = -1;
	private int s1 = 0;
	private int s2 = 0;

	public Peer(int id,int s1,int s2) {
		this.id = id;
		this.s1 = s1;
		this.s2 = s2;
		this.port = 50000 + id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setP1(int p1) {
		this.p1 = p1;
	}
	
	public void setP2(int p2) {
		this.p2 = p2;
	}
	
	public int getS1() {
		return this.s1;
	}
	
	public int getS2() {
		return this.s2;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public boolean check(int hash) {
		return false;
	}
	
	public int getP1() {
		return this.p1;
	}
	
	public int getP2() {
		return this.p2;
	}
}
