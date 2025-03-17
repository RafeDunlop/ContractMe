package nz.ac.canterbury.seng302.homehelper.dto;

public class UpdatePasswordDTO {

    private String currentPassword;
    private String newPassword;
    private String retypePassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRetypePassword() {
        return retypePassword;
    }

    public void setRetypePassword(String repeatedNewPassword) {
        this.retypePassword = repeatedNewPassword;
    }
}
