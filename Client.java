import java.net.*;
import java.util.ArrayList;
import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class Client {
	private Socket socket;
	private OutputStream outToServer;
	private DataOutputStream out;
	private InputStream inFromServer;
	private DataInputStream in;
	
	private static final String HELO =  "HELO";
	private static final String AUTH =  "AUTH comp335";
	private static final String QUIT = "QUIT";
	private static final String REDY = "REDY";
	private static final String NONE = "NONE";
	private static final String ERR = "ERR: No such waiting job exists";
	
	public Client(String address, int port) {
		try {
			System.out.println("Attempting connection with " + address + " at port " + port);
			socket = new Socket(address,port);
			System.out.println("Connected");
			
			//new message to Server
			writeMSG(socket, HELO);
			
			//receive message from Server
			readMSG(socket);
			
			//second message to server
			writeMSG(socket, AUTH);
			
			//parse system.xml
			File file = new File("system.xml");
			String ans = parse(file);
			System.out.println(ans);
			
			//second message from server
			readMSG(socket);
			
			//third message to server
			writeMSG(socket,REDY);
			
			//third message from server
			readMSG(socket);
			
			int i = 0;
			while(true) {

				//job message to server
				writeMSG(socket,"SCHD " + i + " " + ans + " 0");
				
				//get response
				String response = readMSG(socket);
				if(response.contains(NONE) || response.contains(ERR)) {
					break;
				}
				
				//send REDY
				writeMSG(socket, REDY);
				
				String error = readMSG(socket);
				
				if(error.contains(NONE) || response.contains(ERR)) {
					break;
				}
				i++;
			}
			
			//LAST STAGE: QUIT
			writeMSG(socket, QUIT);
			readMSG(socket);
			
		}  catch (UnknownHostException u) {
			System.out.println(u);
		}
		catch (IOException e) {
			System.out.println(e);
		}
		//close all the client communications and the socket
		try {
			inFromServer.close();
			outToServer.close();
			in.close();
			out.close();
			socket.close();
		}  catch (IOException i) {
			System.out.println(i);
		}
	}
	
	/*
	 * Get the strings to send
	 * get them in the right format to send
	 * and send them to client
	 */
	private void writeMSG(Socket socket, String msg) throws IOException {
		outToServer = socket.getOutputStream();
		out = new DataOutputStream(outToServer);
		
		out.write(msg.getBytes());
		System.out.println("messge sent to server: " + msg);
		out.flush();
	}
	/*
	 * get message from server
	 * print out that message has been received
	 * use message in Client method
	 */
	private String readMSG(Socket socket) throws IOException {
		inFromServer = socket.getInputStream();
		in = new DataInputStream(inFromServer);
		
		byte[] rMSG = new byte[1024];
		in.read(rMSG);
		
		String str = new String(rMSG);
		System.out.println("message received from server: "  + str);
		return str;
	}
	
	/*
	 * get information out of file and return
	 * the smallest server (the one with the smallest number of cores)
	 */
	private String parse(File file) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			ArrayList<String> str = new ArrayList<String>();
			ArrayList<Integer> list = new ArrayList<Integer>();
			
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("server");
			
			if(doc.hasChildNodes()) {
				for(int i = 0; i < nList.getLength(); i++) {
					Node node = nList.item(i);
					
					if(node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						
						str.add(element.getAttribute("type"));
						list.add(Integer.parseInt(element.getAttribute("coreCount")));
					}
				}
			}
			
			int smallest = Integer.MAX_VALUE;
			
			for(int i = 1; i<str.size(); i++) {
				if(list.get(i) < list.get(smallest)) {
					smallest  = i;
				}
			}
			
			String ans = new String();
			ans =  str.get(smallest);
			
			return ans;
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
		return "Did not work";
	}
	
	/*
	 * initialize connection
	 */
	public static void main(String[] args) {
		Client client = new Client("127.0.0.1", 50000);
	}

}