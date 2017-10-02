package fr.insalyon.waso.som.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;

import java.util.List;
import java.util.TreeMap;

/**
 * @author WASO Team
 */
public class ServiceObjetMetier {

    protected DBConnection dBConnection;
    protected JsonObject container;

    public ServiceObjetMetier(DBConnection dBConnection, JsonObject container) {
        this.dBConnection = dBConnection;
        this.container = container;
    }

    public void getListeClient() throws ServiceException {
        try {
            JsonArray jsonListe = new JsonArray();

            List<Object[]> listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT ORDER BY ClientID");

            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM Client::getListeClient", ex);
        }
    }

    public void rechercherClientParNumero(Integer numero) throws ServiceException {
        try {
            JsonObject jsonObject = new JsonObject();

            List<Object[]> listObject = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE ClientID = ?", numero);
            if (listObject.size() == 0) return;

            Object[] row = listObject.get(0);

            Integer clientId = (Integer) row[0];
            jsonObject.addProperty("id", clientId);
            jsonObject.addProperty("type", (String) row[1]);
            jsonObject.addProperty("denomination", (String) row[2]);
            jsonObject.addProperty("adresse", (String) row[3]);
            jsonObject.addProperty("ville", (String) row[4]);

            List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
            JsonArray jsonSousListe = new JsonArray();
            for (Object[] innerRow : listePersonnes) {
                jsonSousListe.add((Integer) innerRow[1]);
            }

            jsonObject.add("personnes-ID", jsonSousListe);

            this.container.add("client", jsonObject);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM Client::ServiceObjetMetier", ex);
        }
    }

    public void rechercherClientParDenomination(String denomination, String ville) throws ServiceException {
        try {
            JsonArray jsonListe = new JsonArray();


            List<Object[]> listeClients = this.dBConnection.launchQuery(
                    "SELECT ClientID, TypeClient, Denomination, Adresse, Ville " +
                            "FROM CLIENT " +
                            "WHERE denomination LIKE ? AND ville LIKE ? " +
                            "ORDER BY ClientID",
                    '%'+denomination+'%',
                    '%'+ville+'%');

            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM Client::rechercherClientParDenomination", ex);
        }
    }
}
