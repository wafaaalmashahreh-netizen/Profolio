package smartparking.model;

public class User {
    private int userId;
    private String userName, email, idNumber, userType;
    // subtype fields
    private String faculty;      // student
    private String department;   // employee
    private String passportId;   // visitor

    public User() {}

    public int getUserId()             { return userId; }
    public void setUserId(int v)       { userId = v; }
    public String getUserName()        { return userName; }
    public void setUserName(String v)  { userName = v; }
    public String getEmail()           { return email; }
    public void setEmail(String v)     { email = v; }
    public String getIdNumber()        { return idNumber; }
    public void setIdNumber(String v)  { idNumber = v; }
 public String getUserType() {
    return userType == null ? "unknown" : userType;
}
    public void setUserType(String v)  { userType = v; }
    public String getFaculty()         { return faculty; }
    public void setFaculty(String v)   { faculty = v; }
    public String getDepartment()      { return department; }
    public void setDepartment(String v){ department = v; }
    public String getPassportId()      { return passportId; }
    public void setPassportId(String v){ passportId = v; }

    @Override
    public String toString() { return userName + " (" + userType + ")"; }
}



