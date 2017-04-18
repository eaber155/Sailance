package timer;

/**
 * Created by Admin on 12-Apr-17.
 */

public class Sailors {
    public String nameOfParticipants;
    public Sailors(String nameOfParticipants){
        super();
        this.nameOfParticipants = nameOfParticipants;
    }

    public void setNameOfParticipants(String nameOfParticipants){
        this.nameOfParticipants = nameOfParticipants;
    }

    public String getNameOfParticipants(){
        setNameOfParticipants(nameOfParticipants);
        return nameOfParticipants;
    }
}
