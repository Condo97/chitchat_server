package exceptions;

public class MissingRowException extends Exception {
    private String what, where;

    public MissingRowException(String what, String where) {
        this.what = what;
        this.where = where;
    }


    public String getWhat() {
        return what;
    }

    public String getWhere() {
        return where;
    }

    @Override
    public String toString() {
        return "MissingRowException{" +
                ", what='" + what + '\'' +
                ", where='" + where + '\'' +
                '}';
    }
}
