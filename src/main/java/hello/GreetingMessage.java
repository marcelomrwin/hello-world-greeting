package hello;

public class GreetingMessage {
private Integer hours;
     // constructor
     public GreetingMessage(){
       super();
     }

     public GreetingMessage(Integer hour){
       this.hours = hour;
     }

     // return message
     public String printMessage(){

       configHour();

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

     private void configHour(){
       if (this.hours == null){
         DateTime var = new DateTime();
         this.hours = var.dateTime();
       }
     }
  }
