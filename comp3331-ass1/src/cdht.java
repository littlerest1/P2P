import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class cdht {
	private static int port = 5000;
	private static String ipAddress = "127.0.0.1"; 
	private static DatagramSocket socket = null;
	private static ServerSocket welcomeSocket = null;
	private static boolean Succ1 = false;	
	private static boolean Succ2 = false;
	private static boolean exist1 = false;
	private static boolean exist2 = false;
	private static Peer peer = null;
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final static ScheduledExecutorService scheduler1 = Executors.newScheduledThreadPool(1);
	
	public static void main(String args[]) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: peer_initialised successor_1 successor_2 MSS drop probability");
			return;
		}
		int curr = Integer.parseInt(args[0]);
		int s1 = Integer.parseInt(args[1]);
		int s2 = Integer.parseInt(args[2]);
		int MSS = Integer.parseInt(args[3]);
		peer = new Peer(curr,s1,s2);
	
		
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String[] inputSplit = null;
				while(true) {
					String input = null;
					try {
						input = br.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if(input != null) {
						inputSplit = input.split(" ");
						if(inputSplit[0].equalsIgnoreCase("Request") && !inputSplit[1].isEmpty()) {
							//int filename = Integer.parseInt(inputSplit[1]);
							try {
								TCP.Request(peer,inputSplit[1]);
							}catch (IOException e1){
								e1.printStackTrace();
							}
							
							System.out.println("File request message for " + inputSplit[1]);
						}
					}
				}
			}
			
		})).start();
		
		pingRequest1();
		pingRequest2();
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Ping.received(peer);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		})).start();
		
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					TCP.Receive(peer);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		})).start();
	}
	
	public static void pingRequest1() {
		final Runnable pingSuccessor = new Runnable() {
			@Override
			public void run() {
				try {
				//	System.out.println("peer s1 = " + peer.getS1()  + " current peer = " + peer.getId());
					boolean result = Ping.send(peer, peer.getS1());
					System.out.println(result);
					if(result) {
						exist1 = true;
						System.out.println("my p1 = " + peer.getP1() + " my p2 = " + peer.getP2());
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
/*				try {
					System.out.println("Peer s2 = " + peer.getS2());
					Ping.send(peer,peer.getS2());
				} catch (IOException e) {
					e.printStackTrace();
				}*/
			}
		};
		scheduler.scheduleAtFixedRate(pingSuccessor,0L,15L,TimeUnit.SECONDS);
	}
	
	public static void pingRequest2() {
		final Runnable pingSuccessor1 = new Runnable() {
			@Override
			public void run() {
				try {
				//	System.out.println("Peer s2 = " + peer.getS2() +  " current peer = " + peer.getId());
					boolean result = Ping.send(peer,peer.getS2());
					System.out.println(result);
					if(result) {
						exist2 = true;
						System.out.println("my p1 = " + peer.getP1() + " my p2 = " + peer.getP2());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		scheduler1.scheduleAtFixedRate(pingSuccessor1,0L,15L,TimeUnit.SECONDS);
	}
	
	private static int check(String filname) {
		int digit = Integer.parseInt(filname);
		int remainder = digit%256;
		
		return remainder;
	}

	
	public static void Request(ServerSocket welcomeSocket,int successor,String filename) throws Exception{
		 Socket TCPsocket = new Socket(ipAddress,port+successor);
		 DataOutputStream out = new DataOutputStream(TCPsocket.getOutputStream());
		 out.writeUTF("Request " + filename + " from " + welcomeSocket.getLocalPort());
		 out.close();
		 TCPsocket.close();
	}
	
	public static void Ping(int dest) throws UnknownHostException, IOException {
		InetAddress geek = InetAddress.getByName(ipAddress); 
		byte[] buf = "Ping".getBytes();
		DatagramPacket ping = new DatagramPacket(buf, buf.length, geek, port+dest);
		socket.send(ping);
	}
}
