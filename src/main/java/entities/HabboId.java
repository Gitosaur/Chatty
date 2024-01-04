package entities;

import java.util.Objects;

/**
 * Because the same name can be used in different hotels,
 * the id must be based on the name AND hotel
 */
public class HabboId {

    String name;
    Hotel hotel;

    public HabboId(String name, Hotel hotel) {
        this.name = name;
        this.hotel = hotel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabboId habboId = (HabboId) o;
        return name.equals(habboId.name) &&
                hotel == habboId.hotel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, hotel);
    }
}
