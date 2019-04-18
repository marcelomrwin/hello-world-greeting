package hello;

public class GreetingMessage {
private int hours=0;
     // constructor
     public GreetingMessage(int hour){
       this.hours = hour;
     }

     // return message
     public String printMessage(){
      String message="";
       if (hours < 12)
        message = "Good Morning!";
       else if (hours < 17 && hours != 12)
        message = "Good Afternoon!";
       else if (hours == 12)
        message = "Good Noon!";
       else
        message = "Good Evening!";

        return message;
     }
  }
