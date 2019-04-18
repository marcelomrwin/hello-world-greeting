package hello;

import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class GreetingMessageTest {

   @Test
   public void testGoodMorning() {
      GreetingMessage var = new GreetingMessage(0);
	    String message = "Good Morning!";
      assertEquals(message,var.printMessage());

      var = new GreetingMessage(12);
      message = "Good Noon!";
      assertEquals(message,var.printMessage());

      var = new GreetingMessage(20);
      message = "Good Evening!";
      assertEquals(message,var.printMessage());
   }

   @Test
   public void testGoodAfternon(){
     GreetingMessage var = new GreetingMessage(16);
     String message = "Good Afternoon!";
     assertEquals(message,var.printMessage());
   }
}
