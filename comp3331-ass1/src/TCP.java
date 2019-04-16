import java.io.*;
import java.net.*;

class TCP {
	public static void Request(Peer curr, String filename) throws IOException {
		InetAddress server = InetAddress.getByName("127.0.0.1");
		int dest = curr.getS1() + 50000;
		Socket client = null;
		try {
			client = new Socket(server,dest);
		}catch (IOException e) {
			
		}
		if(client != null) {
			String msg = "Request " + filename + " from " + curr.getId();
			DataOutputStream outToServer = new DataOutputStream(client.getOutputStream());
			outToServer.writeBytes(msg + '\n');
			System.out.println("File request message for " + filename + " has been sent to my successor.");
		}
	}
	
	public static void Receive(Peer p,long startTime) throws Exception{
		int server = p.getPort();
		ServerSocket welcomeSocket = new ServerSocket(server);
		InetAddress serverIP = InetAddress.getByName("127.0.0.1");
		
		while(true) {
			Socket client = welcomeSocket.accept();
			
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String msg = inFromClient.readLine();
			String[] Split = msg.split(" ");
			String filename = null;
			int from = -1;
			System.out.println("-----------------------------------Message : " + msg + "--------------------------------------");
			if(Split[0].equalsIgnoreCase("Request")) {
				filename = Split[1];
				int num = Integer.parseInt(Split[1]);
				from = Integer.parseInt(Split[3]);
			//	System.out.println("Find " + filename + " from " + from);
				boolean exist = check(p,num);
				if(exist) {
					System.out.println("File " + filename + " is here");
					String reply = "Found " + filename + " from " + p.getId();
					Socket clientSocket = new Socket(serverIP,from+50000);
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
					outToServer.writeBytes(reply+'\n');
					System.out.println("A response message, destined for peer " + from + ", has been sent");
					Transfer.fileSender(filename,from, p,startTime);
				}
				else {
					System.out.println("File " + filename + " is not stored here");
					Socket clientSocket = new Socket(serverIP,(p.getS1()+50000));
					DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
					outToServer.writeBytes(msg+'\n');
					System.out.println("File request message has been forwarded to my successor.");
				}
			}
			else if(Split[0].equalsIgnoreCase("Found")) {
				filename = Split[1];
				from = Integer.parseInt(Split[3]);
				System.out.println("Received a response message from peer " + from + ", which has the file " + filename);
				Transfer.fileReceiver(filename,from, p,startTime);
			}
		}
	}
	
	private static boolean check(Peer p,int file) {
		int hashNum = hash(file);
		
		if(hashNum == p.getId()) {
			return true;
		}
		//the smallest , hash larger than the largest exist peer
		else if(p.getId() < p.getP1() && p.getId() < p.getS1() && hashNum > p.getP1()) {
			return true;
		}
		//otherwise
		else if(hashNum < p.getId() && hashNum > p.getP1()) {
			return true;
		}
		return false;
	}
	
	private static int hash(int file) {
		int remainder = file%256;
		return remainder;
	}
}
