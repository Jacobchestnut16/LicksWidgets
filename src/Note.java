public class Note {
    String message;
    String timeStamp;

    public Note(String m, String t){
        message = m;
        timeStamp = t;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
