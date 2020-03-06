package ua.epam.springIntegrationApp.model;

public class Package {

    private int id;

    private DeliveryType type;

    public Package() {
    }

    public Package(int id, DeliveryType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DeliveryType getType() {
        return type;
    }

    public void setType(DeliveryType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Package{" +
                "id=" + id +
                ", type=" + type +
                '}';
    }
}
