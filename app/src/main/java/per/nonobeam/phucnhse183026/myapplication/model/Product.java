package per.nonobeam.phucnhse183026.myapplication.model;

public class Product {
    public int id;
    public String name;
    public String description;
    public double price;
    public int quantity;
    public int sold;

    public Product(int id, String name, String description, double price, int quantity, int sold) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.sold = sold;
    }
}
