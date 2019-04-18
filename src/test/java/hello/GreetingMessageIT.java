package hello;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class GreetingMessageIT {

   @Test
   public void testPrintMessage() {
      GreetingMessage var = new GreetingMessage();
	 String message;
   	GregorianCalendar time = new GregorianCalendar();
 	int hour = time.get(Calendar.HOUR_OF_DAY);
	if (hour < 12)
    message = "Good Morning!";
    else if (hour < 17 && !(hour == 12))
    message = "Good Afternoon!";
    else if (hour == 12)
    message = "Good Noon!";
    else
    message = "Good Evening!";
   	
    assertEquals(message,var.printMessage());

   }
}