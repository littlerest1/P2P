import java.io.*;
import java.net.*;

class Transfer {

	public static void fileSender(String filename,int dest,Peer p,long startTime) throws IOException {
		String FILENAME = filename+".pdf";
		int pieces = 0;
		String temp = null;
		int ACKN = 1;
		File file = new File(FILENAME);
		if(!file.exists() || !file.isFile()) {
			System.out.println("File not exist or is not a file");
		}else {
			System.out.println("We now start sending the file .........");
			long size = file.length();

			
			FileOutputStream fos = null;
			File log;
			
			try {
				log = new File("responding_log.txt");
				fos = new FileOutputStream(log);
				if(!log.exists()) {
					log.createNewFile();
				}	
			}catch(IOException e) {
				
			}	
			pieces = getP(size,p.getMSS());			
			byte sdata[] = new byte[p.getMSS()];
			DatagramSocket dsoc = new DatagramSocket();
			InetAddress ip = InetAddress.getByName("127.0.0.1");
			FileInputStream input = new FileInputStream(FILENAME);
			int nRead = 0;
			int fail = 0;
			while((nRead = input.read(sdata)) != -1) {
			//	System.out.println("Pieces left " + pieces);
				dsoc.setSoTimeout(1000);
				DatagramPacket msg = new DatagramPacket(sdata,sdata.length,ip,8080);
				
			
				double random = getRandom();
				//System.out.println("generated rate " + random);
				if(random >= p.getRate()) {
					long sendTime = System.currentTimeMillis();			
					temp = "snd		" +  (sendTime - startTime) + "		" + ACKN + "	" + p.getMSS() + "		0" + "\n";		
					byte buffer[] = temp.getBytes();
					fos.write(buffer);
				//	System.out.println("snd		" +  (sendTime - startTime) + "		" + ACKN + "		" + p.getMSS() + "		0");
					
					dsoc.send(msg);
				}
				
				
				byte[] buf = new byte[2048];
				DatagramPacket received =  new DatagramPacket(buf,buf.length);
				try{
					dsoc.receive(received);
					
					String[] str = new String(received.getData()).trim().split(" ");

					if(str[0].equalsIgnoreCase("ACK")) {
						long receivedTime = System.currentTimeMillis();
						ACKN += p.getMSS();
						temp = "rcv		" + (receivedTime-startTime) + "		0		" +  p.getMSS() + "		" + ACKN + "\n";
						byte buffer[] = temp.getBytes();
						fos.write(buffer);
						//System.out.println("rcv		" + (receivedTime-startTime) + "		0		" +  p.getMSS() + "		" + ACKN);
						
						pieces --;
					}
				}catch(SocketTimeoutException e) {
					fail ++;
					
					long dropTime = System.currentTimeMillis();
					temp = "Drop	"+ (dropTime-startTime)+ "		"+ ACKN + "		"+ p.getMSS() + "		0\n";
					byte buffer[] = temp.getBytes();
					fos.write(buffer);
					//System.out.println("Drop	"+ (dropTime-startTime)+ "		"+ ACKN + "		"+ p.getMSS() + "		0");
					
					boolean reset = true;
					while(reset) {
						DatagramSocket trans = new DatagramSocket();
						trans.setSoTimeout(1000);
						
						random = getRandom();
						//System.out.println("generated rate " + random);
						if(random >= p.getRate()) {
							long reTransTime = System.currentTimeMillis();
							temp = "RTX		" +  (reTransTime-startTime) + "		" + ACKN + "		"+p.getMSS() + "		0\n";
							buffer = temp.getBytes();
							fos.write(buffer);
							//System.out.println("RTX		" +  (reTransTime-startTime) + "		" + ACKN + "		"+p.getMSS() + "		0");
							trans.send(msg);
						}
						buf = new byte[2048];
						DatagramPacket back =  new DatagramPacket(buf,buf.length);
						try {
							
							trans.receive(back);

							String[] str1 = new String(back.getData()).trim().split(" ");

							if(str1[0].equalsIgnoreCase("ACK")) {
								long received1Time = System.currentTimeMillis();
								ACKN += p.getMSS();
								temp = "rcv		" + (received1Time-startTime) + "		0		" +  p.getMSS() + "		" + ACKN + "\n";
								buffer = temp.getBytes();
								fos.write(buffer);
								//System.out.println("rcv		" + (received1Time-startTime) + "		0		" +  p.getMSS() + "		" + ACKN);
								pieces --;
								reset = false;
							}
						}catch(SocketTimeoutException e1) {
							long reDrop = System.currentTimeMillis();
							temp = "RTX/Drop	" +  (reDrop-startTime) + "		" + ACKN + "		"+ p.getMSS() + "		0\n";
							buffer = temp.getBytes();
							fos.write(buffer);
						//	System.out.println("RTX/Drop		" +  (reDrop-startTime) + "		" + ACKN + "		"+ p.getMSS() + "		0");
						}
					}
				}
			}
			String end = "End";
			byte[] endB = end.getBytes();
			DatagramPacket pong = new DatagramPacket(endB,endB.length,ip,8080);
			dsoc.send(pong);
			System.out.println("The file is sent");
			fos.flush();
		}
	}
	
	
	public static void fileReceiver(String filename,int src,Peer p,long startTime) {
		DatagramSocket server = null;
		String temp = null;
		int ACKN = 1;
		try {
			server = new DatagramSocket(8080);
		}catch (SocketException e) {
			e.printStackTrace();
		}
		
		byte[] buf = new byte[p.getMSS()];
		DatagramPacket msg = new DatagramPacket(buf,buf.length);
		FileOutputStream fos = null;
		File file;
		FileOutputStream output = null;
		File logFile;
		
		try {
			logFile = new File("receiver_log.txt");
			output = new FileOutputStream(logFile);
			if(!logFile.exists()) {
				logFile.createNewFile();
			}
		}catch(IOException e) {
			
		}
		
		try {
			file = new File("received_file.pdf");
			fos = new FileOutputStream(file);
			if(!file.exists()) {
				file.createNewFile();
			}		
			if(server != null) {
				System.out.println("We now start receiving the file .........");
				int count = 0;
				while(true) {
					server.receive(msg);
					buf = msg.getData();
					String[] split = new String(msg.getData(),msg.getOffset(),msg.getLength()).trim().split(" ");
					if(split[0].equalsIgnoreCase("End")) {
						break;
					}
					
					long get = System.currentTimeMillis();
					temp = "rcv		"+ (get-startTime) + "		" + ACKN + "		"+ p.getMSS() + "		0\n";
					byte buffer[] = temp.getBytes();
					output.write(buffer);
					
				//	System.out.println("rcv		"+ (get-startTime) + "		" + ACKN + "		"+ p.getMSS() + "		0");
					ACKN += p.getMSS();
				//	System.out.println("Received segment " + count);
					
					fos.write(msg.getData(),msg.getOffset(),msg.getLength());
					String reply = "ACK";
					buf = new byte[2048];
					buf = reply.getBytes();
					DatagramPacket ping = new DatagramPacket(buf,buf.length,msg.getAddress(),msg.getPort());
					try{
						long replyTime = System.currentTimeMillis();
						temp = "snd		"+ (replyTime-startTime) + "		0" + "		"+ p.getMSS() + "		" + ACKN +"\n";
						buffer = temp.getBytes();
						output.write(buffer);
					//	System.out.println("snd		"+ (replyTime-startTime) + "		0" + "		"+ p.getMSS() + "		" + ACKN);
						server.send(ping);
					}catch (IOException e) {
						e.printStackTrace();
					}
					count ++;
				}
			}
			fos.flush();
			output.flush();
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fos != null) {
					fos.close();
				}
				if(output != null) {
					output.close();
				}
				System.out.println("The file is received");
			}catch(IOException e1) {
				System.out.println("Error in closing the stream");
			}
		}
		

			
	}
	
	
	private static int getP(long size,int mss) {
		int remainder = (int) (size/mss);
		if(remainder*mss < size) {
			return remainder + 1;
		}
		return remainder;
	}
	
	private static double getRandom() {
		double x = Math.random();
		return x;
	}
	

}
