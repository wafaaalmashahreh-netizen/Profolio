package smartparking.model;

public class Validator {

    
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

   
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return email.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    }

   
    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) return false;
        return name.matches("^[a-zA-Z\\s]{3,}$");
    }

  
    public static boolean isValidIdNumber(String id) {
        if (!isNotEmpty(id)) return false;
        return id.matches("^\\d{8}$");
    }

 
    public static boolean isValidLicensePlate(String plate) {
        if (!isNotEmpty(plate)) return false;
        return plate.matches("^[A-Z0-9-]{5,8}$");
    }
}