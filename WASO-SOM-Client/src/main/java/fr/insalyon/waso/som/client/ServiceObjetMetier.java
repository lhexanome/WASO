package fr.insalyon.waso.som.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                jsonListe.add(sqlClientToJsonClient(row));
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM Client::getListeClient", ex);
        }
    }

    public void rechercherClientParNumero(Integer numero) throws ServiceException {
        try {

            List<Object[]> listObject = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE ClientID = ?", numero);
            if (listObject.size() == 0) return;

            this.container.add("client", sqlClientToJsonClient(listObject.get(0)));

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
                jsonListe.add(sqlClientToJsonClient(row));
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM Client::rechercherClientParDenomination", ex);
        }
    }

    public void rechercherClientParPersonne(int[] personneIds, String ville) throws ServiceException {
        try {

            // [*1] Request with "Ville LIKE ?" FAILED
            //      This filter was filter after request

            Set<Integer> clientsIds = new HashSet<Integer>();

            JsonArray jsonListe = new JsonArray();
            for(int personneId :personneIds) {
                List<Object[]> listeClients = this.dBConnection.launchQuery(
                        "SELECT cl.ClientID, cl.TypeClient, cl.Denomination, cl.Adresse, cl.Ville " +
                                "FROM CLIENT cl, COMPOSER co " +
                                "WHERE co.PersonneID = ? AND co.ClientID = cl.ClientID " + // [*1] AND cl.Ville LIKE ?
                                "ORDER BY cl.ClientID",
                        personneId);// [*1] '%' + ville + '%'

                for (Object[] row : listeClients) {

                    Integer clientId = (Integer) row[0];

                    if(!clientsIds.contains(clientId)
                            && ((String)(row[4])).indexOf(ville)!= -1) {// [*1]

                        jsonListe.add(sqlClientToJsonClient(row));

                        clientsIds.add(clientId);
                    }
                }
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw new ServiceException("Exception in SOM Client::rechercherClientParPersonne", ex);
        }
    }

    private JsonObject sqlClientToJsonClient(Object[] row) throws DBException {

        int clientId = (Integer) row[0];

        JsonObject jsonItem = new JsonObject();

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

        return jsonItem;

    }
}
