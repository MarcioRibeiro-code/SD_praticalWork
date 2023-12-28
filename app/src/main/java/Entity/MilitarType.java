package Entity;

public enum MilitarType {
    SEARGENT("seargent"),
    CAPE("cape"),
    SOLDIER("soldier");

    private final String typeString;

    MilitarType(String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString() {
        return typeString;
    }

    public static MilitarType fromString(String text) {
        for (MilitarType militarType : MilitarType.values()) {
            if (militarType.getTypeString().equalsIgnoreCase(text)) {
                return militarType;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

}
