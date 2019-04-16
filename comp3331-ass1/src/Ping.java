import java.io.*;
import java.net.*;

class Ping {
	public static boolean send(Peer curr, int s1) throws IOException {
		boolean get = false;
		//System.out.println("Send to " + s1);
		int s1Port = s1 + 50000;
		DatagramSocket client = null;
		try{
			client = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		InetAddress geek = InetAddress.getByName("127.0.0.1");
		if(client != null) {
			client.setSoTimeout(10000);
			String mess = "Ping" + " " + curr.getId() + " " + curr.getS1();
		//	System.out.println(mess);
			byte[] buf = mess.getBytes();
			DatagramPacket ping = new DatagramPacket(buf,buf.length,geek,s1Port);
			client.send(ping);
			
			buf = new byte[2048];
			DatagramPacket received =  new DatagramPacket(buf,buf.length);
			try{
				client.receive(received);
			
				get = true;
				String[] str = new String(received.getData()).split(" ");
				System.out.println("pong = " + str[1].trim());
				
				System.out.println("A ping response message was received from Peer " + (received.getPort()-50000) + ".");
			}catch(IOException e) {
				
			}
		}
		return get;
	}
	
	public static void received(Peer p) throws Exception{
		DatagramSocket server = null;
		int curr = p.getPort();
	//	System.out.println("this port " + curr);
		try {
			server = new DatagramSocket(curr);
		}catch (SocketException e) {
			e.printStackTrace();
		}
		int sender = 0;
		byte[] buf = new byte[2048];
		DatagramPacket msg = new DatagramPacket(buf,buf.length);
		if(server != null) {
			while(true) {
					server.receive(msg);
					buf = msg.getData();
				//	String ori = new String(msg.getData(),msg.getOffset(),msg.getLength()).trim();
				//	System.out.println(ori);
					String[] split = new String(msg.getData(),msg.getOffset(),msg.getLength()).trim().split(" ");
					//System.out.println(split[0].trim());
					sender = Integer.parseInt(split[1]);
					System.out.println(sender + "'s successor is " + split[2]);
					int successor = Integer.parseInt(split[2]);
					if(successor == p.getId() && p.getP1() != 0 && p.getP2() != 0) {
						if(p.getP1() != sender) {
							p.setP2(p.getP1());		
							p.setP1(sender);
						}
					}
					System.out.println("A ping request message was received from Peer " + sender + ".");
					if(p.getP1() == 0 && p.getP2() == 0) {
						p.setP1(sender);
					}else if(p.getP1() != 0 && p.getP2() == 0){
						p.setP2(sender);
					}
					
					String reply = "Success " + p.getPort();
					buf = new byte[2048];
					buf = reply.getBytes();
					DatagramPacket ping = new DatagramPacket(buf,buf.length,msg.getAddress(),msg.getPort());
					try{
						server.send(ping);
					}catch (IOException e) {
						e.printStackTrace();
					}
					sender = -1;
					split = null;
					
			}
		}
	}
}
