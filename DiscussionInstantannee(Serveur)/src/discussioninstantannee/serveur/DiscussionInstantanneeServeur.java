package discussioninstantannee.serveur;
public class DiscussionInstantanneeServeur {
    public static void main(String[] args) {
        Serveur s = new Serveur("127.0.0.1",43215,100);
        s.connecxion();
        System.out.println("Serveur ON");
    }    
}
