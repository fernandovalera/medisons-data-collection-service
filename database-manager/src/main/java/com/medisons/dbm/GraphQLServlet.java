package com.medisons.dbm;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.coxautodev.graphql.tools.SchemaParser;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import graphql.schema.GraphQLSchema;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.StaticDataFetcher;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.GraphQLInvocationInputFactory;
import graphql.servlet.GraphQLObjectMapper;
import graphql.servlet.GraphQLQueryInvoker;
import graphql.servlet.SimpleGraphQLHttpServlet;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

@WebServlet(name = "GraphQLServlet", urlPatterns = {"graphql"}, loadOnStartup = 1)
public class GraphQLServlet extends SimpleGraphQLHttpServlet {

    private static final SignalDataRepository signalDataRepository;

    static {
        MongoDatabase mongo = new MongoClient().getDatabase("medisons");
        signalDataRepository = new SignalDataRepository(mongo.getCollection("signalDataList"));
    }

    public GraphQLServlet() {
        super(invocationInputFactory(), queryInvoker(), objectMapper(), null, false);
    }

    private static GraphQLInvocationInputFactory invocationInputFactory() {
        return GraphQLInvocationInputFactory.newBuilder(createSchema()).build();
    }

    private static GraphQLSchema createSchema() {
        return SchemaParser.newParser()
                .file("schema.graphqls")
                .resolvers(new Query(signalDataRepository), new Mutation(signalDataRepository))
                .build()
                .makeExecutableSchema();
    }

    private static GraphQLQueryInvoker queryInvoker() {
        return GraphQLQueryInvoker.newBuilder().build();
    }

    private static GraphQLObjectMapper objectMapper() {
        return GraphQLObjectMapper.newBuilder().build();
    }
}