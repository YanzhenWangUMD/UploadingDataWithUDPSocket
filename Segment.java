import java.io.Serializable;

public class Segment implements Serializable {
private static final long serialVersionUID = 1L;// create an ID for interface purpose 
	
	int seqacknum;
	byte[] data;
	
	
	public Segment (int a, byte[] b)
	{
		this.seqacknum = a;
		this.data = b;
	}
	
	public Segment()
	{
		this(0, new byte[1024]);
		
	}
	
	public int getseqacknum()
	{
		return seqacknum;
	}
	
	public byte[] getData()
	{
		return data;
	}
	public void setseqacknum(int s)
	{
		this.seqacknum =s;
	} 
	public void setData(byte[] d)
	{
		this.data = d;
	}

	public Object getBytes() {
		// TODO Auto-generated method stub
		return null;
	}
	

}



