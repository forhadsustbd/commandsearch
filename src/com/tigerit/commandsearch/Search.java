package com.tigerit.commandsearch;

/**
 * @author Forhad Ahmed
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import javax.print.Doc;

import static org.elasticsearch.node.NodeBuilder.*;

public class Search {

    private Node node;
    private Client client;
    private Gson gson;

    public Search() {
        node = nodeBuilder().clusterName("forhad_es").node();
        client = node.client();
        gson = new GsonBuilder().create();
    }

    public IndexResponse put(Document doc, String indexName, String typeName) {
        String doc_str = gson.toJson(doc);
        IndexResponse response = client.prepareIndex(indexName,typeName)
                .setSource(doc_str)
                .execute()
                .actionGet();
        // Index name
        String _index = response.getIndex();
        // Type name
        String _type = response.getType();
        // Document ID (generated or not)
        String _id = response.getId();
        // Version (if it's the first time you index this document, you will get: 1)
        long _version = response.getVersion();
        // isCreated() is true if the document is a new one, false if it has been updated
        boolean created = response.isCreated();

        return response;
    }

    public Document getDocument(String id, String indexName, String typeName) {
        GetResponse response = client.prepareGet(indexName, typeName, id)
                .execute()
                .actionGet();
        String mapStr = gson.toJson(response.getSource());
        Document document = gson.fromJson(mapStr, Document.class);

        return document;
    }

    public void searchDocument(String indexName, String searchString) {

        SearchResponse response = client.prepareSearch(indexName)
                .setQuery(QueryBuilders.queryStringQuery(searchString))
                .addHighlightedField("title",10,2)
                .addHighlightedField("description",10,2)
                .setSize(10).execute().actionGet();

        SearchHit[] searchHits = response.getHits().getHits();

        System.out.println(response.toString());

        for(SearchHit searchHit: searchHits) {
            System.out.println(searchHit.getSource().toString());
            System.out.println(searchHit.getHighlightFields().toString());
        }
    }

    public void closeAll() {
        node.close();
        client.close();
    }

    public static void main(String[] args) {

        Search search = new Search();
        String indexName = "linuxcommands";
        String typeName = "cdCommands";

        Document doc1 = new Document("ls","list view");


        search.searchDocument(indexName, "cd");
        /*Document document = search.getDocument("1", indexName, typeName);

        System.out.println(document.getTitle());
        System.out.println(document.getDescription());*/

        search.closeAll();
    }
}
