package fadarrizz.pizzachatbot.Model;

public class Pizza {
    private String name;
    private String img_url;
    private String price;

    public Pizza() {
    }

    public Pizza(String name, String img_url, String price) {
        this.name = name;
        this.img_url = img_url;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
