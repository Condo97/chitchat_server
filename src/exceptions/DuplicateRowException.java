package exceptions;

public class DuplicateRowException extends Exception {
    private String what, where;

    public DuplicateRowException(String what, String where) {
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
        return "DuplicateRowException{" +
                ", what='" + what + '\'' +
                ", where='" + where + '\'' +
                '}';
    }
}
