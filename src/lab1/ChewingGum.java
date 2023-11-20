package lab1;

import java.util.Objects;

public class ChewingGum extends Food {

    private String flavour;

    public ChewingGum (String flavour, int calories){
        super("Жвачка", calories);
        this.flavour = flavour;
    }

    @Override
    public void consume() {
        System.out.println( this + "cъедено");
    }

    public String getFlavour() {
        return flavour;
    }

    public void setFlavour(String flavour) {
        this.flavour = flavour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChewingGum that = (ChewingGum) o;
        return Objects.equals(flavour, that.flavour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flavour);
    }

    @Override
    public String toString() {
        return super.toString() + " вкуса '" + flavour.toUpperCase() + "'";
    }
}
