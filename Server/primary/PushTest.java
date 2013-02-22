package primary; 

import java.io.File;
import java.util.List;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;

public class PushTest {

	public static void main(String[] args) {

		try {
			List<PushedNotification> notifications = 
					Push.alert("Hello World!", "/home/accts/ss2372/Desktop/CS434/FP/Apple/iphone_dev.p12", "partybeats123", false, "c28d3682eff73e0d6bd112c26d31758415cabdef00d9ac5ae6f5c063bfe4b86f");
			
            for (PushedNotification notification : notifications) {
                if (notification.isSuccessful()) {
                        /* Apple accepted the notification and should deliver it */  
                        System.out.println("Push notification sent successfully to: " +
                                                        notification.getDevice().getToken());
                        /* Still need to query the Feedback Service regularly */  
                } else {
                        String invalidToken = notification.getDevice().getToken();
                        /* Add code here to remove invalidToken from your database */  

                        /* Find out more about what the problem was */  
                        Exception theProblem = notification.getException();
                        theProblem.printStackTrace();

                        /* If the problem was an error-response packet returned by Apple, get it */  
                        ResponsePacket theErrorResponse = notification.getResponse();
                        if (theErrorResponse != null) {
                                System.out.println(theErrorResponse.getMessage());
                        }
                }
        }
			
			System.out.println("done sending");
			
		} catch (CommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeystoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}