package fr.insalyon.waso.sma;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.JsonHttpClient;
import fr.insalyon.waso.util.exception.ServiceException;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.message.BasicNameValuePair;

/**
 * @author WASO Team
 */
public class ServiceMetier {

    protected String somClientUrl;
    protected String somPersonneUrl;
    protected JsonObject container;

    protected JsonHttpClient jsonHttpClient;

    public ServiceMetier(String somClientUrl, String somPersonneUrl, JsonObject container) {
        this.somClientUrl = somClientUrl;
        this.somPersonneUrl = somPersonneUrl;
        this.container = container;

        this.jsonHttpClient = new JsonHttpClient();
    }

    public void release() {
        try {
            this.jsonHttpClient.close();
        } catch (IOException ex) {
            // Ignorer
        }
    }

    private HashMap<Integer, JsonObject> getJsonPersonnesHashed() throws ServiceException, IOException {
        JsonObject personneContainer = this.jsonHttpClient.post(this.somPersonneUrl, new BasicNameValuePair("SOM", "getListePersonne"));

        if (personneContainer == null) {
            throw new ServiceException("Appel impossible au Service Personne::getListePersonne [" + this.somPersonneUrl + "]");
        }

        HashMap<Integer, JsonObject> personnes = new HashMap<Integer, JsonObject>();

        for (JsonElement p : personneContainer.getAsJsonArray("personnes")) {

            JsonObject personne = p.getAsJsonObject();

            personnes.put(personne.get("id").getAsInt(), personne);
        }

        return personnes;

    }

    private void insertPersonnesInJsonClient(JsonObject jsonOutputClient, HashMap<Integer, JsonObject> personnes) throws IOException, ServiceException {

        JsonArray personnesID = jsonOutputClient.get("personnes-ID").getAsJsonArray();

        JsonArray outputPersonnes = new JsonArray();

        for (JsonElement personneID : personnesID) {
            JsonObject personne = personnes.get(personneID.getAsInt());
            outputPersonnes.add(personne);
        }

        jsonOutputClient.add("personnes", outputPersonnes);
    }
    private void insertPersonnesInJsonClientListe(JsonArray jsonOutputClientListe, HashMap<Integer, JsonObject> personnes) throws IOException, ServiceException {

        for (JsonElement clientElement : jsonOutputClientListe.getAsJsonArray()) {
            insertPersonnesInJsonClient(clientElement.getAsJsonObject(),personnes);

        }
    }

    public void getListeClient() throws ServiceException {
        try {

            // 1. Obtenir la liste des Clients

            JsonObject clientContainer = this.jsonHttpClient.post(this.somClientUrl, new BasicNameValuePair("SOM", "getListeClient"));

            if (clientContainer == null) {
                throw new ServiceException("Appel impossible au Service Client::getListeClient [" + this.somClientUrl + "]");
            }

            JsonArray jsonOutputClientListe = clientContainer.getAsJsonArray("clients"); //new JsonArray();


            // 2. Construire la liste des Personnes pour chaque Client (directement dans le JSON)
            insertPersonnesInJsonClientListe(jsonOutputClientListe,getJsonPersonnesHashed());


            // 3. Ajouter la liste de Clients au conteneur JSON

            this.container.add("clients", jsonOutputClientListe);

        } catch (IOException ex) {
            throw new ServiceException("Exception in SMA getListeClient", ex);
        }
    }


    public void rechercherClientParNumero(Integer numero) throws ServiceException {
        try {

            // 1. Obtenir le client

            JsonObject clientContainer = this.jsonHttpClient.post(this.somClientUrl,
                    new BasicNameValuePair("SOM", "rechercherClientParNumero"),
                    new BasicNameValuePair("numero", numero.toString())
            );

            if (clientContainer == null) {
                throw new ServiceException("Appel impossible au Service Client::rechercherClientParNumero [" + this.somClientUrl + "]");
            }

            JsonObject jsonOutputClient = clientContainer.getAsJsonObject("client");



            // 2. Construire la liste des Personnes pour le Client (directement dans le JSON)
            insertPersonnesInJsonClient(jsonOutputClient,getJsonPersonnesHashed());


            // 3. Ajouter la liste de Clients au conteneur JSON

            this.container.add("client", jsonOutputClient);

        } catch (IOException ex) {
            throw new ServiceException("Exception in SMA rechercherClientParNumero", ex);
        }
    }

}
