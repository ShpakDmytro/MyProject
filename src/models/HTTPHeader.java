package models;
public class HTTPHeader {
    private String name;
    private String value;

    public HTTPHeader (String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
