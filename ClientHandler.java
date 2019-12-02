import java.util.*;
import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
	private static User[] userList;
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private User client;
	private String[] swearlist;
	private String[] wordlist;

	public static void setUserList(User[] list){
		userList = list;
	}

	public static void removeUser(User u){

		for(int i = 0; i < userList.length; i++){

			if(userList[i] == u){
				userList[i] = null;
			}

		}

	}
	
	public ClientHandler(User client, String[] swears, String[] words){
		this.client = client;
		this.swearlist = swears;
		this.wordlist = words;
		clientSocket = client.getSocket();
	}
	
	public void stopConnection() throws Exception{
        in.close();
        out.close();
		clientSocket.close();
		ClientHandler.removeUser(this.client);
	}

	private Boolean validUsername(String s){
		boolean[] checks = new boolean[4];
		checks[0] = checkWordLength(s,20);
		checks[1] = checkSwears(s);
		checks[2] = checkCharRange(s, 32, 126);
		checks[3] = checkDuplicate(s); //Checks duplicate usernames

		boolean result = true;

		for (int i = 0; i < checks.length; i++) {
			if (!checks[i]) {
				result = false;
				//returns false if any checks fail, true in any other situation
			}
		}

		return result;
	}

	private Boolean checkDuplicate(String s){
		String name;

		for(int i = 0; i < userList.length; i++){

			if(userList[i] != null){
				name = userList[i].getUsername();

				if(s.equals(name)){
					return false; // Inverted this because checks should show if pass (no dup) or fail (dup)
				}

			}

		}

		return true;
	}

	private boolean checkCharRange(String checkString, int minChar, int maxChar) {
		char currentChar;
		//Checks if a string has all characters in a certain range
        for(int i = 0; i < checkString.length(); i++){
            currentChar = checkString.charAt(i);

            if(currentChar < minChar || currentChar > maxChar){
                return false;
            }

		}
		return true;
	}

	private String randomName(){
		Random r = new Random();
		String newName = "";
		int randomint = 5;
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz 1234567890!@#$%^&*()~=-+_{}[]|<>,./?;:";
		int length = alphabet.length();

		while(!validUsername(newName)){
			newName = "";
			randomint = r.nextInt(21); //I fixed the random username code


			for(int i = 0; i < randomint; i++){
				newName += alphabet.charAt(r.nextInt(length));
			}

		}

		return newName;
	}

    private String swearFilter(String string) {
        int arrayindex = 0;
        String tempstring = "";
        String tempstring2 = "";
        LinkedList<String> swearscontained = new LinkedList<String>();
        int startindex = 0;
        int endindex = 0;

        Random random = new Random();

        ArrayList<LinkedList<Object>> result = new ArrayList<LinkedList<Object>>(3);
        String resultstring = "";
        // Index 1: Each character of the string as it slowly gets reduced
        // Index 2: The sourcemap of the modified string to the original
        // Index 3: If the specified part of the sourcemap triggered a swear alert
        // This uses a lot of down and up casting

        LinkedList<Object> modifiables = new LinkedList<Object>();
        LinkedList<Object> sourcemap = new LinkedList<Object>();
        LinkedList<Object> swears = new LinkedList<Object>();

        result.add(modifiables);
        result.add(sourcemap);
        result.add(swears);

        // IMPORTANT - must load sourcemap before converting to lowercase
        char[] stringaschars = string.toCharArray();
        for (int i = 0; i < stringaschars.length; i++) {
            sourcemap.add(Character.toString(stringaschars[i]));
        }

        // Pre-processing
        string = string.toLowerCase();

        stringaschars = string.toCharArray();

        for (int i = 0; i < stringaschars.length; i++) {
            modifiables.add(Character.toString(stringaschars[i]));
        }

        for (int i = 0; i < stringaschars.length; i++) {
            swears.add(false);
            // True if being used in the swear word processor (just for semantics)
        }

        // Process the string and change numbers to letters, or any other substitutions
        // 0:O 1:I 2:Z 3:e 4:A 5:S 6:G 7:T 8:b 9:g 
        for (int i = 0; i < modifiables.size(); i++) {
            switch((String) modifiables.get(i)) {
                case "0":
                    modifiables.set(i,"o");
                    break;
                case "1":
                    modifiables.set(i,"i");
                    break;
                case "2":
                    modifiables.set(i,"z");
                    break;
                case "3":
                    modifiables.set(i,"e");
                    break;
                case "4":
                    modifiables.set(i,"a");
                    break;
                case "5":
                    modifiables.set(i,"s");
                    break;
                case "6":
                    modifiables.set(i,"g");
                    break;
                case "7":
                    modifiables.set(i,"t");
                    break;
                case "8":
                    modifiables.set(i,"b");
                    break;
                case "9":
                    modifiables.set(i,"g");
                    break;
            }
        }

        //Process the string and remove any duplicate characters
        while (arrayindex < modifiables.size() - 1) {
            // If same character found, consume all characters
            if (((String) modifiables.get(arrayindex)).charAt(0) == ((String) modifiables.get(arrayindex + 1)).charAt(0)) {

                
                while (((String) modifiables.get(arrayindex)).charAt(0) == ((String) modifiables.get(arrayindex + 1)).charAt(0)) {
                    merge(result, arrayindex, arrayindex + 1);
                    if (arrayindex + 2 > modifiables.size()) {
                        break; //I'm sorry Mr. Tinling
                    }
                }


                modifiables.set(arrayindex,((String) modifiables.get(arrayindex)).substring(0,1));
            }
            arrayindex++;
        }


        // Process the string and remove any non-letters (merge them using algorithm in the sourcemap)
        // set the non-letters modifiable map to "", but maintain their sourcemap
        for (int i = 0; i < modifiables.size(); i++) {
            if (!(96 < (int) ((String) modifiables.get(i)).charAt(0) && (int) ((String) modifiables.get(i)).charAt(0) < 123)) {
                modifiables.set(i,"");
            }
            //Get the modifiable as a character and see if it is a letter
        }        

        
        // Process swears (only for filter)
        for (int i = 0; i < modifiables.size(); i++) {
            tempstring += (String) modifiables.get(i);
        }
        for (int i = 0; i < swearlist.length; i++) {
            if (tempstring.contains(swearlist[i])) {
                swearscontained.add(swearlist[i]);
            }
        }
        tempstring = "";
        for (int i = 0; i < modifiables.size(); i++) {
            tempstring += (String) modifiables.get(i);
            if (!((String) modifiables.get(i)).equals("")) {
                for (int j = 0; j < swearscontained.size(); j++) {
                    if (tempstring.endsWith(swearscontained.get(j))) {
                        tempstring2 = "";
                        endindex = i + 1;
                        startindex = endindex;
                        while (!(swearscontained.get(j).equals(tempstring2))) {
                            startindex--;
                            tempstring2 = (String) modifiables.get(startindex) + tempstring2;
                        }
                        for (int k = startindex; k < endindex; k++) {
                            swears.set(k, true);
                        }
                    }
                }
            }
        }

        // Replace swears (only for filter)
        arrayindex = 0;
        while (arrayindex < swears.size() - 1) {
            // If same character found, consume all characters
            if ((Boolean) swears.get(arrayindex) && (Boolean) swears.get(arrayindex + 1)) {

                
                while  ((Boolean) swears.get(arrayindex) && (Boolean) swears.get(arrayindex + 1)){
                    merge(result, arrayindex, arrayindex + 1);
                    if (arrayindex + 2 > swears.size()) {
                        break; //I'm sorry Mr. Tinling
                    }
                }


                modifiables.set(arrayindex,((String) modifiables.get(arrayindex)).substring(0,1));
            }
            arrayindex++;
        }

        for (int i = 0; i < swears.size(); i++) {
            if ((Boolean) swears.get(i)) {
                sourcemap.set(i, wordlist[random.nextInt(wordlist.length)]);
            }
        }

        tempstring = "";
        for (int i = 0; i < sourcemap.size(); i++) {
            tempstring += (String) sourcemap.get(i);
        }

        return tempstring;
    }

    private void merge(ArrayList<LinkedList<Object>> result, int index1, int index2) {
        // Most of the time these should be sequential but who knows

        // Merge modifiable entry from index2 to index1 (append)
        result.get(0).set(index1, (String) result.get(0).get(index1) + (String) result.get(0).get(index2));
        // Merge sourcemap entry from index2 to index1 (append)
        result.get(1).set(index1, (String) result.get(1).get(index1) + (String) result.get(1).get(index2));
        // Merge swear entry using or (if either is true, make both true)
        if ((Boolean) result.get(2).get(index2)) {
            result.get(2).set(index1, true);
        }
        // Delete index2
        for (int i = 0; i < 3; i++) {
            result.get(i).remove(index2);
        }
    }
	
	private Boolean validMessage(String s){
		//Running through all checks
		boolean[] checks = new boolean[3]; //Number of checks = 3
		checks[0] = checkMessageTime(s, client);
		checks[1] = checkWordLength(s, 200);
		checks[2] = checkSwears(s);

		boolean result = true;

		for (int i = 0; i < checks.length; i++) {
			if (!checks[i]) {
				result = false;
				//returns false if any checks fail, true in any other situation
			}
		}

		return result;
	}

	private static boolean checkMessageTime(String checkString, User user) {

		if ( new Date().getTime() - user.getTime() > 500) {
			//Prevents more than 3 messages per second
			//To change this modify 500 to 1000/desired messages per second
			return true;
		}
		else {
			return false;
		}
	}

	private static boolean checkWordLength (String checkString, int maxlength) {
		if(checkString.length() > 0 && checkString.length() < maxlength){
			return true;
		}
		else {
			return false;
		}
	}

	private static boolean checkSwears(String checkString) {
		String[] forbidden = {"fuck","shit","bitch","ass"}; //Feel free to add more

		for (int i = 0; i < forbidden.length; i++) {
			if (checkString.contains(forbidden[i])) {
				return false;
			}
		}
		return true;
	}

	private void sendToAll(String s){

		for(int i = 0; i < userList.length; i++){

			if(userList[i] != null && userList[i] != this.client){
				userList[i].sendMessage(s);
			}

		}

	}

	public void run(){

		try{
			System.out.println("Connected with " + clientSocket.getRemoteSocketAddress().toString());
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			String inputLine = "";

			while(inputLine.length() == 0 && clientSocket.isConnected() && !clientSocket.isClosed()){
				inputLine = in.readLine();
			}

			System.out.println("Username input: " + inputLine);

			inputLine = swearFilter(inputLine);

			if(validUsername(inputLine)){
				client.setUsername(inputLine);
				client.sendMessage("Username accepted. Start chatting!");
				System.out.println("Assigned valid name");
			} else{
				client.setUsername(randomName());
				client.sendMessage("Username not accepted. Random username " + client.getUsername() + " assigned. Start chatting!");
				System.out.println("Assigned random name");
			}

			System.out.println("Client now speaking");
			
			while (clientSocket.isConnected() && !clientSocket.isClosed() && (inputLine = in.readLine()) != null) {
				
				if ("exit".equals(inputLine)) {
					out.println("Goodbye");
					break;
				}

				inputLine = swearFilter(inputLine);

				if(validMessage(inputLine)){
					client.sent();
					sendToAll(client.getUsername() + ": " + inputLine);
				} else{
					out.println("Message not delivered");
					System.out.println("Message not delivered");
				}

				System.out.println(client.getUsername() + ": " + inputLine);
			}

		} catch(Exception E){
			E.printStackTrace();
		} finally{

			if(clientSocket != null){
				System.out.println("Disconnecting with " + clientSocket.getRemoteSocketAddress().toString());

				try{
					stopConnection();
				} catch(Exception E){

				}

				System.out.println("Disconnected");
			}

		}

	}

}