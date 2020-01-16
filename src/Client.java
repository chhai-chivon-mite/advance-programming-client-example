import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Scanner;

/*
 * @author: Chhai Chivon on Jan 15, 2020
 * Senior Application Developer
 */

public class Client {

	public static void main(String[] agrs) {
		System.out.println("===Exchange Currency Client===");
		
		try {
			final Socket connection = new Socket("localhost", 1234);
			System.out.println("Connected");
            System.out.println("Send request to server.");
            final Writer streamWriter = new OutputStreamWriter(connection.getOutputStream());
            final Scanner streamReader = new Scanner(connection.getInputStream());
            final Scanner keyboardReader = new Scanner(System.in);
            System.out.println("*******************************");
            System.out.println("    Exchange Currency Client   ");
            System.out.println("*******************************");
            System.out.println("Please select an action:");
            System.out.println("  1, List supported currencies");
            System.out.println("  2, Convert currencies");
            while (true) {
                System.out.print("Enter number: ");
                final String actionNumber = keyboardReader.nextLine();
                if (actionNumber.equals("1")) {
                    requestCurrencyList(streamWriter, streamReader);
                    break;
                }
                if (actionNumber.equals("2")) {
                    processConvertRequest(streamWriter, streamReader, keyboardReader);
                    break;
                }
                System.out.println("Invalid input.");
            }
            keyboardReader.close();
            connection.close();
            System.out.println("Done!");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private static void requestCurrencyList(final Writer streamWriter, final Scanner streamReader) {
        try {
            streamWriter.write("=>list<=#\n");
            streamWriter.flush();
            final String response = streamReader.nextLine();
            final String[] parts = response.split("#");
            if (parts.length != 2) {
                System.out.println("Unexpected error.");
                return;
            }
            final String status = parts[0];
            if (status.equals("=>ok<=")) {
                processListResponse(parts[1]);
            }
            else if (status.equals("=>invalid<=") || status.equals("=>error<=") || status.equals("=>unknown<=")) {
                processNonOkResponse(parts[1]);
            }
            else {
                System.out.println("Unexpected error.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void processConvertRequest(final Writer streamWriter, final Scanner streamReader, final Scanner keyboardReader) {
        System.out.print("Please enter source currency: ");
        final String sourceCurrency = keyboardReader.nextLine();
        System.out.print("Please enter amount: ");
        final String amount = keyboardReader.nextLine();
        System.out.print("Please enter destination currency: ");
        final String destinationCurrency = keyboardReader.nextLine();
        final String request = String.format("=>convert<=#=>%s<==>%s<==>%s<=\n", sourceCurrency, amount, destinationCurrency);
        try {
            streamWriter.write(request);
            streamWriter.flush();
            final String response = streamReader.nextLine();
            final String[] parts = response.split("#");
            if (parts.length != 2) {
                System.out.println("Unexpected error.");
                return;
            }
            final String status = parts[0];
            if (status.equals("=>ok<=")) {
                processConvertResponse(parts[1], destinationCurrency);
            }
            else if (status.equals("=>invalid<=") || status.equals("=>error<=") || status.equals("=>unknown<=")) {
                processNonOkResponse(parts[1]);
            }
            else {
                System.out.println("Unexpected error.");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void processListResponse(final String data) {
        final String[] dataValues = data.split("=>");
        for (int i = 1; i < dataValues.length; ++i) {
            final String value = dataValues[i].replace("<=", "").toUpperCase();
            System.out.println(" - " + value);
        }
    }
    
    private static void processNonOkResponse(final String data) {
        final String message = data.replace("=>", "").replace("<=", "");
        System.out.println(message);
    }
    
    private static void processConvertResponse(final String data, final String destinationCurrency) {
        final String message = data.replace("=>", "").replace("<=", "");
        final double amount = Double.parseDouble(message);
        final String formatedAmount = new DecimalFormat("#,###.00").format(amount);
        final String displayMessage = String.format("Result: %s %s", formatedAmount, destinationCurrency.toUpperCase());
        System.out.println(displayMessage);
    }

}
