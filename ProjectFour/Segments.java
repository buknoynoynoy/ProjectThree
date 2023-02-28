public class Segments {
    String[] inputStatement;
    String[] segments = new String[3];

    public Segments() {
        this.segments[0] = "";
        this.segments[1] = "";
        this.segments[2] = "";
    }

    public void prepareSegments(String statement) {
        inputStatement = statement.split("\\s+");
        for (int i = 0; i < inputStatement.length; i++) {
            this.segments[i] = inputStatement[i];
        }
    }

    public void clearSegments() {
        this.segments[0] = "";
        this.segments[1] = "";
        this.segments[2] = "";
    }

    public String getFirst() {
        return this.segments[0];
    }

    public String getSecond() {
        return this.segments[1];
    }

    public String getThird() {
        return this.segments[2];
    }

    public void setFirst(String token) {
        this.segments[0] = token;
    }

    public void setSecond(String token) {
        this.segments[1] = token;
    }

    public void setThird(String token) {
        this.segments[2] = token;
    }

    public static String removeNonAlphanumeric(String str) {
        // replace the given string
        // with empty string
        // except the pattern "[^a-zA-Z0-9]"
        str = str.replaceAll(
          "[^a-zA-Z0-9]", "");
 
        // return string
        return str;
    }

}
