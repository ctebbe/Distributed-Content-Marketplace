package cs555.tebbe.data;

/**
 * Created by ct.
 */
public class ContentStoreData {

    public final String ID;
    public final String address;
    public final String publisher;
    public final double price;
    public int numPurchases;
    public byte[] content = null;

    public ContentStoreData(String id, String address, String publisher, double price) {
        ID = id;
        this.address = address;
        this.price = price;
        this.numPurchases = 0;
        this.publisher = publisher;
    }

    public String toString() {
        return ID +" : "+ address +" : "+ price +" : "+ numPurchases + " : " + publisher + " - " + (content==null ? "PURCHASED" : "NOT PURCHASED");
    }

    @Override
    public boolean equals(Object object) {
        if(object == null || object.getClass() != getClass()) {
            return false;
        } else {
            ContentStoreData other = (ContentStoreData) object;
            return ID.equals(other.ID);
        }
    }
}
